package com.viergegroup.swmain.managers

import com.shared.commons.AppHostSettingsData
import com.shared.commons.AppSettings
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.WatcherContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class HostCommManagerReconnectTest {

    companion object {
        private lateinit var testBaseDir: File

        @JvmStatic
        @BeforeAll
        fun initGlobalSettings() {
            testBaseDir = File("target/test-logs-reconnect").absoluteFile
            File(testBaseDir, "vierge/consolelogs").mkdirs()
            File(testBaseDir, "prosa/consolelogs").mkdirs()
            File(testBaseDir, "logs").mkdirs()
            val basePath = testBaseDir.absolutePath

            val appSettings = AppSettings.getInstance(basePath, basePath)
            appSettings.dataPath = basePath
            appSettings.dataPathMicrosoft = basePath
            appSettings.dataPathLinux = basePath
            appSettings.logsGlobalLevel = emptyList()
            appSettings.enableAlarms = false
            appSettings.enableTelegramAlarms = false
            appSettings.canReboot = false
        }
    }

    private val testHostType = HostType.TestingDUKPTHost
    private lateinit var serverSocket: ServerSocket
    private lateinit var serverThread: Thread
    private val serverRunning = AtomicBoolean(false)
    private val acceptedCount = AtomicInteger(0)
    private val acceptedSockets = CopyOnWriteArrayList<Socket>()
    @Volatile
    private var closeImmediatelyOnFirstAccept = false

    private lateinit var manager: HostCommManager
    private lateinit var hostConfig: AppHostSettingsData

    @BeforeEach
    fun setUp() {
        acceptedCount.set(0)
        acceptedSockets.clear()
        closeImmediatelyOnFirstAccept = false

        serverSocket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        serverRunning.set(true)
        serverThread = Thread({
            while (serverRunning.get() && !serverSocket.isClosed) {
                try {
                    val client = serverSocket.accept()
                    val count = acceptedCount.incrementAndGet()
                    acceptedSockets.add(client)
                    if (closeImmediatelyOnFirstAccept && count == 1) {
                        // Envía TCP FIN limpio para provocar bytesRead == -1 (EOF)
                        client.close()
                    }
                } catch (_: Exception) {
                    // ServerSocket cerrado en tearDown
                }
            }
        }, "test-iso-server").apply {
            isDaemon = true
            start()
        }

        hostConfig = AppHostSettingsData().apply {
            hostName = testHostType.hostTypeName
            hostIp = "127.0.0.1"
            hostPort = serverSocket.localPort
            hostIsActiveOnWindows = true
            hostIsActiveOnLinux = true
            isLinux = !System.getProperty("os.name").lowercase().contains("win")
            keepConnectionAlive = true
            enableLogOns = false
            enableEchoTests = false
            enableTls = false
            hostConnectionTimeOut = 3000
            checkMessageReceptionEvery = 200
            sendEchoTestEvery = 150
            maxConnectionTries = 3
            mustSendTxnToHost = true
            logsHostLevel = emptyList()
            logsOperationLevel = emptyList()
            commLogsPath = File(testBaseDir, "logs").absolutePath
            hostLogsFilePath = File(testBaseDir, "logs").absolutePath
        }
        registerHostConfig(testHostType, hostConfig)

        manager = HostCommManager()
        setField(manager, "hostType", testHostType)
        setField(manager, "lastReconnectAttemptAtMs", 0L)
        // Evitar que downBalancer ejecute scripts del SO durante tests
        setField(manager, "balancerDown", true)
        // Evitar que startThreads() lance ReceiveMessageThread/ReversesThread en segundo plano
        // compitiendo con las invocaciones deterministas de cada test
        parkBackgroundThreads(manager)
    }

    @AfterEach
    fun tearDown() {
        try {
            stopManagerThreads(manager)
            manager.disconnectHost(WatcherContext.getInstance())
        } catch (_: Exception) {
        }
        serverRunning.set(false)
        acceptedSockets.forEach { runCatching { it.close() } }
        runCatching { serverSocket.close() }
        runCatching { serverThread.join(1000) }
    }

    // -------------------------------------------------------------------------
    // CASO 1: EchoTestThread detecta !isConnected() -> ECHO_THREAD_DISCONNECTED
    // -------------------------------------------------------------------------
    @Test
    fun `caso 1 - EchoTestThread reconecta cuando detecta host desconectado`() {
        hostConfig.enableEchoTests = true
        hostConfig.sendEchoTestEvery = 120L

        assertFalse(manager.isConnected(), "Debe iniciar desconectado")

        val echoClass = HostCommManager::class.java.declaredClasses.first { it.simpleName == "EchoTestThread" }
        val ctor = echoClass.getDeclaredConstructor(HostCommManager::class.java).apply { isAccessible = true }
        val runnable = ctor.newInstance(manager) as Runnable
        val echoThread = Thread(runnable, "test-echo-thread").apply {
            isDaemon = true
            start()
        }

        try {
            val reconnected = waitUntil(timeoutMs = 6000) { manager.isConnected() && acceptedCount.get() >= 1 }
            assertTrue(reconnected, "EchoTestThread debió reconectar contra el ServerSocket local")
            assertTrue(acceptedCount.get() >= 1)
        } finally {
            echoThread.interrupt()
        }
    }

    // -------------------------------------------------------------------------
    // CASO 2: receiveMessage recibe EOF remoto (bytesRead == -1, noDisconnect=true) -> RECEIVE_EOF
    // -------------------------------------------------------------------------
    @Test
    fun `caso 2 - receiveMessage reconecta inmediatamente ante EOF remoto`() {
        closeImmediatelyOnFirstAccept = true
        val initial = manager.connectToHost(WatcherContext.getInstance())
        assertEquals(0, initial.errorCode)
        assertTrue(waitUntil(2000) { acceptedCount.get() >= 1 })
        // Detener ReceiveMessageThread para invocar receiveMessage de forma determinista
        setField(manager, "lastReconnectAttemptAtMs", 0L)

        // Pequeña pausa para asegurar que el FIN del servidor llegue al buffer del cliente
        Thread.sleep(150)

        manager.receiveMessage(noDisconnect = true, readingTimeout = 2000)

        assertTrue(manager.isConnected(), "Debe quedar reconectado tras RECEIVE_EOF")
        assertTrue(acceptedCount.get() >= 2, "Debe existir una segunda conexión TCP aceptada (actual=${acceptedCount.get()})")
    }

    // -------------------------------------------------------------------------
    // CASO 3: receiveMessage detecta PROSA_SILENCE > 40s en SocketTimeoutException -> PROSA_SILENCE
    // -------------------------------------------------------------------------
    @Test
    fun `caso 3 - receiveMessage reconecta ante PROSA_SILENCE`() {
        val initial = manager.connectToHost(WatcherContext.getInstance())
        assertEquals(0, initial.errorCode)
        assertEquals(1, acceptedCount.get())
        setField(manager, "lastReconnectAttemptAtMs", 0L)
        // Simular que el último mensaje recibido fue hace 50 segundos (> silenceThresholdMs = 40s)
        setField(manager, "lastHostMessageAtMs", System.currentTimeMillis() - 50_000L)
        setField(manager, "balancerDown", true)

        manager.receiveMessage(noDisconnect = true, readingTimeout = 250)

        assertTrue(manager.isConnected(), "Debe reconectar tras PROSA_SILENCE")
        assertTrue(acceptedCount.get() >= 2, "Debe existir una segunda conexión TCP aceptada (actual=${acceptedCount.get()})")
    }

    // -------------------------------------------------------------------------
    // CASO 4: receiveMessage sufre SocketException (no timeout) -> RECEIVE_EXCEPTION
    // -------------------------------------------------------------------------
    @Test
    fun `caso 4 - receiveMessage reconecta ante excepcion de lectura no timeout`() {
        val initial = manager.connectToHost(WatcherContext.getInstance())
        assertEquals(0, initial.errorCode)
        assertEquals(1, acceptedCount.get())
        setField(manager, "lastReconnectAttemptAtMs", 0L)

        val failingInput = DataInputStream(object : InputStream() {
            override fun read(): Int = throw SocketException("Connection reset")
            override fun read(b: ByteArray, off: Int, len: Int): Int = throw SocketException("Connection reset")
        })
        setField(manager, "dataInputStream", failingInput)

        manager.receiveMessage(noDisconnect = true, readingTimeout = 1000)

        assertTrue(manager.isConnected(), "Debe reconectar tras RECEIVE_EXCEPTION")
        assertTrue(acceptedCount.get() >= 2, "Debe existir una segunda conexión TCP aceptada (actual=${acceptedCount.get()})")
        assertEquals(0, manager.commReadExceptionCounter.get(), "commReadExceptionCounter debe resetearse tras reconectar")
    }

    // -------------------------------------------------------------------------
    // CASO 5: sendMessage sufre Broken pipe y otra excepcion de escritura -> SEND_BROKEN_PIPE / SEND_EXCEPTION
    // -------------------------------------------------------------------------
    @Test
    fun `caso 5a - sendMessage reconecta ante Broken pipe`() {
        val initial = manager.connectToHost(WatcherContext.getInstance())
        assertEquals(0, initial.errorCode)
        assertEquals(1, acceptedCount.get())
        setField(manager, "lastReconnectAttemptAtMs", 0L)

        val brokenPipeOutput = object : OutputStream() {
            override fun write(b: Int) = throw SocketException("Broken pipe")
            override fun write(b: ByteArray) = throw SocketException("Broken pipe")
            override fun write(b: ByteArray, off: Int, len: Int) = throw SocketException("Broken pipe")
        }
        setField(manager, "outputStream", brokenPipeOutput)

        manager.sendMessage(buildEchoMetaData(), WatcherContext.getInstance())

        assertTrue(manager.isConnected(), "Debe reconectar tras SEND_BROKEN_PIPE")
        assertTrue(acceptedCount.get() >= 2, "Debe existir una segunda conexión TCP aceptada (actual=${acceptedCount.get()})")
        assertEquals(0, manager.brokenPipeCounter.get(), "brokenPipeCounter debe resetearse tras reconectar")
    }

    @Test
    fun `caso 5b - sendMessage reconecta ante excepcion generica de escritura`() {
        val initial = manager.connectToHost(WatcherContext.getInstance())
        assertEquals(0, initial.errorCode)
        assertEquals(1, acceptedCount.get())
        setField(manager, "lastReconnectAttemptAtMs", 0L)

        val failingOutput = object : OutputStream() {
            override fun write(b: Int) = throw IOException("Simulated write failure")
            override fun write(b: ByteArray) = throw IOException("Simulated write failure")
            override fun write(b: ByteArray, off: Int, len: Int) = throw IOException("Simulated write failure")
        }
        setField(manager, "outputStream", failingOutput)

        manager.sendMessage(buildEchoMetaData(), WatcherContext.getInstance())

        assertTrue(manager.isConnected(), "Debe reconectar tras SEND_EXCEPTION")
        assertTrue(acceptedCount.get() >= 2, "Debe existir una segunda conexión TCP aceptada (actual=${acceptedCount.get()})")
        assertEquals(0, manager.commWriteExceptionCounter.get(), "commWriteExceptionCounter debe resetearse tras reconectar")
    }

    // -------------------------------------------------------------------------
    // CASO 6: sendMessage con streams cerrados (null) -> SEND_STREAMS_CLOSED
    // -------------------------------------------------------------------------
    @Test
    fun `caso 6 - sendMessage reconecta cuando outputStream o dataInputStream son null`() {
        setField(manager, "outputStream", null)
        setField(manager, "dataInputStream", null)
        setField(manager, "lastReconnectAttemptAtMs", 0L)

        manager.sendMessage(buildEchoMetaData(), WatcherContext.getInstance())

        assertTrue(manager.isConnected(), "Debe reconectar tras SEND_STREAMS_CLOSED")
        assertTrue(acceptedCount.get() >= 1, "Debe abrir conexión TCP contra el ServerSocket local")
    }

    // -------------------------------------------------------------------------
    // BONUS: Single-flight (concurrencia) y Cooldown de reconnect()
    // -------------------------------------------------------------------------
    @Test
    fun `reconnect es single-flight ante llamadas concurrentes y respeta cooldown`() {
        val threadCount = 5
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val pool = Executors.newFixedThreadPool(threadCount)

        try {
            repeat(threadCount) { idx ->
                pool.submit {
                    try {
                        startLatch.await(2, TimeUnit.SECONDS)
                        if (manager.reconnect("CONCURRENT_$idx", WatcherContext.getInstance(), minIntervalMs = 0L)) {
                            successCount.incrementAndGet()
                        }
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }
            startLatch.countDown()
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Todos los hilos deben terminar")
            assertEquals(1, successCount.get(), "Solo 1 hilo debe ejecutar la reconexión simultánea")
            assertEquals(1, acceptedCount.get(), "Solo debe abrirse 1 conexión TCP")

            // Cooldown: un segundo intento inmediato con minIntervalMs = 5000 debe omitirse
            val secondAttempt = manager.reconnect("COOLDOWN_TEST", WatcherContext.getInstance(), minIntervalMs = 5_000L)
            assertFalse(secondAttempt, "Debe omitirse por cooldown")
            assertEquals(1, acceptedCount.get(), "No debe abrirse otra conexión durante el cooldown")
        } finally {
            pool.shutdownNow()
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private fun buildEchoMetaData(): Iso8583MetaData {
        val td = TransactionData().apply {
            hostType = testHostType
            transactionType = TransactionType.EchoTest
            systemTraceAuditNumber = "123456"
            networkManagmentInformationCode = "301"
        }
        return Iso8583MetaData().apply {
            transactionData = td
            transactionType = TransactionType.EchoTest
            isoAscii = "ISO0260000500800822000000000000004000000000000001124120000123456301"
        }
    }

    private fun parkBackgroundThreads(hcm: HostCommManager) {
        listOf("receiveMessageThread", "reversesThread").forEach { fieldName ->
            val dummy = Thread({
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (_: InterruptedException) {
                }
            }, "parked-$fieldName").apply {
                isDaemon = true
                start()
            }
            setField(hcm, fieldName, dummy)
        }
    }

    private fun stopManagerThreads(hcm: HostCommManager) {
        listOf("receiveMessageThread", "threadEchoTest", "reversesThread").forEach { fieldName ->
            val thread = getField(hcm, fieldName) as? Thread
            thread?.interrupt()
            setField(hcm, fieldName, null)
        }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(50)
        }
        return condition()
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerHostConfig(hostType: HostType, config: AppHostSettingsData) {
        val field = AppSettings::class.java.getDeclaredField("hostConfigurationMap")
        field.isAccessible = true
        val map = field.get(null) as MutableMap<HostType, AppHostSettingsData>
        map[hostType] = config
    }

    private fun setField(target: Any, name: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }

    private fun getField(target: Any, name: String): Any? {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target)
    }
}
