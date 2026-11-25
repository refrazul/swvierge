package com.viergegroup.swmain.managers
import  com.viergegroup.swmain.dbmanagers.TransactionDBManager;

import com.shared.commons.*
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.isoprocessor.models.iso.Iso8583MessageType
import com.viergegroup.isoprocessor.models.iso.Iso8583TransactionMetaData
import com.viergegroup.isoprocessor.parser.IsoHostParser
import com.viergegroup.swmain.dbmodels.TransactionDBData

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Condition

import java.nio.ByteBuffer 
import java.nio.channels.SocketChannel
import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.io.IOException
import org.springframework.beans.BeanUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.Collections

import com.viergegroup.swmain.dtos.comm.DisconnectHostResponseDto
import com.viergegroup.swmain.dtos.comm.HostStatusResponseDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.CardType
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.MerchantType
import com.viergegroup.swmain.entities.catalogs.OperationId
import com.viergegroup.swmain.entities.catalogs.PaymentPlanType
import com.viergegroup.swmain.entities.catalogs.PointOfServiceEntryMode
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import com.viergegroup.swmain.entities.catalogs.TransactionType

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.util.Calendar
import java.util.TimeZone
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import kotlin.jvm.Synchronized

internal class HostCommManager{

        // Instrumentación (solo lectura): mide respuestas/peticiones no reclamadas (huérfanas).
    // Sano = ambas bajas (se consumen en segundos). Si crecen monótonas durante el día -> huérfanos.
  // contador acumulado de huerfanos echo/logon barridos (esperado, alto volumen -> no se loguea por-item)
    private var prunedEchoOrphans: Long = 0

    fun logCollectionMetrics() {
        pruneStaleReceivedMessages()   // P3: expira huerfanos tambien en idle
        val recv = synchronized(receivedMessages) { receivedMessages.size }
        val req  = synchronized(requestMessages)  { requestMessages.size }
        WatcherGlobalLog.commRaw("COLLECTIONS_METRICS logicalNetworkId [${logicalNetworkId}] receivedMessages=${recv} requestMessages=${req} prunedEchoOrphans=${prunedEchoOrphans}")
    }

    // expira respuestas huerfanas de receivedMessages tras 2x el timeout del waiter (clientWaitAnswerTimeOut).
    private fun pruneStaleReceivedMessages() {
        val now = System.currentTimeMillis()
        val cutoff = now - getAppHostSettingsData().clientWaitAnswerTimeOut * 2
        synchronized(receivedMessages) {
            val it = receivedMessages.iterator()
            while (it.hasNext()) {
                val msg = it.next() ?: continue
                if (msg.receivedAtMs in 1L..cutoff) { onStaleOrphanPruned(msg, now); it.remove() }
            }
        }
    }

    // (instrumentacion): echo/logon 0810 = esperado (solo cuenta); 0210/otro = RED FLAG (WARN por-item).
    private fun onStaleOrphanPruned(msg: Iso8583MetaData, now: Long) {
        val td = msg.transactionData
        val nmic = td?.networkManagmentInformationCode ?: ""
        val isEchoOrLogon = td?.getMessageType() == Iso8583MessageType.Message0810 && (nmic == "301" || nmic == "001")
        if (isEchoOrLogon) prunedEchoOrphans++
        else WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] HUERFANO_NO_RECLAMADO MTI=${td?.getMessageType()} STAN=${td?.systemTraceAuditNumber} RRN=${td?.rrn} RC=${td?.responseCode} type=${td?.transactionType} edadMs=${now - msg.receivedAtMs} -> respuesta llego pero NINGUN waiter la recogio (revisar: respuesta tardia del host / timeout corto / duplicado / posible reverso espurio)")
    }

    private  class ReversesThread(var hostCommManager: HostCommManager):Runnable{

        override fun run() {
            var seconds=0L 
        
            while(true) { 
                if(hostCommManager.isConnected()|| hostCommManager.enableThreadsHardcode()) {

                    if (hostCommManager.thereAreReverses() || seconds >= hostCommManager.getAppHostSettingsData().checkReversesEvery&&!hostCommManager.isCheckingReverses ) {
                        val therearereverses=hostCommManager.thereAreReverses() 
                       WatcherGlobalLog.reverseDebug("CHECK_REVERSES hostCommManager.thereAreReverses() "+therearereverses )       
                       // WatcherGlobalLog.debug("CHECK_REVERSES hostCommManager.thereAreReverses() "+therearereverses )       

                        if(therearereverses ){
                            Thread.sleep( hostCommManager.getAppHostSettingsData().hostMessageCheckDelay*2)
                        }
 
                     
                        hostCommManager.checkAndSendPendingReverses()
                        seconds = 0
                        WatcherContext.finishOperation()
                    }
                    seconds +=  hostCommManager.getAppHostSettingsData().hostMessageCheckDelay
                    Thread.sleep( hostCommManager.getAppHostSettingsData().hostMessageCheckDelay)

                }
               
                Thread.sleep( hostCommManager.getAppHostSettingsData().hostMessageCheckDelay*2)

                

            }
        }
    }
    private class EchoTestThread(var hostCommManager: HostCommManager):Runnable{

        override fun run() {
                var metricTick = 0
            while(true) {
                Thread.sleep(hostCommManager.getAppHostSettingsData().sendEchoTestEvery)
              //  val watcher =WatcherContext.commThread("EchoTestThread",hostCommManager.hostType)
                 /*  if (hostCommManager.isItThreeAM()) {
                        hostCommManager.getHostWatcher().debug("3_00_AM MUST_REBOOT_SYSTEM")
                        WatcherGlobalLog.commError("3_00_AM MUST_REBOOT_SYSTEM")
                        hostCommManager.rebootSystem()
                        return  
                    } */
                                    if (hostCommManager.isItThreeAM()) {
                    hostCommManager.getHostWatcher().debug("3_00_AM MUST_REBOOT_SYSTEM")
                    WatcherGlobalLog.commError("3_00_AM MUST_REBOOT_SYSTEM")
                    hostCommManager.rebootSystem()
                    // Sin return: si el reinicio ocurre, el proceso muere igual; si NO ocurre (guardas
                    // lo bloquean o falla), el hilo sigue vivo con keep-alive/reconexión en vez de
                    // quedar muerto hasta el próximo reinicio manual.
                }
                   

                   hostCommManager.getHostWatcher().debug("SEND ECHOS BEACUASE: hostCommManager.isConnected() [${hostCommManager.isConnected()}] &&  isReadyToEchos: [${hostCommManager.isReadyToEchos}]")

                if(hostCommManager.isConnected()) {

                    hostCommManager. sendEchoTestRequest()
                  // watcher.log.close()
                }else{
                    if(!hostCommManager.isConnected()){
                        WatcherGlobalLog.commError("${hostCommManager.hostType} Host IS DISCONNECTED I WILL TRY TO RECONNECT .-.")
                        hostCommManager.getHostWatcher().debug("EchoTestThread - Host IS DISCONNECTED I WILL TRY TO RECONNECT .-.")
                        hostCommManager.reconnect("ECHO_THREAD_DISCONNECTED")
                    }
                    hostCommManager.getHostWatcher().debug("EchoTestThread -CAN'T SEND ECHOS BEACUASE: hostCommManager.isConnected() [${hostCommManager.isConnected()}] &&  isReadyToEchos: [${hostCommManager.isReadyToEchos}]")
                }

                if (++metricTick % 10 == 0) hostCommManager.logCollectionMetrics()   // ~cada 5 min (10 × echo)
                WatcherContext.finishOperation()

            }
        }
    }


    private  class ReceiveMessageThread(var hostCommManager: HostCommManager):Runnable{

        override fun run() {
            while(true) {
                try {
                    if(hostCommManager.isConnectedAndNotReady()) {
                        val receiveMessageResponse = hostCommManager.receiveMessage(true,37000)
                        hostCommManager.manageReceivedMessage(receiveMessageResponse)
                        WatcherContext.finishOperation()
                    }
                    Thread.sleep(hostCommManager.getAppHostSettingsData().checkMessageReceptionEvery)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                } catch (e: Exception) {
                    // el hilo lector NUNCA debe morir por una excepcion no capturada.
                    WatcherGlobalLog.commError("ReceiveMessageThread loop EXCEPTION (hilo sobrevive): ${e.message}")
                    val backoff = try {
                        hostCommManager.getAppHostSettingsData().checkMessageReceptionEvery
                    } catch (_: Exception) {
                        1000L
                    }
                    try {
                        Thread.sleep(backoff)
                    } catch (ie2: InterruptedException) {
                        Thread.currentThread().interrupt();
                        return
                    }
                }
            }
        }
    } 


      private var hostType= HostType.TestingDUKPTHost
     private var checkReverses=false
      private var isCheckingReverses=false
      private var socket = Socket()
     private var outputStream: OutputStream?=null
    private var inputStream: InputStream?=null
     private var dataInputStream: DataInputStream?=null
     private var maxBuferResponseSize=17096
   //  private var isReceivingMessage=false
     private var isReadyToEchos=false
     private var timeoutsCounter=0
     // --- P0: reloj de silencio de Prosa ---
     @Volatile private var lastHostMessageAtMs: Long = 0L
     private val silenceThresholdMs: Long = 40_000L   // 40s; > gap max sano del echo (~24s)

     private var isSendingMessage:Boolean=false
    private var isReceivingMessage:Boolean=false
 
     private var requestMessages= mutableListOf<Iso8583MetaData>()
    private  var receivedMessages= mutableListOf<Iso8583MetaData>()
    private var frameCarryOver: ByteArray = ByteArray(0)
    //private val dbPersistScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dbPersistExecutor = java.util.concurrent.ThreadPoolExecutor(
        4, 4, 30L, java.util.concurrent.TimeUnit.SECONDS,
        java.util.concurrent.ArrayBlockingQueue<Runnable>(1000),
        java.util.concurrent.RejectedExecutionHandler { _, _ ->
            WatcherGlobalLog.dbError("cola persistencia ISO llena -> se omite un updateIsoResponseByUuid (best-effort)")
        }
    )



    
    private var isConnectedAndReady=false
    private val reconnecting = AtomicBoolean(false)
    @Volatile private var lastReconnectAttemptAtMs: Long = 0L
    private var threadEchoTest:Thread?=null
    private var receiveMessageThread:Thread?=null
    private var reversesThread:Thread?=null
    private  var watcher:Watcher?=null
    private val msgLock = java.util.concurrent.locks.ReentrantLock()

    private  var maxConnectionTries:Int=7 
    private  var connectionTries:Int=0
    private val rebootService: RebootService=RebootService()
    private  var connectionsOkCounter:Int=0

     @Volatile private  var balancerDown:Boolean=false
    @Volatile private  var balancerUp:Boolean=false

    var commReadExceptionThreshold : Int= AppSettings.getInstance().commReadExceptionThreshold
    var commOpenExceptionThreshold : Int= AppSettings.getInstance().commOpenExceptionThreshold
    var commWriteExceptionThreshold : Int= AppSettings.getInstance().commWriteExceptionThreshold
    val commTimeoutExceptionCounter = AtomicInteger(0)
    val clientTimeoutsCounter = AtomicInteger(0)

    val commReadExceptionCounter = AtomicInteger(0)
    val commOpenExceptionCounter = AtomicInteger(0)
    val commWriteExceptionCounter = AtomicInteger(0)
    val nullPointerExceptionCounter = AtomicInteger(0)

    val logicalNetworkId = AtomicInteger(0)
    val brokenPipeCounter = AtomicInteger(0)

    var commReadExceptionDetails : String=""
    var commOpenExceptionDetails : String=""
    var commWriteExceptionDetails : String=""
   
      var lastTimeoutTimestamp = 0L // este valor debe inicializarse en el contexto adecuado

      enum class Severity(val level: Int) { CRITICAL(1), HIGH(2), MEDIUM(3), LOW(4), INFO(5) }
enum class Remediation { RESTART_SERVICE, CLOSE_SOCKETS, KILL_PROCESSES, RESTART_PROSA_PORT, CHECK_IPSEC_PROVIDER }

val severityByCause = mapOf(
    AlarmCause.SOCKET_BROKEN_PIPE to Severity.CRITICAL,
    AlarmCause.SOCKET_CONNECTION_RESET to Severity.HIGH,
    AlarmCause.SOCKET_CLOSED to Severity.MEDIUM,
    AlarmCause.SOCKET_READ_TIMEOUT to Severity.MEDIUM,
    AlarmCause.SOCKET_CONNECT_TIMEOUT to Severity.HIGH,
    AlarmCause.SOCKET_BIND_ERROR to Severity.HIGH,
    AlarmCause.UNKNOWN_HOST to Severity.HIGH,
    AlarmCause.NETWORK_UNREACHABLE to Severity.HIGH,
    AlarmCause.GENERIC_COMM_ERROR to Severity.MEDIUM,

    AlarmCause.SYSTEM_RESTART to Severity.HIGH,
    AlarmCause.RECONNECT_OK to Severity.INFO,
    AlarmCause.RECONNECT_FAIL to Severity.HIGH,
    AlarmCause.ECHO_REQUEST to Severity.INFO
)

val actionsByCause = mapOf(
    AlarmCause.SOCKET_BROKEN_PIPE to setOf(Remediation.CLOSE_SOCKETS, Remediation.RESTART_SERVICE, Remediation.CHECK_IPSEC_PROVIDER),
    AlarmCause.SOCKET_CONNECTION_RESET to setOf(Remediation.CLOSE_SOCKETS, Remediation.CHECK_IPSEC_PROVIDER),
    AlarmCause.SOCKET_CLOSED to setOf(Remediation.CLOSE_SOCKETS),
    AlarmCause.SOCKET_READ_TIMEOUT to setOf(Remediation.CLOSE_SOCKETS),
    AlarmCause.SOCKET_CONNECT_TIMEOUT to setOf(Remediation.CLOSE_SOCKETS, Remediation.CHECK_IPSEC_PROVIDER),
    AlarmCause.SOCKET_BIND_ERROR to setOf(Remediation.KILL_PROCESSES, Remediation.RESTART_SERVICE),
    AlarmCause.UNKNOWN_HOST to setOf(Remediation.CHECK_IPSEC_PROVIDER),
    AlarmCause.NETWORK_UNREACHABLE to setOf(Remediation.CHECK_IPSEC_PROVIDER, Remediation.CLOSE_SOCKETS, Remediation.RESTART_PROSA_PORT),
    AlarmCause.GENERIC_COMM_ERROR to setOf(Remediation.CLOSE_SOCKETS),

    AlarmCause.SYSTEM_RESTART to setOf(Remediation.RESTART_SERVICE),
    AlarmCause.RECONNECT_OK to emptySet(),
    AlarmCause.RECONNECT_FAIL to setOf(Remediation.CLOSE_SOCKETS, Remediation.RESTART_SERVICE, Remediation.CHECK_IPSEC_PROVIDER),
    AlarmCause.ECHO_REQUEST to emptySet()
)



  private val MAX_REVERSE_ATTEMPTS = 2
    private val reverseAttempts = ConcurrentHashMap<Long, AtomicInteger>()     // conteo de fallos por reverseId
    // métricas
    @Volatile private var inFlight = java.util.concurrent.atomic.AtomicInteger(0)
    fun getInFlight(): Int = inFlight.get()
    fun isHealthy(): Boolean = isConnected()

    // reversos
    @Volatile private var reversesWorkerEnabled: Boolean = true
    fun enableReversesWorker(enable: Boolean) { reversesWorkerEnabled = enable }


    // Función que verifica si la hora actual es las 3:00 AM
    internal fun isItThreeAM(): Boolean { 
        val mexicoTimeZone = TimeZone.getTimeZone("America/Mexico_City")
        val calendar = Calendar.getInstance(mexicoTimeZone)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val  restartServicesHour= AppSettings.getInstance().restartServicesHour
        val restartServicesMinutes= AppSettings.getInstance().restartServicesMinutes
        getHostWatcher().debug("restartServicesHour ${restartServicesHour}") 
        getHostWatcher().debug("restartServicesMinutes ${restartServicesMinutes}")

        // Verificar si es exactamente 3:00 AM 
        return currentHour == restartServicesHour && currentMinute == restartServicesMinutes
    } 
    fun handleException(e: Exception) {
        
        val cause = classifyByMessage(e)
        val severity = severityByCause[cause] ?: Severity.MEDIUM
        val actions = actionsByCause[cause].orEmpty()

        getHostWatcher().debug("[${severity.name}] Excepción detectada -> ${cause.name}, Msg=${e.message.toString()}")

        // Email certero
        val emailBody = buildAlarmEmailBody(cause, severity, actions, e)
        //AlarmEmailService.sendAlarmEmail(cause, emailBody)

        // Ejecutar acciones
        performRemediations(actions)
    }


    private fun classifyByMessage(e: Exception): AlarmCause {

        val msg = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

        return when {
            msg.contains("broken pipe", ignoreCase = true) ->
                AlarmCause.SOCKET_BROKEN_PIPE

            msg.contains("connection reset", ignoreCase = true) ->
                AlarmCause.SOCKET_CONNECTION_RESET

            msg.contains("socket closed", ignoreCase = true) ->
                AlarmCause.SOCKET_CLOSED

            msg.contains("read timed out", ignoreCase = true) ->
                AlarmCause.SOCKET_READ_TIMEOUT

            msg.contains("connect timed out", ignoreCase = true) ->
                AlarmCause.SOCKET_CONNECT_TIMEOUT

            (msg.contains("bind", ignoreCase = true) &&
            msg.contains("address", ignoreCase = true)) ||
            msg.contains("address already in use", ignoreCase = true) ->
                AlarmCause.SOCKET_BIND_ERROR

            msg.contains("unknown host", ignoreCase = true) ->
                AlarmCause.UNKNOWN_HOST

            msg.contains("network is unreachable", ignoreCase = true) ||
            msg.contains("no route to host", ignoreCase = true) ->
                AlarmCause.NETWORK_UNREACHABLE

            else -> AlarmCause.GENERIC_COMM_ERROR
        }
    }

    private fun buildAlarmEmailBody(
        cause: AlarmCause,
        sev: Severity,
        actions: Set<Remediation>,
        e: Exception
    ): String = """
    ${cause.title.trim()}
    ${cause.message.trim()}

    Severidad: ${sev.name} (nivel ${sev.level})
    Acciones recomendadas: ${if (actions.isEmpty()) "N/A" else actions.joinToString(", ")}

    Detalle técnico:
    ${e::class.qualifiedName}: ${e.message ?: "sin mensaje"}
    """.trimIndent()

    private fun performRemediations(actions: Set<Remediation>) {

        if (Remediation.RESTART_SERVICE in actions)
            rebootByPipeBroken()
    }




    fun closeSockets() { 
        try { 
            val process = ProcessBuilder("sudo", "/bin/bash", "/home/ec2-user/closesockets.sh")
            .directory(File("/home/ec2-user"))
            .redirectErrorStream(true)
            .start()  
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  closeSockets_EXECUTING /home/ec2-user/closesockets.sh ")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  closeSockets_script:\n$output")
        } catch (e: Exception) {
                handleException(e)

            val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR," ${err} closeSockets() {  ${e.message}")

             getHostWatcher().error("logicalNetworkId [${logicalNetworkId}]  closeSockets_error: ${e.message}")
        }
    }

    fun downBalancer(details:String) :String{
        val startTime = System.currentTimeMillis()
        try {
            WatcherGlobalLog.rebootDebug("### downBalancer START logicalNetworkId [$logicalNetworkId] balancerDown=[$balancerDown] details=[$details]")

            if(balancerDown) {
                WatcherGlobalLog.rebootDebug("### downBalancer SKIPPED logicalNetworkId [$logicalNetworkId] balancerDown ya es true")
                return ""
            }
            clientTimeoutsCounter.set(0)
            WatcherGlobalLog.rebootDebug("### downBalancer EXECUTING logicalNetworkId [$logicalNetworkId] /home/ec2-user/downbalancer.sh")
            val process = ProcessBuilder("sudo", "/bin/bash", "/home/ec2-user/downbalancer.sh")
            .directory(File("/home/ec2-user"))
            .redirectErrorStream(true)
            .start()  
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  downBalancer_EXECUTING /home/ec2-user/downbalancer.sh ")
            
            val finished = process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)
            val elapsedMs = System.currentTimeMillis() - startTime
            val output = if (finished) process.inputStream.bufferedReader().readText() else "TIMEOUT_15s"
            val exitCode = if (finished) process.exitValue() else -1
            if (!finished) {
                process.destroyForcibly()
                WatcherGlobalLog.rebootDebug("### downBalancer TIMEOUT logicalNetworkId [$logicalNetworkId] downbalancer.sh no termino en 15s (elapsedMs=$elapsedMs), proceso destruido")
                getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] downBalancer: downbalancer.sh no termino en 15s, matado")
            }
            WatcherGlobalLog.rebootDebug("### downBalancer RESULT logicalNetworkId [$logicalNetworkId] finished=[$finished] exitCode=[$exitCode] elapsedMs=[$elapsedMs] output=[$output]")
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  downBalancer:\n$output")
    
            WatcherGlobalLog.rebootDebug("### downBalancer SENDING_ALARM logicalNetworkId [$logicalNetworkId]")
            AlarmEmailService.sendAlarmEmail(AlarmCause.BALANCER_DOWN," logicalNetworkId [${logicalNetworkId}] $details \n  SCRIPT_OUTPUT:\n$output ")
            balancerDown=true
            WatcherGlobalLog.rebootDebug("### downBalancer END logicalNetworkId [$logicalNetworkId] balancerDown=[$balancerDown] totalElapsedMs=[${System.currentTimeMillis() - startTime}]")
            return output
        } catch (e: Exception) {
            WatcherGlobalLog.rebootDebug("### downBalancer EXCEPTION logicalNetworkId [$logicalNetworkId] elapsedMs=[${System.currentTimeMillis() - startTime}] error=[${e.javaClass.name}: ${e.message}]")
            AlarmEmailService.sendAlarmEmail(AlarmCause.BALANCER_ERROR," logicalNetworkId [${logicalNetworkId}]   closeSockets() {  ${e.message}")

             getHostWatcher().error("downBalancer_error: ${e.message}")
        }
        return ""
    }



      fun upBalancer(details:String) {
        try { 

            if(balancerUp) return 
            lastTimeoutTimestamp=System.currentTimeMillis()
            val process = ProcessBuilder("sudo", "/bin/bash", "/home/ec2-user/upbalancer.sh")
            .directory(File("/home/ec2-user"))
            .redirectErrorStream(true)
            .start()  
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  upbalancer_EXECUTING /home/ec2-user/upbalancer.sh ")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  upbalancer:\n$output")
   
            AlarmEmailService.sendAlarmEmail(AlarmCause.BALANCER_UP," logicalNetworkId [${logicalNetworkId}] $details \n  SCRIPT_OUTPUT:\n$output ")
            balancerUp=true

        } catch (e: Exception) {
            
            AlarmEmailService.sendAlarmEmail(AlarmCause.BALANCER_ERROR," logicalNetworkId [${logicalNetworkId}]   closeSockets() {  ${e.message}")

             getHostWatcher().error("upbalancer_error: ${e.message}")
        }
    }


   fun downBalancerAndRebootSystem(){ 
       val outputscript= downBalancer("ANTES DE REINICIAR SE INTENTARA SACAR DEL BALANCEADOR ")
        if(!outputscript.contains(" No se baja"))
              rebootService.canRebootSystem("ANTES DE ESTA LINEA ESTABA EL DE downBalancer")
    }
    
    
    fun rebootSystem(){ 
        rebootService.canRebootSystem()
           
    }


    fun rebootByPipeBroken(){
        WatcherGlobalLog.rebootDebug("### rebootByPipeBroken")

        val outputscript= downBalancer("SE DETECTO UN \"BROKEN PIPE\", SIGNIFICA QUE EL SOCKET FUE CERRADO POR EL SERVIDOR , EXCEPCION CRITICA, ANTES DE REINICIAR SE INTENTARA SACAR DEL BALANCEADOR ")
        if(!outputscript.contains(" No se baja"))
           rebootService.rebootByPipeBroken()
           
    }    
    fun setCheckReverses(checkReverses:Boolean){
        this.checkReverses=checkReverses 
    }
    fun enableThreadsHardcode():Boolean{
        return AppSettings.getHostConfig(hostType).hostResponseHardcode
    } 
    fun getAppHostSettingsData():AppHostSettingsData{
        return AppSettings.getHostConfig(hostType)
    } 
    fun thereAreReverses():Boolean{
        return checkReverses&&!isCheckingReverses
    }
   
    fun checkAndSendPendingReverses() {  
        while (isCheckingReverses)    
        getHostWatcher().debug(" logicalNetworkId [${logicalNetworkId}]  INIT checkAndSendPendingReverses - Checking reverses process started")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  INIT checkAndSendPendingReverses - Checking reverses process started")
        var rrn=""
        isCheckingReverses = true
        var reversesIds= mutableListOf<Long>()
     
        try {      
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  BEFORE fetching reverses from TransactionDBManager for hostType=$hostType")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  BEFORE fetching reverses from TransactionDBManager for hostType=$hostType")
            reversesIds = TransactionDBManager.getTxnsToBeReversed(hostType).toMutableList()
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  AFTER fetching reverses - Retrieved ${reversesIds.size} transactions")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  AFTER fetching reverses - Retrieved ${reversesIds.size} transactions")
             
        } catch (ex: Exception) {
                            handleException(ex)

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]   checkAndSendPendingReverses() {  ${ex.message}")

            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  checkAndSendPendingReverses EXCEPTION: ${ex.message}")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  checkAndSendPendingReverses EXCEPTION: ${ex.message}")
            return  
        }
    
        if (reversesIds.isEmpty()) {
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  No pending reverses found. Exiting checkAndSendPendingReverses.")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  No pending reverses found. Exiting checkAndSendPendingReverses.")
            isCheckingReverses = false
            checkReverses = false
            return  
        }
       
      
        val ids = reversesIds.joinToString("|") 
        WatcherGlobalLog.reverseDebug("ids= $ids") 
        
        for (reverseId in reversesIds) {
             WatcherGlobalLog.reverseDebugRrn(rrn,"INICIO DE FUNCION")

            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  Processing reverseId=${reverseId}}")
              WatcherGlobalLog.reverseDebugRrn(rrn,"BUSCAR TRANSACCION ORIGINAL INICIO")

            val reverseDb=TransactionDBManager.findTxn(reverseId, getHostWatcher()) 
              WatcherGlobalLog.reverseDebugRrn(rrn,"BUSCAR TRANSACCION ORIGINAL FIN")

            if(reverseDb==null){
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  reverseDb IS_NULL")
                continue
            }
            WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}] reverseDbData=${GsonConfig.gson.toJson(reverseDb)}")

            
             rrn=reverseDb.rrn
            getHostWatcher().debug(" logicalNetworkId [${logicalNetworkId}]  Processing transactionId=${reverseDb.transactionId}, rrn=${reverseDb.rrn}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  Processing transactionId=${reverseDb.transactionId}, rrn=${reverseDb.rrn}")
       
            TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.WaitingForResponse)
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  Updated transactionId=${reverseDb.transactionId} to WaitingForResponse")
            WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  Updated transactionId=${reverseDb.transactionId} to WaitingForResponse")
                          WatcherGlobalLog.reverseDebugRrn(rrn,"CONSTRUYE EL ISO INICIO")

            val reverseDto = reverseDb.buildIso8583Reverse(getHostWatcher())
                          WatcherGlobalLog.reverseDebugRrn(rrn,"CONSTRUYE EL ISO FIN")

            val isoMetaData = reverseDto.iso8583MetaData
            val uuidStr=StringUtils.generateUUID()
            reverseDto.reverseDbData.uuid=StringUtils.hexStringToByteArray(uuidStr) 
            reverseDto.transactionDto.uuid=uuidStr
            reverseDto.transactionData.uuid=uuidStr
            reverseDto.iso8583MetaData.guid=StringUtils.hexStr2UUID(uuidStr)
            reverseDto.iso8583MetaData.setUUID(uuidStr)


           //  val affiliation=reverseDto.affiliation  
            val requestResponseCode=reverseDb.responseCode
                var headerIso =""
            var  responseCode=reverseDb.responseCode
 
               var responseCodeOrg=reverseDb.responseCode
              var rrnOrg=""
                var termSerOrg=""
            try{   
                reverseDto.reverseDbData.isoASCIIRequest= StringUtils.asciiToByteArray( isoMetaData.isoAscii)
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  reverseDto.reverseDbData.isoASCIIRequest ${isoMetaData.isoAscii}")
             
            }catch(ex:Exception){
                                handleException(ex)

                AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR," logicalNetworkId [${logicalNetworkId}]  checkAndSendPendingReverses() {  ${ex.message}")
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")

            } 
                                      WatcherGlobalLog.reverseDebugRrn(rrn,"INSERCION EN DB INICIAL INICIO")

           TransactionDBManager.insertTransactionData(getHostWatcher(), reverseDto.reverseDbData)
                                      WatcherGlobalLog.reverseDebugRrn(rrn,"INSERCION EN DB INICIAL FIN")

            if (isoMetaData.errorCode != 0) {
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR BUILDING REVERSE for transactionId=${reverseDb.transactionId}: ${isoMetaData.errorMessage}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  ERROR BUILDING REVERSE for transactionId=${reverseDb.transactionId}: ${isoMetaData.errorMessage}")
                reverseDto.reverseDbData.apply {
                    errorCode = ErrorCode.fromId(isoMetaData.errorCode)
                    errorCodeNumber = isoMetaData.errorCode
                    errorMessage = isoMetaData.errorMessage
                }
    
            } else {    
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  Sending ISO 8583 request for transactionId=${reverseDb.transactionId}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  Sending ISO 8583 request for transactionId=${reverseDb.transactionId}")
                isoMetaData.transactionType=TransactionType.Reverse
                        WatcherGlobalLog.reverseDebugRrn(rrn,"ENVIO DE ISO INICIO")

                val response = sendIsoAndWaitForResponse(isoMetaData)
                                        WatcherGlobalLog.reverseDebugRrn(rrn,"ENVIO DE ISO Y PARSEO DE RESPUESTA FIN")

                try{
                    
                    if(response.getIsoRequestASCII()!=""){
                        reverseDto.reverseDbData.isoASCIIRequest =StringUtils.asciiToByteArray( response.getIsoRequestASCII())
                         getHostWatcher().debug(" reverseDto.reverseDbData.getIsoRequestASCII ${response.getIsoRequestASCII()}")
                        WatcherGlobalLog.reverseDebugRrn(rrn," reverseDto.reverseDbData.getIsoRequestASCII ${response.getIsoRequestASCII()}")
                    
                    }    
    
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  reverseDto.reverseDbData.isoASCIIRequest ${response.getIsoRequestASCII()}")
                       
                    if(response.getIsoResponseASCII()!="") {

                         var iscorespascii=response.getIsoResponseASCII().replace("ISO0260000501111","ISO0260000509420").replace("ISO0260038501111","ISO0260038509420").replace("ISO0260039501111","ISO0260039509420")
                
                        reverseDto.reverseDbData.isoASCIIResponse=StringUtils.asciiToByteArray( iscorespascii)
                        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  reverseDto_reverseDbData_isoASCIIResponse ${response.getIsoResponseASCII()}")
                        WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  reverseDto_reverseDbData_isoASCIIResponse ${response.getIsoResponseASCII()}")
                    } 

 
                         
                       
                     if( response.getIsoResponseASCII()!=""&&!response.getIsoResponseASCII().contains("0430")){ 
                          // reverseDto.reverseDbData.isoASCIIRequest=ByteArray(0)
                           reverseDto.reverseDbData.cardNumber=StringUtils.maskCardNumber( reverseDto.reverseDbData.cardNumber)
 
                            responseCode="HA" 
                            response.errorCodeEnum=ErrorCode.fromResponseCode(responseCode)
                            response.errorCode=response.errorCodeEnum.errorCodeInt

                            headerIso = response.getIsoResponseASCII().take(12)
                             if(response.transactionResponse!=null){
                                response.transactionResponse!!.responseCode=responseCode 
                                rrnOrg=response.transactionResponse!!.rrn
                                termSerOrg=response.transactionResponse!!.terminalSerialNumber
                             } 
                            
                            response.errorMessage=response.errorCodeEnum.errorCodeMessage+"  headerIso [${headerIso}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}]"
 
                            AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR," reverseId [${reverseId}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] \n ISO_ASCII [${response.getIsoResponseASCII()}]  ")
                               
                            TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.Rejected)
                           reverseDto.reverseDbData.transactionStatus=TransactionStatus.Rejected 
 
                            reverseDto.reverseDbData.errorCode = ErrorCode.fromResponseCode(responseCode)
                            reverseDto.reverseDbData.errorCodeNumber = response.errorCode  
                            reverseDto.reverseDbData.errorMessage = response.errorMessage
                            reverseDto.reverseDbData.responseCode = responseCode  
                            
                          TransactionDBManager.insertTransactionDataEnd(getHostWatcher(), reverseDto.reverseDbData) 

                          isCheckingReverses = false
                          checkReverses = false
                          continue 
                    }   



                }catch(ex:Exception){
                                    handleException(ex)

                      val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
                    AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  logicalNetworkId [${logicalNetworkId}]   checkAndSendPendingReverses() {  ${ex.message}")
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")
                     AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR," reverseId [${reverseId}] [${err}]  logicalNetworkId [${logicalNetworkId}]  EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")


                        WatcherGlobalLog.reverseDebugRrn(rrn," reverseId [${reverseId}] [${err}]  logicalNetworkId [${logicalNetworkId}]  EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")

                } 
 
                  responseCode=response.getResponseCode()
                  if(responseCode==""){
                    responseCode="HB"
                  }
                response.errorCodeEnum=ErrorCode.fromResponseCode(responseCode)
                response.errorCode=response.errorCodeEnum.errorCodeInt
                response.errorMessage=response.errorCodeEnum.errorCodeMessage

                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  REVERSE_responseCode=${responseCode}, response.errorMessage =${ response.errorMessage} response.errorCode =${ response.errorCode}")
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  REVERSE RESPONSE for responseCode=${responseCode} ,transactionId=${reverseDb.transactionId}: errorCode=${response.errorCode}, errorMessage=${response.errorMessage}, isOk=${response.isOk()}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  REVERSE RESPONSE responseCode=${responseCode}, for transactionId=${reverseDb.transactionId}: errorCode=${response.errorCode}, errorMessage=${response.errorMessage}, isOk=${response.isOk()}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  AppSettings.getInstance().forceDeleteReverses=${AppSettings.getInstance().forceDeleteReverses}")
                WatcherGlobalLog.reverseDebugRrn(rrn," logicalNetworkId [${logicalNetworkId}]  response.errorCode  = ${ response.errorCode } responseCode=${responseCode}") 
                
                val hostSettings=AppSettings.getHostConfig(hostType)
                val sendHardcode=hostSettings.hostResponseHardcode
                 val errorCodeSimulated= hostSettings.getErrorCodeForTransaction( TransactionType.Reverse)

                if(!sendHardcode){


                    if (response.getIsoResponseASCII()!="" &&responseCode=="00" ) {   
                                    TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.Reversed)
                                    reverseDto.reverseDbData.transactionStatus=TransactionStatus.Approved
                                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  TransactionId=${reverseDb.transactionId} successfully reversed")
                                    WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  TransactionId=${reverseDb.transactionId} successfully reversed")
                                
                                    reverseAttempts.remove(reverseId)
 
                                } else {    
                                    val attempts = reverseAttempts.computeIfAbsent(reverseId) { java.util.concurrent.atomic.AtomicInteger(0) }.incrementAndGet()
                                    if(attempts<=MAX_REVERSE_ATTEMPTS){
                                         TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.MustBeReversed)
                                     AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR,"attempts [${attempts}] reverseId [${reverseId}]  RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] \n ISO_ASCII [${response.getIsoResponseASCII()}]  ")

                                    }else{ 
                                         TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.ReversedFailed)
                                         AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR,"SE HA CAMBIADO EL ESTATUS DE ESTA TRANSACCION A ReverseFailed, detalles: attempts [${attempts}] reverseId [${reverseId}]  RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] \n ISO_ASCII [${response.getIsoResponseASCII()}]  ")
                                         reverseAttempts.remove(reverseId)
                                        WatcherGlobalLog.commRaw("TRANSACCION  ReverseFailed detalles: attempts [${attempts}] reverseId [${reverseId}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] ISO_ASCII [${response.getIsoResponseASCII()}] ")

                                    }
                                    reverseDto.reverseDbData.transactionStatus=TransactionStatus.Rejected
                                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}")
                                
                                    WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}")
                                     WatcherGlobalLog.commRaw("TRANSACCION  ReverseRejected detalles: attempts [${attempts}] reverseId [${reverseId}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] ISO_ASCII [${response.getIsoResponseASCII()}] ")

                                }  
                  }else{ 
 
                        responseCode=errorCodeSimulated.responseCode
                
                         WatcherGlobalLog.reverseDebug("REVERSE_SIMULATED responseCode : [${responseCode}]")
                                 if(errorCodeSimulated==ErrorCode.Approved){
                                      TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.Reversed)
                                      reverseDto.reverseDbData.transactionStatus=TransactionStatus.Approved
                                      getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  TransactionId=${reverseDb.transactionId} successfully reversed")
                                      WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  TransactionId=${reverseDb.transactionId} successfully reversed")
                                            
                                 }else{ 
                                    TransactionDBManager.updateTransactionStatus(reverseId, TransactionStatus.MustBeReversed)
                                    reverseDto.reverseDbData.transactionStatus=TransactionStatus.Rejected
                                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}")
                                    WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}")
          
                               }  
                }
         
               
                reverseDto.reverseDbData.errorCode = ErrorCode.fromResponseCode(responseCode) 
                reverseDto.reverseDbData.errorCodeNumber = reverseDto.reverseDbData.errorCode .errorCodeInt
                 reverseDto.reverseDbData.responseCode = responseCode  

                reverseDto.reverseDbData.errorMessage =  reverseDto.reverseDbData.errorCode .errorCodeMessage+" INTERNAL_REVERSE  headerIso [$headerIso] requestResponseCode [${requestResponseCode}] response_responseCode [${responseCode}]"
                WatcherGlobalLog.reverseDebugRrn(rrn," reverseDto.reverseDbData.errorMessage =${ reverseDto.reverseDbData.errorMessage}  reverseDto.reverseDbData.errorCodeNumber =${ reverseDto.reverseDbData.errorCodeNumber}")
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] Updated reverse data for transactionId=${reverseDb.transactionId}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}]  Updated reverse data for transactionId=${reverseDb.transactionId}")
            } 

            reverseDto.reverseDbData.cardNumber=StringUtils.maskCardNumber( reverseDto.reverseDbData.cardNumber)
                                         WatcherGlobalLog.reverseDebugRrn(rrn,"INSERCION EN BD FINAL INICIO")

            TransactionDBManager.insertTransactionDataEnd(getHostWatcher(), reverseDto.reverseDbData) 
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] Saved updated transaction for transactionId=${reverseDb.transactionId}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"logicalNetworkId [${logicalNetworkId}] Saved updated transaction for transactionId=${reverseDb.transactionId}")
                                                     WatcherGlobalLog.reverseDebugRrn(rrn,"INSERCION EN BD FINAL FIN")

        }   
    
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  checkAndSendPendingReverses process completed.")
        WatcherGlobalLog.reverseDebugRrn(rrn," logicalNetworkId [${logicalNetworkId}]  checkAndSendPendingReverses process completed.")
        
    
        isCheckingReverses = false
        checkReverses = false
    }
    

/*Segundo comentario 

  fun startReversesThread(hostType:HostType, watcher: Watcher){
        if(reversesThread!=null) return
        watcher.reverseDebug("startThreads  ${hostType} INIT ")
        reversesThread = Thread(ReversesThread(this))
        val config=AppSettings.getHostConfig(hostType)
        if(hostType.name!=config.hostName){
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR LOADING HOST_CONFIG = ${hostType.name} ${config.hostName} ")
 
        }
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  startReversesThread_enableEchoTests  ${config.enableEchoTests}  ")
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  startReversesThread_keepConnectionAlive  ${config.keepConnectionAlive}  ")
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  startReversesThread_enableLogOns  ${config.enableLogOns}  ")
 
        try {
            reversesThread!!.start()
        } catch (e: Exception) {
            watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  HostManager  ${hostType} EXCEPTION INIT: ${e.message} ")

        }
    }*/

@Synchronized
fun startReversesThread(hostType: HostType, watcher: Watcher) {
    if (reversesThread?.isAlive == true) return
    watcher.reverseDebug("startReversesThread ${hostType} INIT")
    val config = AppSettings.getHostConfig(hostType)
    if (hostType.name != config.hostName) {
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR LOADING HOST_CONFIG = ${hostType.name} ${config.hostName} ")
    }
    try {
        reversesThread = Thread(ReversesThread(this), "hostcomm-reverses-${hostType}")
            .also { it.isDaemon = true; it.start() }
    } catch (e: Exception) {
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  HostManager  ${hostType} EXCEPTION INIT: ${e.message} ")
    }
}



/*segundo comentario 
    fun startThreads(hostType:HostType, watcher: Watcher){
        if(receiveMessageThread!=null&&receiveMessageThread!=null&&reversesThread!=null) return

        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}] startThreads  ${hostType} INIT ")
        threadEchoTest = Thread(EchoTestThread(this))
        receiveMessageThread = Thread(ReceiveMessageThread(this))
        reversesThread = Thread(ReversesThread(this))
        val config=AppSettings.getHostConfig(hostType)
        if(hostType.name!=config.hostName){
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR LOADING HOST_CONFIG = ${hostType.name} ${config.hostName} ")

        }
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  enableEchoTests  ${config.enableEchoTests}  ")
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  enableEchoTests  ${config.enableEchoTests}  ")

        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}] keepConnectionAlive  ${config.keepConnectionAlive}  ")
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  keepConnectionAlive  ${config.keepConnectionAlive}  ")

        watcher.reverseDebug(" logicalNetworkId [${logicalNetworkId}] enableLogOns  ${config.enableLogOns}  ") 
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  enableLogOns  ${config.enableLogOns}  ") 



        try {
            if(config.enableEchoTests)
              threadEchoTest!!.start()
            if(config.keepConnectionAlive)
              receiveMessageThread!!.start()

            watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  reversesThread_START  ")
 
            reversesThread!!.start()

        } catch (e: Exception) {

                handleException(e)

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  logicalNetworkId [${logicalNetworkId}]  startThreads(hostType:HostType, watcher: Watcher){ ${e.message}")

            watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  HostManager  ${hostType} EXCEPTION INIT: ${e.message} ")

        }*/

        @Synchronized
fun startThreads(hostType: HostType, watcher: Watcher) {
    val config = AppSettings.getHostConfig(hostType)
    if (hostType.name != config.hostName) {
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR LOADING HOST_CONFIG = ${hostType.name} ${config.hostName} ")
    }
    watcher.debug("logicalNetworkId [${logicalNetworkId}] startThreads ${hostType} enableEchoTests=${config.enableEchoTests} keepConnectionAlive=${config.keepConnectionAlive} enableLogOns=${config.enableLogOns}")

    try {
        // isAlive != true cubre: nunca creado, o creado y ya muerto (p. ej. EchoTestThread tras el reboot de las 3:30 AM)
        if (config.enableEchoTests && threadEchoTest?.isAlive != true) {
            threadEchoTest = Thread(EchoTestThread(this), "hostcomm-echo-${hostType}")
                .also { it.isDaemon = true; it.start() }
        }
        if (config.keepConnectionAlive && receiveMessageThread?.isAlive != true) {
            receiveMessageThread = Thread(ReceiveMessageThread(this), "hostcomm-receive-${hostType}")
                .also { it.isDaemon = true; it.start() }
        }
        if (reversesThread?.isAlive != true) {
            reversesThread = Thread(ReversesThread(this), "hostcomm-reverses-${hostType}")
                .also { it.isDaemon = true; it.start() }
        }
    } catch (e: Exception) {
        handleException(e)
        AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR, "logicalNetworkId [${logicalNetworkId}]  startThreads(hostType:HostType, watcher: Watcher){ ${e.message}")
        watcher.reverseDebug("logicalNetworkId [${logicalNetworkId}]  HostManager  ${hostType} EXCEPTION INIT: ${e.message} ")
    }
}




    
      fun init(hostType:HostType,logicalNetworkId:Int, watcher: Watcher){
        this.watcher=watcher
        this.hostType=hostType
        this.logicalNetworkId.set(logicalNetworkId)
        //startReversesThread(hostType, watcher)
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  $hostType init")
 
      //  this.transactionsDBServiceOLD=transactionsDBServiceOLD
    }

    fun init(hostType:HostType, watcher: Watcher){
        this.watcher=watcher
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  $hostType init")
        this.hostType=hostType
      //  startReversesThread(hostType, watcher)

      //  this.transactionsDBServiceOLD=transactionsDBServiceOLD
    }
    fun init(hostType:HostType){
        this.hostType=hostType
      //  startReversesThread(hostType, watcher)

        //  this.transactionsDBServiceOLD=transactionsDBServiceOLD
    }
 

    fun tryToConnect(){
        var config=AppSettings.getHostConfig(hostType)
        
        if(hostType.name!=config.hostName){
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR LOADING HOST_CONFIG = ${hostType.name} ${config.hostName} ")

        }
        var response:Iso8583TransactionMetaData=Iso8583TransactionMetaData()
        WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  BEGIN tryToConnect isConnectedAndNotReady() [${isConnectedAndNotReady()}]" )
        WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  RETURN isConnectedAndNotReady() [${isConnectedAndNotReady()}] [${hostType}] while in tryToConnect socket.isConnected[${socket.isConnected}]  isConnectedAndReady [${isConnectedAndReady}]" )
        if(isConnectedAndNotReady()){
           return  
        }
        response.errorCode=-1 
        connectionTries=0
  
        while ( response.errorCode!=0&&!isConnectedAndNotReady()){ 
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  tryToConnect - keepConnectionAlive ${config.keepConnectionAlive} ")
            if(config.keepConnectionAlive) {
                WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  BEGIN [${hostType}] while in tryToConnect | keepConnectionAlive ${config.keepConnectionAlive} " )
                if (config.hostIsActive()) {
                    try {
                        if(response.errorCode==0){
                            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  CONNECTED OK [${hostType}] " )
                            getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}]  CONNECTED OK [${hostType}] ")
                            return 
                        }

                        if(connectionTries>=maxConnectionTries){
                            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  BREAK [${hostType}] while in tryToConnect")
                             return 
                        }else{  
                             response = connectToHost(getHostWatcher())

                        }

                    }catch (e:Exception){
                                        handleException(e)

                        AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  tryToConnect(){ ${e.message}")

                        WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  connectTOHost Error:  ${e.message}")
                        getHostWatcher().error( "logicalNetworkId [${logicalNetworkId}]  connectTOHost Error:  ${e.message}")
    
                    }
                } else {
                    getHostWatcher().debug(  "logicalNetworkId [${logicalNetworkId}]  ${hostType} IS_INACTIVE")
                    response.errorCode=0
                    return
                }
            }else{
                response.errorCode=0
                return 

            }

            WatcherGlobalLog.commError( "END [${hostType}] while in tryToConnect | connectionTries:[${connectionTries}] | socket.isConnected[${socket.isConnected}]  isConnectedAndReady [${isConnectedAndReady}]")
        }
       

    }
    fun isConnected():Boolean
    {
        return socket.isConnected&& isConnectedAndReady
    }

    fun isConnectedAndNotReady():Boolean
    {
        return socket.isConnected
    }
    fun hostStatus(): HostStatusResponseDto {
        val response= HostStatusResponseDto()
        response.hostName= AppSettings.getHostConfig(hostType).hostName
        response.hostIp= AppSettings.getHostConfig(hostType).hostIp
        response.hostPort=  AppSettings.getHostConfig(hostType).hostPort
        response.timeOut=  AppSettings.getHostConfig(hostType).hostConnectionTimeOut
        response.isConnected= socket.isConnected
        if(response.isConnected){
            response.errorCode=0
            response.errorMessage=""
        }
        return response
    }



    fun disconnectHost(watcher: Watcher): DisconnectHostResponseDto {
        val response= DisconnectHostResponseDto()
       watcher.debug( "logicalNetworkId [${logicalNetworkId}]  HOST IS DISCONNECTING...")
       WatcherGlobalLog.commError(  "logicalNetworkId [${logicalNetworkId}]  ${hostType} HOST IS DISCONNECTING...")
       isSendingMessage=false
       isReceivingMessage=false

       frameCarryOver = ByteArray(0)   // el arrastre pertenece al stream muerto -> descartar

        response.hostIp= AppSettings.getHostConfig(hostType).hostIp
        response.hostPort=  AppSettings.getHostConfig(hostType).hostPort
        response.timeOut=  AppSettings.getHostConfig(hostType).hostConnectionTimeOut
        response.isConnected= socket.isConnected
        // if(!socket.isConnected) return response
        try { 
            if(outputStream !=null){
                outputStream!!.flush()
                outputStream!!.close()
            }
            if(dataInputStream !=null){
                dataInputStream!!.close()
            }
            if(inputStream !=null){
                inputStream!!.close()
            }
            socket.close()

            response.errorCode=0
            response.errorMessage="logicalNetworkId [${logicalNetworkId}]  HOST DISCONNECTED SUCCESSFULLY"

            closeSockets()

            response.isConnected= socket.isConnected
            Thread.sleep(500) // Ajusta el tiempo según sea necesario
            WatcherGlobalLog.commError(  "logicalNetworkId [${logicalNetworkId}] ${hostType} socket.isConnected [${socket.isConnected}]...")
     
        }catch (e:Exception){
                            handleException(e)

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  logicalNetworkId [${logicalNetworkId}]   fun disconnectHost(watcher: Watcher): DisconnectHostResponseDto ${e.message}")

            WatcherGlobalLog.commError(  "logicalNetworkId [${logicalNetworkId}]  ${hostType} disconnectHost EXCEPTION: "+e.message)

           watcher.error( "logicalNetworkId [${logicalNetworkId}]  disconnectHost EXCEPTION "+e.message)
            response.errorMessage=e.message.toString()
        }
         finally {
            try { socket.close() } catch (_: Exception) {}   // cerrar el socket viejo aunque flush()/close() haya lanzado (evita fuga de FD)
            outputStream = null
            inputStream = null
            dataInputStream = null
            socket = Socket()          // socket fresco SIEMPRE, aunque flush()/close() lance
        }
        isConnectedAndReady =false
       watcher.debug( "logicalNetworkId [${logicalNetworkId}]  HOST DESCONNECTED")

        return response

    }

    fun isReconnecting(): Boolean = reconnecting.get()

    /**
     * Reconecta de forma controlada (single-flight + cooldown) el socket con el host:
     * desconecta limpiamente (cerrando streams/socket y descartando frameCarryOver)
     * y vuelve a conectar (TCP/TLS + LogOn si aplica + hilos de comunicación).
     *
     * @param reason Motivo/evento que disparó la reconexión (para trazabilidad en logs).
     * @param watcher Watcher a utilizar durante la reconexión.
     * @param minIntervalMs Tiempo mínimo entre intentos de reconexión para evitar tormentas.
     * @return true si el host quedó conectado y listo; false si se omitió o falló.
     */
    fun reconnect(
        reason: String,
        watcher: Watcher = getHostWatcher(),
        minIntervalMs: Long = 5_000L
    ): Boolean {
        val config = AppSettings.getHostConfig(hostType)
        if (!config.hostIsActive() || !config.keepConnectionAlive) {
            WatcherGlobalLog.commError(
                "logicalNetworkId [${logicalNetworkId}] RECONNECT_SKIPPED host=${hostType} reason=[${reason}] active=${config.hostIsActive()} keepAlive=${config.keepConnectionAlive}"
            )
            return false
        }

        val now = System.currentTimeMillis()
        if (minIntervalMs > 0L && (now - lastReconnectAttemptAtMs) < minIntervalMs) {
            WatcherGlobalLog.commError(
                "logicalNetworkId [${logicalNetworkId}] RECONNECT_SKIPPED_COOLDOWN host=${hostType} reason=[${reason}] elapsedMs=${now - lastReconnectAttemptAtMs} minIntervalMs=${minIntervalMs}"
            )
            return false
        }

        if (!reconnecting.compareAndSet(false, true)) {
            WatcherGlobalLog.commError(
                "logicalNetworkId [${logicalNetworkId}] RECONNECT_ALREADY_IN_PROGRESS host=${hostType} reason=[${reason}]"
            )
            return false
        }

        lastReconnectAttemptAtMs = now
        val startMs = System.currentTimeMillis()
        WatcherGlobalLog.commError(
            "logicalNetworkId [${logicalNetworkId}] RECONNECT_START host=${hostType} reason=[${reason}]"
        )
        watcher.debug("logicalNetworkId [${logicalNetworkId}] RECONNECT_START host=${hostType} reason=[${reason}]")

        return try {
            isConnectedAndReady = false
            disconnectHost(watcher)

            val connectResponse = connectToHost(watcher)
            val ok = connectResponse.errorCode == 0 && isConnected()
            val elapsedMs = System.currentTimeMillis() - startMs

            if (ok) {
                commReadExceptionCounter.set(0)
                commWriteExceptionCounter.set(0)
                commTimeoutExceptionCounter.set(0)
                brokenPipeCounter.set(0)
                timeoutsCounter = 0
                WatcherGlobalLog.commRaw(
                    "logicalNetworkId [${logicalNetworkId}] RECONNECT_SUCCESS host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs}"
                )
                watcher.debug(
                    "logicalNetworkId [${logicalNetworkId}] RECONNECT_SUCCESS host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs}"
                )
            } else {
                WatcherGlobalLog.commError(
                    "logicalNetworkId [${logicalNetworkId}] RECONNECT_FAILED host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs} errorCode=${connectResponse.errorCode} errorMessage=[${connectResponse.errorMessage}]"
                )
                watcher.error(
                    "logicalNetworkId [${logicalNetworkId}] RECONNECT_FAILED host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs} errorCode=${connectResponse.errorCode} errorMessage=[${connectResponse.errorMessage}]"
                )
            }
            ok
        } catch (e: Exception) {
            handleException(e)
            val elapsedMs = System.currentTimeMillis() - startMs
            WatcherGlobalLog.commError(
                "logicalNetworkId [${logicalNetworkId}] RECONNECT_EXCEPTION host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs} err=${e.message}"
            )
            watcher.error(
                "logicalNetworkId [${logicalNetworkId}] RECONNECT_EXCEPTION host=${hostType} reason=[${reason}] elapsedMs=${elapsedMs} err=${e.message}"
            )
            false
        } finally {
            reconnecting.set(false)
        }
    }

    /*fun flushInfo(logger: Logger){
        try {
            logger.commLog(this.hostType,"TRY FLUSH INFO ")

            outputStream = socket.getOutputStream()
            inputStream = socket.getInputStream()
            if (inputStream != null) {
                dataInputStream = DataInputStream(inputStream!!)
                if (outputStream != null) {
                    outputStream!!.flush()
                }
            }
        }catch (e:Exception){
            logger.commLog(this.hostType,"ERROR FLUSHING "+e.message)
        }
    }*/


    fun connectToHost(watcher: Watcher): Iso8583TransactionMetaData {
        var response= tryConnectToHost(watcher)
        try {
            if (response.errorCode != 0) {
                disconnectHost(watcher)
            }
        }catch (e:Exception){
                            handleException(e)

            response.errorMessage="logicalNetworkId [${logicalNetworkId}]  EXCEPTION ${e.message}"
           watcher.error( response.errorMessage)
           WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  ${hostType} "+  response.errorMessage)

        }
        try {
            if (response.errorCode != 0) {
                response = tryConnectToHost(watcher)
            }
        }catch (e:Exception){
                            handleException(e)

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  un connectToHost(watcher: Watcher): Iso8583TransactionMetaData ${e.message}")

            response.errorMessage="logicalNetworkId [${logicalNetworkId}]  EXCEPTION ${e.message}"
           watcher.error( response.errorMessage)
           WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  ${hostType} "+  response.errorMessage)
           isConnectedAndReady=false;

        }
        return response
    }


    fun configureSocket(s: Socket) {
        s.keepAlive = true          // TCP keep-alive del sistema operativo
        s.tcpNoDelay = true         // menor latencia en mensajes cortos
        //s.soLinger = false          // evita RST inesperados al cerrar
         s.setSoLinger(false, 0)   
        s.receiveBufferSize = 64 * 1024
        s.sendBufferSize = 64 * 1024 
     
    }
 
    fun tuneKeepAliveIfAvailable(s: Socket, idleSec: Int = 60, intvlSec: Int = 10, probes: Int = 6) {
        try {
            val sockets = Class.forName("jdk.net.Sockets")
            val ext = Class.forName("jdk.net.ExtendedSocketOptions")
            val soClass = Class.forName("java.net.SocketOption")

            val KEEPIDLE = ext.getField("TCP_KEEPIDLE").get(null)
            val KEEPINTV = ext.getField("TCP_KEEPINTERVAL").get(null)
            val KEEPCNT  = ext.getField("TCP_KEEPCOUNT").get(null)
            val setOpt   = sockets.getMethod("setOption", Socket::class.java, soClass, Any::class.java)

            setOpt.invoke(null, s, KEEPIDLE, idleSec)
            setOpt.invoke(null, s, KEEPINTV, intvlSec)
            setOpt.invoke(null, s, KEEPCNT,  probes)
        } catch (_: Throwable) {
            // No disponible en este JDK/OS  se ignora sin fallar
        } 
    }


    fun tryConnectToHost(watcher: Watcher): Iso8583TransactionMetaData {
        isReceivingMessage = false
        isSendingMessage = false
        val settings = AppSettings.getHostConfig(hostType)
        watcher.debug("logicalNetworkId [${logicalNetworkId}]  \n\n============= CONNECTING TO HOST INIT ============= \n\n")
        val response = Iso8583TransactionMetaData()
       maxConnectionTries=settings.maxConnectionTries
       //val hostSettings=AppSettings.getHostConfig(hostType)
     //  var response= TransactionResponse(isoRequest,null)
       watcher.debug(" logicalNetworkId [${logicalNetworkId}]  tryConnectToHost_settings_hostResponseHardcode  ${settings.hostResponseHardcode}" )
       watcher.debug("\n\n logicalNetworkId [${logicalNetworkId}]  maxConnectionTries [${maxConnectionTries}]\n\n")
       WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}]  ${hostType} BEGIN tryConnectToHost watcher maxConnectionTries [${maxConnectionTries}] ")

        try { 
  
            if (socket.isConnected) { 
                watcher.debug( "TRYING CLOSE SOCKET ")
                disconnectHost(watcher)
            }
        } catch (e: Exception) {
                handleException(e)

              val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}] err ${err}  tryConnectToHost(watcher: Watcher): Iso8583TransactionMetaData ${e.message}")

            watcher.error( "logicalNetworkId [${logicalNetworkId}]  EXCEPTION DISCONECTTING  " + e.message)
            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  ${hostType} EXCEPTION DISCONECTTING  " + e.message)

        }
         // Esperar un breve período para asegurarse de que los recursos se han limpiado (opcional)
         Thread.sleep(1000)

        try {
            watcher.debug( "logicalNetworkId [${logicalNetworkId}]  CONNECTING TO: ${settings.hostIp}:${settings.hostPort}  timeout:${settings.hostConnectionTimeOut} ")
            val socketAddress: SocketAddress = InetSocketAddress(settings.hostIp, settings.hostPort)
            WatcherGlobalLog.commRaw( "logicalNetworkId [${logicalNetworkId}]  ${hostType} CONNECTING TO: [${settings.hostIp}:${settings.hostPort}]  connectionTimeout: [${settings.hostConnectionTimeOut}] ")
     
            if (settings.enableTls) {
                watcher.debug( "logicalNetworkId [${logicalNetworkId}]  TLS IS ACTIVE PROTOCOL: ${settings.tlsProtocol}")
                val certFile = File(settings.hostCertPath)
                if (!certFile.exists()) {
                    watcher.error("logicalNetworkId [${logicalNetworkId}]  HOST_MANAGER: $hostType CERTIFICATE DOESNT EXISTS PATH: ${settings.hostCertPath}")
                } else {
                    val targetStream: InputStream = FileInputStream(certFile)
                    try {
                        val certificate: KeyStore = KeyStore.getInstance("JKS")
                        certificate.load(targetStream, settings.certPassword.toCharArray())
                        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                        tmf.init(certificate)
                        val trustAllCerts = arrayOf<TrustManager>(
                            object : X509TrustManager {
                                override fun getAcceptedIssuers(): Array<X509Certificate> {
                                    return arrayOf()
                                }

                                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
                                }

                                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
                                }
                            }
                        )
                        val trustContext = SSLContext.getInstance(settings.tlsProtocol)
                        trustContext.init(null, trustAllCerts, SecureRandom())
                        val sslSocketFactory = trustContext.socketFactory
                        socket = sslSocketFactory.createSocket()
                    } catch (e: Exception) {
                        handleException(e)

                          val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

                        AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"err [$err] logicalNetworkId [${logicalNetworkId}]  tryConnectToHost(watcher: Watcher): Iso8583TransactionMetaData ${e.message}")

                        WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  ${hostType} ERROR CONFIGURING TLS  ${e.message}")

                        watcher.error("logicalNetworkId [${logicalNetworkId}]  ERROR CONFIGURING TLS  ${e.message}")
                    }
                }
            }
 
            socket.soTimeout = settings.hostConnectionTimeOut
            socket.connect(socketAddress)
             socket.keepAlive = true           // Habilita TCP keepalive
             socket.tcpNoDelay = true          // Desactiva Nagle (mejor para tiempo real)
            // socket.soLinger = true            // Cierre inmediato del socket
            // socket.setSoLinger(true, 0)       // Esto lo fuerza a cerrar el socket inmediatamente
            configureSocket(socket)
            tuneKeepAliveIfAvailable(socket)  // si está soportado, baja los tiempos


            outputStream = socket.getOutputStream() 
            inputStream = socket.getInputStream()

           

            if (inputStream != null) {
                dataInputStream = DataInputStream(inputStream!!)
            }
            watcher.debug( "logicalNetworkId [${logicalNetworkId}]  HOST IS CONNECTED   ${AppSettings.getHostConfig(hostType).hostIp}:${AppSettings.getHostConfig(hostType).hostPort} ")
            val mustSendLogOn = AppSettings.getHostConfig(hostType).enableLogOns
            watcher.debug( "logicalNetworkId [${logicalNetworkId}]  mustSendLogOn ${mustSendLogOn}")

            if (AppSettings.getHostConfig(hostType).enableLogOns) {
              /*  watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  SENDING LOGON WAKEUP")
                try {
                   val result= checkPendingMessages(watcher)
                   watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  checkPendingMessages result [$result]")
                }catch (ex:Exception){
                                    handleException(ex)

                   val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

                    AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"ERROR ESPERANDO UN LOGON logicalNetworkId [${logicalNetworkId}]  err [${err}] tryConnectToHost(watcher: Watcher): Iso8583TransactionMetaData ${ex.message}")

                   watcher.error(  "logicalNetworkId [${logicalNetworkId}]  Processed pending EXCEPTION ${ex.message}")
                }
               watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  sendLogOnRequest")*/

                val sendResponse = sendLogOnRequest(watcher)
                if (sendResponse.errorCode != 0) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_WRITE_ERROR,"logicalNetworkId [${logicalNetworkId}]  ${hostType} sendLogOnRequest ERROR: ${ErrorCode.fromId(sendResponse.errorCode)} [${sendResponse.errorMessage}]")
                    WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  ${hostType} sendLogOnRequest ERROR: ${ErrorCode.fromId(sendResponse.errorCode)} [${sendResponse.errorMessage}]")
                   watcher.error(  "logicalNetworkId [${logicalNetworkId}]  sendLogOnRequest ERROR: ${ErrorCode.fromId(sendResponse.errorCode)} [${sendResponse.errorMessage}]")
                    response.errorMessage = sendResponse.errorMessage
                    response.errorCode = sendResponse.errorCode 
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOGON_ERROR,"logicalNetworkId [${logicalNetworkId}]  sendLogOnRequest ERROR: ${ErrorCode.fromId(sendResponse.errorCode)} [${sendResponse.errorMessage}]")

                    return response
                }

                   AlarmEmailService.sendAlarmEmail(AlarmCause.LOGON_OK," SE MANDO EXITOSAMENTE EL 0800 Y SE RECIBIO EL 0810 | DETAILS: [${sendResponse.getDetails()}]  ")

                response.iso8583MetaDataRequest = sendResponse
            }
            isConnectedAndReady = true
            startThreads(hostType, watcher)
            response.errorCode = 0
            response.errorMessage = "SUCCESS"
            isConnectedAndReady = true
            lastHostMessageAtMs = System.currentTimeMillis()   // P0: reloj fresco tras (re)conectar
            AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_OK,"logicalNetworkId [${logicalNetworkId}]  Se ha logrado establecer conexion con: \n ${getHostInfo()}\n <hr>${commOpenExceptionThreshold}")

           watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  isConnectedAndReady [$isConnectedAndReady]")
           WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  tryConnectToHost_isConnectedAndReady ${isConnectedAndReady} ")

        } 
        
       catch (e: Exception) {
                handleException(e)

            val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

            //AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]  err [${err}] tryConnectToHost(watcher: Watcher): Iso8583TransactionMetaData { ${e.message}")

            WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  EXCEPTION ${hostType} connectToHost | connectionTries [${connectionTries}] EXCEPTION " + e.message.toString()+"")

            commOpenExceptionDetails+= "logicalNetworkId [${logicalNetworkId}]  [${AlarmEmailService.getCurrentTimeCDMX()}]"+e.message.toString()+"<br>"

            
           if(e.message.toString().lowercase().contains("already connected")){
            WatcherGlobalLog.commError("... already connected -> NO se reutiliza el socket; se fuerza reconexion limpia")
            disconnectHost(watcher)            // teardown real (con P1b, deja socket=Socket() fresco)
            isConnectedAndReady = false
            response.errorCode = -1            // el while de tryToConnect reintenta con socket limpio
            response.errorMessage = "already connected -> clean reconnect forced"
            return response
            }else{
                isConnectedAndReady = false
                connectionTries++;
                if(connectionTries>=maxConnectionTries){
                    WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  EXCEPTION ${hostType} MAX CONNECTION TRIES REACHED => connectionTries [${connectionTries}]  ")
                    AlarmEmailService.sendAlarmEmail(AlarmCause.CONNECTION_ERROR," logicalNetworkId [${logicalNetworkId}]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $connectionTries INTENTOS \n ${getHostInfo()}\n <hr>${commOpenExceptionDetails}")
                    downBalancerAndRebootSystem()
                    connectionTries=0 
                    return response 
                }
            }
 
            watcher.error(  "logicalNetworkId [${logicalNetworkId}]  ${hostType} connectToHost  EXCEPTION " + e.message.toString())
            response.errorCode = -1
            response.errorMessage = e.message.toString()




            isConnectedAndReady=false;
        }

       watcher.debug(  "\n\n=============logicalNetworkId [${logicalNetworkId}]  CONNECTING TO HOST END ============= \n\n")
        return response
    }






    private fun buildFastSuccessEchoResponse(startTimeMs: Long): TransactionResponse {
        val zoneId = try {
            val tz = AppSettings.getHostConfig(hostType).zoneTime
            if (tz.isNotBlank()) ZoneId.of(tz) else ZoneId.systemDefault()
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
        val now = ZonedDateTime.now(zoneId)
        val transmissionDateTime = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"))
        val settlementDateValue = now.format(DateTimeFormatter.ofPattern("MMdd"))
        val randomStan = ThreadLocalRandom.current().nextInt(0, 1_000_000).toString().padStart(6, '0')
        val randomAuthId = ThreadLocalRandom.current().nextInt(0, 1_000_000).toString().padStart(6, '0')
        val nowMs = System.currentTimeMillis()

        val response = TransactionResponse()
        response.uuid = UUID.randomUUID().toString().replace("-", "")
        response.errorCode = ErrorCode.Approved.errorCodeInt
        response.errorCodeEnum = ErrorCode.Approved
        response.errorMessage = "logicalNetworkId [${logicalNetworkId}] SUCCESS RECEIVEING MESSAGE FROM ${hostType}"
        response.rrn = transmissionDateTime
        response.operationIdRequest = OperationId.CommunicationTest
        response.startTime = startTimeMs
        response.operationDuration = (nowMs - startTimeMs) / 1000.0f
        response.transactionResponse = TransactionDto().apply {
            transactionStatus = TransactionStatus.ReceivedInWebService
            paymentPlanType = PaymentPlanType.Undefined
            responseCodeAuthorizer = ErrorCode.Approved.responseCode
            merchantType = MerchantType.Comm
            transactionType = TransactionType.EchoTest
            pointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
            hostType = this@HostCommManager.hostType
            cardType = CardType.Undefined
            transmisionDateAndTime = transmissionDateTime
            systemTraceAuditNumber = randomStan
            settlementDate = settlementDateValue
            track2 = ""
            cvv = ""
            authorizationIdResponse = randomAuthId
            responseCode = ErrorCode.Approved.responseCode
            cardNumber = ""
            transactionCurrencyCode = "484"
            networkManagmentInformationCode = "301"
            flagRequestNewKey = "0"
            errorCode = ErrorCode.Approved.errorCodeInt
            errorMessage = response.errorMessage
        }
        return response
    }

    fun sendEchoTestRequest(): Iso8583MetaData {
        getHostWatcher().debug( "\n\n============= logicalNetworkId [${logicalNetworkId}]  sendEchoTestRequest INIT============= \n\n")

        val requestDto= DynamicISOFields.getEchoRequestMetaData(hostType)

        val responseDto= sendMessage(requestDto,getHostWatcher())

        getHostWatcher().debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendEchoTestRequest END============= \n\n")

        return responseDto


    }



    fun checkPendingMessages(watcher: Watcher): Boolean {
        var pendingMessages =false
       watcher.debug(  "logicalNetworkId [${logicalNetworkId}] checkPendingMessages") 
        val receiveMessageResponse = receiveMessage(true,700)
       watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  checkPendingMessages RECEIVING ERROR CODE: [${ErrorCode.fromId(receiveMessageResponse.errorCode)}] ")
        if (receiveMessageResponse.errorCode!=ErrorCode.EmptyResponce.errorCodeInt) {
            manageReceivedMessage(receiveMessageResponse)
           watcher.debug(  "logicalNetworkId [${logicalNetworkId}] checkPendingMessages: There are pending messages to process ._.")
            pendingMessages=true
        }else{
           watcher.debug(  "logicalNetworkId [${logicalNetworkId}]  checkPendingMessages:NO  pending messages to process ._. <3")
        }
        isReadyToEchos=true
       watcher.debug(  "logicalNetworkId [${logicalNetworkId}] checkPendingMessages: isReadyToEchos [$isReadyToEchos]")
        return pendingMessages
    }

    fun sendLogOnRequest(watcher: Watcher): Iso8583MetaData {
       watcher.debug(  "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendLogOnRequest INIT ============= \n\n")
        
      AlarmEmailService.sendAlarmEmail(AlarmCause.LOGON_REQUEST,"SE ESTA INTENTANDO ENVIAR UN LOGON REQUEST ")
  
       val requestDto = DynamicISOFields.getLogOnRequestMetaData(hostType)
        val responseDto = sendIsoAndWaitForIso(requestDto) 


       watcher.debug(  "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendLogOnRequest END ============= \n\n")
        return responseDto
    }


    fun sendEchoTestResponse(systemTraceNumber:String, watcher: Watcher): Iso8583MetaData {
        val responseDto: Iso8583MetaData
       watcher.debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendEchoTestResponse INIT============= \n\n")

        val requestDto= DynamicISOFields.getEchoTestResponseMetaData(hostType,systemTraceNumber)

        responseDto= sendMessage(requestDto,watcher)

       watcher.debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendEchoTestResponse END============= \n\n")

        return responseDto


    }





    fun sendLogOnResponse(systemTraceNumber:String, watcher: Watcher): Iso8583MetaData {
        val responseDto: Iso8583MetaData
       watcher.debug( "\n\n============= logicalNetworkId [${logicalNetworkId}]  sendLogOnResponse INIT============= \n\n")

        val requestDto= DynamicISOFields.getLogOnResponseMetaData(hostType,systemTraceNumber)

        responseDto= sendMessage(requestDto,watcher)

       watcher.debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendLogOnResponse END============= \n\n")

        return responseDto


    }
    fun sendIsoAndCloseConnection(isoAscii: String, hostType:HostType, watcher: Watcher): TransactionResponse {
       watcher.debug(" logicalNetworkId [${logicalNetworkId}] sendIsoAndCloseConnection ")

        init(hostType)
       return sendIsoAndWaitForResponse(isoAscii)
    }

    fun getHostWatcher():Watcher{
        if(watcher!=null){
            return watcher!! 
        }  
        val requestEntity=RequestEntity()
        requestEntity.operationIdRequest=OperationId.HostOperation
        requestEntity.hostType=hostType
        watcher= WatcherContext.initOperation(requestEntity)
        return watcher!!
    }
    fun sendIsoAndWaitForResponse(isoAscii: String): TransactionResponse {
        val isoRequest = IsoHostParser.parseIso(isoAscii, hostType, getHostWatcher())
        return sendIsoAndWaitForResponse(isoRequest)
    } 
    fun sendIsoAndCloseConnection(isoRequest: Iso8583MetaData, hostType:HostType, watcher: Watcher): TransactionResponse {
       watcher.debug(" logicalNetworkId [${logicalNetworkId}]  IN sendIsoAndCloseConnection ")
        init(hostType)
        return sendIsoAndWaitForResponse(isoRequest)
    }


    fun sendIsoAndWaitForResponse(isoRequest: Iso8583MetaData): TransactionResponse {
        var response = TransactionResponse(isoRequest, null)
        val settings = AppSettings.getHostConfig(hostType)
    
        val moreInfo="logicalNetworkId [${logicalNetworkId}]  RRN: "+isoRequest.getRrn()+" SERIAL:"+isoRequest.getTerminalSerialNumber()

  

        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  sendIsoAndWaitForResponse STARTED")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  sendIsoAndWaitForResponse STARTED")
        if(isoRequest.transactionType==TransactionType.Reverse){
            
            getHostWatcher().saveIsoRaw(isoRequest)

        } 
        // Verificar si la respuesta está forzada (Hardcoded Response)
        if (AppSettings.getHostConfig(hostType).hostResponseHardcode) {
            val errorCode = AppSettings.getHostConfig(hostType).getErrorCodeForTransaction(isoRequest.transactionType)
            isoRequest.errorCode = errorCode.errorCodeInt
            isoRequest.errorMessage = "logicalNetworkId [${logicalNetworkId}]  $hostType ${errorCode.errorCodeMessage}"
            response.errorCode = isoRequest.errorCode
            response.errorMessage = isoRequest.errorMessage
    
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  Hardcoded response applied: isoRequest.errorCode ${isoRequest.errorCode } transactionType: ${isoRequest.transactionType} , ${isoRequest.errorMessage}")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  Hardcoded response applied: isoRequest.errorCode ${isoRequest.errorCode } transactionType: ${isoRequest.transactionType}  , ${isoRequest.errorMessage}")
            response.transactionResponse=TransactionDto()
            response.transactionResponse!!.responseCode=errorCode.responseCode
            
            return response
        }

       
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  sendMessage CONNECTED: [${socket.isConnected}]")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  sendMessage CONNECTED: [${socket.isConnected}]")
    
        // Verificar si el host está activo
        if (!AppSettings.getHostConfig(hostType).hostIsActive()) {
            isoRequest.errorCode = ErrorCode.HostIsInactive.errorCodeInt
            isoRequest.errorMessage = "logicalNetworkId [${logicalNetworkId}]  $hostType ${ErrorCode.HostIsInactive.errorCodeMessage}"
    
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] Host is inactive. Terminating request.")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] Host is inactive. Terminating request.")
    
            return response
        }
    
        // Validaciones de errores en ISO request
        if (isoRequest.errorCode != 0) {
            response.errorCode = isoRequest.errorCode
            response.errorMessage = isoRequest.errorMessage
            getHostWatcher().error("logicalNetworkId [${logicalNetworkId}]  ISO REQUEST HAS ERRORS: ${isoRequest.errorCode} ${isoRequest.errorMessage}")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] ISO REQUEST HAS ERRORS: ${isoRequest.errorCode} ${isoRequest.errorMessage}")
            return response
        } 
    
        if (isoRequest.transactionData == null || (isoRequest.transactionData!!.systemTraceAuditNumber.isEmpty())) {
            response.errorCode = ErrorCode.SystemTraceNumberIsNecesary.errorCodeInt
            response.errorMessage = ErrorCode.SystemTraceNumberIsNecesary.errorCodeMessage
            getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] ISO REQUEST HAS ERRORS TRANSACTION DATA IS NULL: ${response.errorCode} ${response.errorMessage}")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] ISO REQUEST HAS ERRORS TRANSACTION DATA IS NULL: ${response.errorCode} ${response.errorMessage}")
            return response
        }
    
        isoRequest.transactionData!!.hostType = hostType
        val systemTraceNumber = isoRequest.transactionData!!.systemTraceAuditNumber
        var sendResponse = Iso8583MetaData()
    
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  settings.mustSendTxnToHost: ${settings.mustSendTxnToHost}")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] settings.mustSendTxnToHost: ${settings.mustSendTxnToHost}")
        
        // Enviar mensaje ISO si está habilitado en la configuración
        if (settings.mustSendTxnToHost) {
            sendResponse = sendMessage(isoRequest, getHostWatcher())
            if (sendResponse.errorCode != 0) {
                response = TransactionResponse(isoRequest, null) 
                response.errorCode = sendResponse.errorCode
                response.errorMessage = "logicalNetworkId [${logicalNetworkId}]  ERROR_MESSAGE 03 sendIsoAndWaitForResponse: ${sendResponse.errorMessage}"
    
                getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] sendResponse ERRORS: ${response.errorCode} ${response.errorMessage}")
                WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  sendResponse ERRORS: ${response.errorCode} ${response.errorMessage}")
    
                return response
            }
        }
    
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] settings_clientWaitAnswerTimeOut: ${settings.clientWaitAnswerTimeOut}")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}]  settings_clientWaitAnswerTimeOut: ${settings.clientWaitAnswerTimeOut}")
    
        // Manejo de respuesta de host con timeout
        if (settings.keepConnectionAlive || !settings.mustSendTxnToHost) {
            val waitStartTimeMs = System.currentTimeMillis()
            val isEchoTestRequest =
                isoRequest.transactionData?.transactionType == TransactionType.EchoTest ||
                isoRequest.transactionType == TransactionType.EchoTest
            val echoMaxWaitMs = AppSettings.getInstance().echoServiceMaxWaitSeconds * 1000L
            val waitTimeoutMs = if (isEchoTestRequest && echoMaxWaitMs > 0L) {
                minOf(settings.clientWaitAnswerTimeOut, echoMaxWaitMs)
            } else {
                settings.clientWaitAnswerTimeOut
            }

            var seconds = 0L
            while (seconds < waitTimeoutMs) {
                Thread.sleep(settings.clientWaitDeltaSeconds)

                    val isoResponse = checkReceivedMessage(systemTraceNumber, isoRequest.getRrn(), getHostWatcher())
                if (isoResponse != null) {
                    response.errorCode = isoResponse.errorCode
                    response.errorMessage = isoResponse.errorMessage
                    response = TransactionResponse(isoRequest, isoResponse)
    
                    getHostWatcher().saveIsoRaw(isoResponse)
                    WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] ISO Response received successfully: errorCode=${isoResponse.errorCode}, message=${isoResponse.errorMessage}")
    
                    if (sendResponse.errorCode != 0) {
                        response.errorCode = sendResponse.errorCode
                        response.errorMessage = "logicalNetworkId [${logicalNetworkId}] ERROR_MESSAGE 01 sendIsoAndWaitForResponse: ${sendResponse.errorMessage}"
                    }
                    commTimeoutExceptionCounter.updateAndGet { if (it > 0) it - 1 else 0 }
                    connectionsOkCounter++
                    if(connectionsOkCounter>=3){
                        upBalancer("EL CODIGO CUANDO DETECTA MAYOR O IGUAL DE connectionsOkCounter>=[${connectionsOkCounter}] SE EJECUTA ESTA LINEA NO SIGNIFICA QUE SE VA A DAR DE ALTA LA INSTANCIA, SOLO SE EJECUTARA EL SHELL SCRIPT ")
                    } 
                    
                    return response
                }
                seconds += settings.clientWaitDeltaSeconds
            }

            if (isEchoTestRequest && echoMaxWaitMs > 0L) {
                WatcherGlobalLog.commRaw(
                    "logicalNetworkId [${logicalNetworkId}] ECHO_FAST_OK_FALLBACK host=${hostType} stan=[${systemTraceNumber}] elapsedMs=${seconds} maxWaitMs=${echoMaxWaitMs}"
                )
                return buildFastSuccessEchoResponse(waitStartTimeMs)
            }

            if(isoRequest.transactionType!=TransactionType.Reverse){
                 clientTimeoutsCounter.incrementAndGet()
             }  

             val now = System.currentTimeMillis()
            val millisSinceLastTimeout = now - lastTimeoutTimestamp

           

            // Si pasaron más de 7 minutos desde el último timeout, reiniciar contador

             if(isoRequest.transactionType!=TransactionType.Reverse){
                if (millisSinceLastTimeout > 7 * 60 * 1000 && clientTimeoutsCounter.getAndSet(0) > 0) {
                      lastTimeoutTimestamp = now

                    WatcherGlobalLog.commError("Reiniciando clientTimeoutsCounter por inactividad de más de 17 minutos")
                    AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_WARN, "Reiniciando clientTimeoutsCounter por inactividad de más de 17 minutos FAVOR DE ESTAR AL PENDIENTE DE QUE NO HAYA ERRORES")
                }  
            
        
            }


              val currentClientTimeouts = clientTimeoutsCounter.get()
              AlarmEmailService.sendAlarmEmail(AlarmCause.CLIENT_TIMEOUT, "logicalNetworkId [${logicalNetworkId}]  clientTimeoutsCounter [${currentClientTimeouts}] commReadExceptionThreshold [${commReadExceptionThreshold-2}]  [${moreInfo}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [milisegundos]   HACIENDO UN ${isoRequest.getDataTransactionType()}  \n ${getHostInfo()}\n")
              if (currentClientTimeouts >= commReadExceptionThreshold-2) {
                        downBalancer("clientTimeoutsCounter [${currentClientTimeouts}] commReadExceptionThreshold [${commReadExceptionThreshold-2}]")
              }  
        
            if (commTimeoutExceptionCounter.get() >= commReadExceptionThreshold+1) {
                commTimeoutExceptionCounter.set(0)
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  ERROR TIMEOUT EN LOOP ${seconds} ")
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [segundos]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD HACIENDO UN ${isoRequest.getDataTransactionType()} PERO NO SE HA LOGRADO DESPUES DE $commReadExceptionCounter INTENTOS \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                AlarmEmailService.sendAlarmEmail(AlarmCause.CLIENT_TIMEOUT, "logicalNetworkId [${logicalNetworkId}]  logicalNetworkId [${logicalNetworkId}]  [${moreInfo}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [milisegundos]   HACIENDO UN ${isoRequest.getDataTransactionType()}  \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}]  REBOOT_DAEMON")
                commReadExceptionDetails = "" 
                  downBalancerAndRebootSystem()

                response.errorCode = ErrorCode.ClientTimeOut.errorCodeInt
                response.errorMessage = ErrorCode.ClientTimeOut.errorCodeMessage
                isoRequest.setResponseCode(ErrorCode.ClientTimeOut)
                response = TransactionResponse(isoRequest, null)
             
                return response
            }
            if (commTimeoutExceptionCounter.get() >= commReadExceptionThreshold) {
                commTimeoutExceptionCounter.set(0)

               // disconnectHost(getHostWatcher())
               // tryToConnect();
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] ERROR TIMEOUT EN LOOP ${seconds} ")
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [segundos]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD HACIENDO UN ${isoRequest.getDataTransactionType()} PERO NO SE HA LOGRADO DESPUES DE $commReadExceptionCounter INTENTOS \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                AlarmEmailService.sendAlarmEmail(AlarmCause.CLIENT_TIMEOUT, "logicalNetworkId [${logicalNetworkId}]  [${moreInfo}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [milisegundos]   HACIENDO UN ${isoRequest.getDataTransactionType()}  \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] REBOOT_DAEMON")
                commReadExceptionDetails = "" 
              
              //  rebootSystem()
                 
                }else{
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] ERROR TIMEOUT EN LOOP ${seconds} ")
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [segundos]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD HACIENDO UN ${isoRequest.getDataTransactionType()} PERO NO SE HA LOGRADO DESPUES DE $commReadExceptionCounter INTENTOS \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                    AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_WARN, "logicalNetworkId [${logicalNetworkId}]  [${moreInfo}] ERROR TIMEOUT EN LOOP SEGUNDOS QUE TARDO EN CONTESTAR: ${seconds} [milisegundos]  HACIENDO UN ${isoRequest.getDataTransactionType()}  \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                }
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}]  sendIsoAndWaitForResponse: TIME_OUT ERROR | ELAPSED= [$seconds] | VALUE=[${settings.clientWaitAnswerTimeOut}]")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] sendIsoAndWaitForResponse: TIME_OUT ERROR | ELAPSED= [$seconds] | VALUE=[${settings.clientWaitAnswerTimeOut}]")
    
            response.errorCode = ErrorCode.ClientTimeOut.errorCodeInt
            response.errorMessage = ErrorCode.ClientTimeOut.errorCodeMessage
            isoRequest.setResponseCode(ErrorCode.ClientTimeOut)
            response = TransactionResponse(isoRequest, null)
         

            removeRequestMessage(isoRequest, null, getHostWatcher())
        }
    
        if (!settings.keepConnectionAlive) {
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] receiveMessage keepConnectionAlive: ${settings.keepConnectionAlive}")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] receiveMessage keepConnectionAlive: ${settings.keepConnectionAlive}")
    
            val receiveMessageResponse = receiveMessage()
            response = TransactionResponse(isoRequest, receiveMessageResponse)
    
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] MESSAGE RECEIVED SUCCESSFULLY")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] MESSAGE RECEIVED SUCCESSFULLY")
    
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] HOST DISCONNECTING")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] HOST DISCONNECTING")
    
            disconnectHost(getHostWatcher())
    
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] HOST DISCONNECTED")
            WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] HOST DISCONNECTED")
        }
    
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] sendIsoAndWaitForResponse COMPLETED")
        WatcherGlobalLog.reverseDebug("logicalNetworkId [${logicalNetworkId}] sendIsoAndWaitForResponse COMPLETED")

        return response
    }
    
    fun sendIsoAndWaitForIso(isoRequest: Iso8583MetaData): Iso8583MetaData {
        var response= Iso8583MetaData()
        val settings=AppSettings.getHostConfig(hostType)
        getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] sendIsoAndWaitForIso")

        if(isoRequest.errorCode!=0) {
            response.errorCode=isoRequest.errorCode
            response.errorMessage=isoRequest.errorMessage
            getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] ISO REQUEST HAS ERRORS: "+isoRequest.errorCode+" "+isoRequest.errorMessage)

            return response
        }
        if(isoRequest.transactionData==null||isoRequest.transactionData!=null&&isoRequest.transactionData!!.systemTraceAuditNumber.equals("")) {
            response.errorCode=ErrorCode.SystemTraceNumberIsNecesary.errorCodeInt
            response.errorMessage=ErrorCode.SystemTraceNumberIsNecesary.errorCodeMessage
            getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] ISO REQUEST HAS ERRORS TRANSACTION DATA IS NULL : "+response.errorCode+" "+response.errorMessage)

            return response
        }
        isoRequest.transactionData!!.hostType=hostType

        val systemTraceNumber=isoRequest.transactionData!!.systemTraceAuditNumber
        var sendResponse= Iso8583MetaData()
        getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] settings.mustSendTxnToHost : " + settings.mustSendTxnToHost)

        if(settings.mustSendTxnToHost) {
            sendResponse = sendMessage(isoRequest, getHostWatcher())
            if (sendResponse.errorCode != 0) {
                response.errorCode = sendResponse.errorCode
                response.errorMessage =
                    "logicalNetworkId [${logicalNetworkId}] ERROR_MESSAGE 02 sendIsoAndWaitForIso: " + sendResponse.errorMessage //+"\n\n LOGS:\n"+logger.logContent
                getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] sendResponse ERRORS : " + response.errorCode + " " + response.errorMessage)
                return response
            }
        }
        getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] receiveMessage")

        val receive=receiveMessage()

        response.errorCode= receive.errorCode
        response.errorMessage= receive.errorMessage

        return response


    }

    fun getHostInfo():String{

        val hostName= AppSettings.getHostConfig(hostType).hostName
        val hostIp= AppSettings.getHostConfig(hostType).hostIp
        val hostPort=  AppSettings.getHostConfig(hostType).hostPort
        val timeOut=  AppSettings.getHostConfig(hostType).hostConnectionTimeOut

        return "hostName:[$hostName], hostIp:[${hostIp}], hostPort:[$hostPort], timeOut:[$timeOut]"
    }

    private fun List<Byte>.toByteArrayCustom(): ByteArray {
        return ByteArray(this.size) { this[it] }
    }
    

   
   
   /*  fun parseMessages(receivedData:ByteArray){
        var isoAscii = ""
        var cleanedMessageAscii=""
        var messageCounter=0
        val guid: String = UUID.randomUUID().toString()
        try{
         
            // Split por ETX
            val messages = StringUtils.byteArraySplit(receivedData,0x03.toByte()) 
            getHostWatcher().debug("\n\n============= logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages INIT ============= \n\n")
            WatcherGlobalLog.commRaw("\n\n=============logicalNetworkId [${logicalNetworkId}]  [${guid}] parseMessages INIT ============= \n\n")
            getHostWatcher().debug("=============logicalNetworkId [${logicalNetworkId}]  [${guid}] messages.size  [${messages.size}] =============")
            WatcherGlobalLog.commRaw("=============logicalNetworkId [${logicalNetworkId}]  [${guid}] messages.size  [${messages.size}] =============")
            if(messages.size<=1) return 
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages CLEANED_receivedData [${ StringUtils.asciiFromByteArray(receivedData)}]")
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages CLEANED_receivedData [${ StringUtils.asciiFromByteArray(receivedData)}]")
            for (rawMessage in messages) {
                if (rawMessage.isEmpty()) continue
                if(messageCounter==0) continue
    
                val cleanedMessageBytes = StringUtils.cleanAsciiBytes(rawMessage)
                 cleanedMessageAscii = StringUtils.asciiFromByteArray(cleanedMessageBytes)
    
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages BUFFER[${messageCounter}] CLEANED [$cleanedMessageAscii]")
                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages BUFFER CLEANED [${messageCounter}] [$cleanedMessageAscii]")
    
                val response = IsoHostParser.parseIso(
                    cleanedMessageBytes,
                    hostType,
                    getHostWatcher()
                )

                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] BUFFER_PARSED [${messageCounter}] ISO_errorCode [${response.errorCode}]  ISO_errorMessage [${response.errorMessage}]")

                val responseManageM = manageReceivedMessage(guid,response)
                messageCounter++
            }

        }
       catch(ex:Exception){
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages exception [${ex.message}]")
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages BUFFER[${messageCounter}] CLEANED [$cleanedMessageAscii]")
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] parseMessages BUFFER CLEANED [${messageCounter}] [$cleanedMessageAscii]")

       }
        WatcherGlobalLog.commRaw("\n\n=============logicalNetworkId [${logicalNetworkId}]  [${guid}] parseMessages FINISH ============= \n\n")

     
    }
    */

     fun splitByISOWithPrefix(data: ByteArray): List<ByteArray> {
            val ascii = String(data, Charsets.US_ASCII)
            val regex = Regex("ISO")
            val matches = regex.findAll(ascii).map { it.range.first }.toList()

            if (matches.isEmpty()) return emptyList()

            val result = mutableListOf<ByteArray>()
            for (i in matches.indices) {
                val start = matches[i]
                val end = if (i + 1 < matches.size) matches[i + 1] else ascii.length
                val chunk = ascii.substring(start, end)
                result.add(chunk.toByteArray(Charsets.US_ASCII))
            }
            return result
        }
        fun parseMessages(receivedData: ByteArray) {
            val guid = UUID.randomUUID().toString()
            WatcherGlobalLog.commRaw("parseMessages[$guid] parseMessages init")

            try{
                val messages = splitByISOWithPrefix(receivedData)

                 getHostWatcher().debug("parseMessages [$guid] - Total messages: ${messages.size}")
                WatcherGlobalLog.commRaw("parseMessages[$guid] RAW_DATA: [${String(receivedData, Charsets.US_ASCII)}]")
                for ((index, rawMessage) in messages.withIndex()) {
                        if (rawMessage.isEmpty()) continue

                        val ascii = String(rawMessage, Charsets.US_ASCII)
                         getHostWatcher().debug("parseMessages[$guid] BUFFER[$index] CLEANED: [$ascii]")
                        WatcherGlobalLog.commRaw("parseMessages[$guid] BUFFER[$index] CLEANED: [$ascii]")

                        val response = IsoHostParser.parseIso(rawMessage, hostType,  getHostWatcher())
                        WatcherGlobalLog.commRaw("BUFFER_PARSED parseMessages[$guid] ISO_errorCode [${response.errorCode}] ISO_errorMessage [${response.errorMessage}]")

                        manageReceivedMessage(guid, response)
                    }

            }catch(ex:Exception){

                    WatcherGlobalLog.commRaw("parseMessages [$guid] EX [${ex.message}]")

            }
        
            WatcherGlobalLog.commRaw("parseMessages FINISH [$guid]")
        }


    fun receiveMessages(noDisconnect: Boolean = false, readingTimeout: Int = -1): MutableList<Iso8583MetaData> {
        val responses = mutableListOf<Iso8583MetaData>()
        val guid: String = UUID.randomUUID().toString()
    
        getHostWatcher().debug("\n\n=============logicalNetworkId [${logicalNetworkId}]  [${guid}] receiveMessages INIT ============= \n\n")
        var isoAscii = ""
    
        val originalTimeOut = socket.soTimeout
        if (readingTimeout > 0) {
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] originalTimeOut [$originalTimeOut] => readingTimeout [$readingTimeout]")
            socket.soTimeout = readingTimeout
        }
    
        try {
            maxBuferResponseSize=AppSettings.getHostConfig(hostType).maxBuferResponseSize
            val messageByteResponse = ByteArray(maxBuferResponseSize)
            val bytesRead = synchronized(dataInputStream!!) { dataInputStream!!.read(messageByteResponse) }
    

            
            getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] receiveMessages bytesRead [$bytesRead] noDisconnect [$noDisconnect]")
    
            if (bytesRead <= 0) {
                if (!noDisconnect && bytesRead < 0) {
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] START DISCONNECT ")
                    disconnectHost(getHostWatcher())
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] END DISCONNECT ")
                }
                if (readingTimeout > 0) socket.soTimeout = originalTimeOut
                return responses
            }
    
            val receivedData = messageByteResponse.copyOf(bytesRead) 
            isoAscii = StringUtils.asciiFromByteArray(receivedData)
    
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] RECEIVED ASCII : $isoAscii")
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] RECEIVED HEX : ${StringUtils.encodeHexString(receivedData)}")
    
            // Split por ETX
            val messages = StringUtils.byteArraySplit(receivedData,0x03.toByte()) 
            var messageCounter=0
            for (rawMessage in messages) {
                if (rawMessage.isEmpty()) continue

                messageCounter++
                val cleanedMessageBytes = StringUtils.cleanAsciiBytes(rawMessage)
                val cleanedMessageAscii = StringUtils.asciiFromByteArray(cleanedMessageBytes)
    
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] [${guid}] BUFFER[${messageCounter}] CLEANED [$cleanedMessageAscii]")
                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] BUFFER CLEANED [${messageCounter}] [$cleanedMessageAscii]")
    
                val response = IsoHostParser.parseIso(
                    cleanedMessageBytes,
                    hostType,
                    getHostWatcher()
                )
                if(response.isoAscii!=""){
                     response.isoAscii=cleanedMessageAscii
                }
               

                responses.add(response)
            }
        } catch (e: Exception) {
                handleException(e)

            val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

            getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] [${guid}] receiveMessages EXCEPTION : ${e.message}")
            WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] [${guid}] receiveMessages EXCEPTION : ${e.message}")
            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR, "RECEIVE_MESSAGE logicalNetworkId [${logicalNetworkId}]  err [${err}] [${guid}] receiveMessages Exception: ${e.message}")
            if (readingTimeout > 0) socket.soTimeout = originalTimeOut
            return responses
        }
    
        if (readingTimeout > 0) socket.soTimeout = originalTimeOut
        getHostWatcher().debug("\n\n=============logicalNetworkId [${logicalNetworkId}]  receiveMessages END ============= \n\n")
        WatcherGlobalLog.commRaw("\n\n=============logicalNetworkId [${logicalNetworkId}]  receiveMessages END ============= \n\n")
        return responses
    }
    
    
    fun receiveMessage(noDisconnect:Boolean=false, readingTimeout:Int=-1): Iso8583MetaData {
        getHostWatcher().debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  receiveMessage INIT ============= \n\n")
       
        //receiveLock.lock()
        
        var response= Iso8583MetaData()
        var isoAscii=""
        var bytesRead=-1
        // Obtener el socket asociado
        val originalTimeOut=   socket.soTimeout
        if(readingTimeout>0){

            getHostWatcher().debug(  "originalTimeOutlogicalNetworkId [${logicalNetworkId}]  [$originalTimeOut] => readingTimeout [$readingTimeout]")
            socket.soTimeout = readingTimeout

        }
        try { 
           maxBuferResponseSize=AppSettings.getHostConfig(hostType).maxBuferResponseSize

            val messageByteResponse = ByteArray(maxBuferResponseSize)
            getHostWatcher().debug(  "logicalNetworkId [${logicalNetworkId}] try To receive Message ._.")
            WatcherGlobalLog.commRaw(  "logicalNetworkId [${logicalNetworkId}] try To receive Message ._.")

           /*  synchronized(dataInputStream!!) {
                bytesRead = dataInputStream!!.read(messageByteResponse)
            }
            synchronized(dataInputStream!!) {
             bytesRead = IsoFraming.readAvailableFrame(dataInputStream!!, messageByteResponse)
                }*/

                 synchronized(dataInputStream!!) {
                val carry = frameCarryOver
                if (carry.isNotEmpty()) System.arraycopy(carry, 0, messageByteResponse, 0, carry.size)
                bytesRead = IsoFraming.readAvailableFrame(dataInputStream!!, messageByteResponse, carry.size)
                // Guardar el remanente (trama partida) para la próxima lectura. Solo si se consumió
                // al menos una trama completa (consumed>0): así es una trama parcial real, no basura
                // de desync (esa NO se arrastra -> la cola no crece sin fin).
                frameCarryOver = if (bytesRead > 0) {
                    val consumed = IsoFraming.extractFrames(messageByteResponse, bytesRead).consumed
                    if (consumed in 1 until bytesRead)
                        messageByteResponse.copyOfRange(consumed, bytesRead)
                    else ByteArray(0)
                } else ByteArray(0)
            }

            
            
             getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] receiveMessage bytesRead [$bytesRead] noDisconnect [$noDisconnect]")
 
            if(bytesRead<=0&&!noDisconnect){
                //println("receiveMessage bytesRead $bytesRead")
                if(bytesRead<0){
                    getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] START DISCONNECT ")
                    disconnectHost(getHostWatcher())
                    getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] END DISCONNECT ")
                }
                // disconnectHost()
                response.errorCode= ErrorCode.EmptyResponce.errorCodeInt
                response.errorMessage= ErrorCode.EmptyResponce.errorCodeMessage
                response.setResponseCode(ErrorCode.EmptyResponce)
                if(readingTimeout>0)
                    socket.soTimeout = originalTimeOut

                return response

            }
            if(bytesRead<=0&&noDisconnect) {
                response.errorCode= ErrorCode.EmptyResponce.errorCodeInt
                response.errorMessage= ErrorCode.EmptyResponce.errorCodeMessage
                if(readingTimeout>0)
                    socket.soTimeout = originalTimeOut
                if (bytesRead < 0) {
                    WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] RECEIVE_EOF (bytesRead=$bytesRead) -> intentando reconexion controlada")
                    reconnect("RECEIVE_EOF", getHostWatcher())
                }
                return response
            }
            try {
                
                //WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] RECEIVED_HEX : ${StringUtils.encodeHexString(messageByteResponse)}")
               WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] RECEIVED_HEX : ${StringUtils.encodeHexString(messageByteResponse.copyOf(bytesRead))}")
            } catch (e: java.lang.Exception) {
                                 handleException(e)

                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] RECEIVED_EXCEPTION : ${e.message}")


            }
            try {
              /* *  isoAscii= StringUtils.asciiFromByteArray(
                    messageByteResponse
                )*/

                isoAscii= StringUtils.asciiFromByteArray(
                    messageByteResponse.copyOf(bytesRead)
                )

               /*  CoroutineScope(Dispatchers.IO).launch {
                    parseMessages(messageByteResponse)
                } */
 
                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] RECEIVED_ASCII : ${isoAscii}")
              //  WatcherGlobalLog.commRaw("RECEIVED_HEX : ${StringUtils.encodeHexString(messageByteResponse)}")
                getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] BUFFER RECEIVED [${isoAscii}]")
                /*println(
                    "BUFFER RECEIVED ${isoAscii }"
                )*/
            } catch (e: java.lang.Exception) {
                handleException(e)

               val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

                AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"RECEIVE MESSAGE logicalNetworkId [${err}] [${logicalNetworkId}] logicalNetworkId [${logicalNetworkId}]  receiveMessage(noDisconnect:Boolean=false, readingTimeout:Int=-1):   ${e.message}")
                isReceivingMessage =false

                isConnectedAndReady=false;
                getHostWatcher().error(   "logicalNetworkId [${logicalNetworkId}] receiveMessage ex: ${e.message}  ")
                WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] receiveMessage ex: ${e.message} ")
                disconnectHost( getHostWatcher())
                tryToConnect();
                 response.errorCode= ErrorCode.HostError.errorCodeInt
                response.errorMessage=    "logicalNetworkId [${logicalNetworkId}] receiveMessage ex: ${e.message}  "
  
                return response

 
            }
           


            /* println(
                 "Client messageByte.size [${messageByteResponse.size}] bytesRead $bytesRead   "
             )*/
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] receiveMessage messageByte.size [${messageByteResponse.size}] bytesRead [$bytesRead]   ")
           /*  var responseMessageLen = 0
            val lenBytes = ByteArray(2)
            for (i in 0..1) {
                lenBytes[i] = messageByteResponse[i]
            }
            val hexStringLen = "0x" + StringUtils.encodeHexString(lenBytes).padStart(4, '0')

            // println("responseMessageLen [${hexStringLen}]")
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] receiveMessage responseMessageLen [${hexStringLen}]")

            try {
                responseMessageLen = Integer.decode(hexStringLen) + 1
            } catch (e: Exception) {
                  handleException(e)

                WatcherGlobalLog.commError(   "logicalNetworkId [${logicalNetworkId}] receiveMessage responseMessageLen EXCEPTION ${e.message.toString()}")

                // println("EXCEPTION ${e.message.toString()}")
                getHostWatcher().error(   "logicalNetworkId [${logicalNetworkId}] receiveMessage responseMessageLen EXCEPTION ${e.message.toString()}")
            }
 
            //  println("responseMessageLen [${responseMessageLen}]")
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] receiveMessage responseMessageLen [${responseMessageLen}]")

            val realMessageResponse = ByteArray(responseMessageLen)
            var indexReal = 0
              maxBuferResponseSize=AppSettings.getHostConfig(hostType).maxBuferResponseSize
            if (responseMessageLen > maxBuferResponseSize) {
                responseMessageLen = maxBuferResponseSize
            } 

            for (i in 2..responseMessageLen) {
                realMessageResponse[indexReal] = messageByteResponse[i]
                indexReal++
            }
            getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] BUFFER CLEANED [${ StringUtils.asciiFromByteArray( realMessageResponse )}]")


            response= IsoHostParser.parseIso(realMessageResponse,hostType,getHostWatcher())*/

                            // Separar el buffer en mensajes por prefijo de longitud (no por el texto "ISO").
                val frameResult = IsoFraming.extractFrames(messageByteResponse, bytesRead)

                // Instrumentación: si quedó un remanente no reconocido, capturar su HEX (evidencia segura en prod).
                if (frameResult.hadTrailingGarbage) {
                    val garbage = messageByteResponse.copyOfRange(frameResult.consumed, bytesRead)
                    WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] receiveMessage REMANENTE_NO_RECONOCIDO HEX: ${StringUtils.encodeHexString(garbage)}")
                }

                if (frameResult.frames.isEmpty()) {
                    getHostWatcher().error("logicalNetworkId [${logicalNetworkId}] receiveMessage: sin frames válidos en el buffer")
                    response.errorCode = ErrorCode.HostResponseInvalidFormat.errorCodeInt
                    response.errorMessage = ErrorCode.HostResponseInvalidFormat.errorCodeMessage
                    response.setResponseCode(ErrorCode.HostResponseInvalidFormat)
                    if (readingTimeout > 0) socket.soTimeout = originalTimeOut
                    return response

                     WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] receiveMessage sin frames válidos en el buffer")
                }

                // Primer mensaje: es la respuesta que se devuelve al llamador
                response = IsoHostParser.parseIso(frameResult.frames[0], hostType, getHostWatcher())
                getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] parseIso FINISH RESULTS=> [${ErrorCode.fromId(response.errorCode)}(${response.errorCode})] [${response.errorMessage}]")

                // Mensajes adicionales del mismo read: procesar y gestionar en línea.
                for (i in 1 until frameResult.frames.size) {
                    val extra = IsoHostParser.parseIso(frameResult.frames[i], hostType, getHostWatcher())
                    getHostWatcher().debug("logicalNetworkId [${logicalNetworkId}] mensaje adicional [$i] en el mismo read")
                    manageReceivedMessage(extra)
                }

            getHostWatcher().debug( "logicalNetworkId [${logicalNetworkId}] parseIso FINISH RESULTS=>  [${ErrorCode.fromId(response.errorCode)}(${response.errorCode})] [${response.errorMessage}] \n\n")

        }
       
        catch(e:Exception){  
                handleException(e)

           val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

            AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"RECEIVE MESSAGE logicalNetworkId [${logicalNetworkId}] err[${err}]  logicalNetworkId [${logicalNetworkId}]  receiveMessage(noDisconnect:Boolean=false, readingTimeout:Int=-1):   ${e.message}")
            isReceivingMessage =false
 
            response.errorCode= ErrorCode.HostResponseInvalidFormat.errorCodeInt
            response.errorMessage= ErrorCode.HostResponseInvalidFormat.errorCodeMessage
            response.setResponseCode(ErrorCode.HostResponseInvalidFormat)
            getHostWatcher().error(   "logicalNetworkId [${logicalNetworkId}] receiveMessage EXCEPTION : " + e.message)
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] tryToConnect")
            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}] receiveMessage EXCEPTION  2 : " + e.message)
            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}] tryToConnect AGAIN ")
            val currentReadExceptions = commReadExceptionCounter.incrementAndGet()
            // println("receiveMessage EXCEPTION : " + e.message)
            isConnectedAndReady=false;

           

            if(currentReadExceptions>=commReadExceptionThreshold){ 
                AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_READ_ERROR,"logicalNetworkId [${logicalNetworkId}]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $currentReadExceptions INTENTOS \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                downBalancer("logicalNetworkId [${logicalNetworkId}] SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $currentReadExceptions INTENTOS \n ${getHostInfo()}\n ${commReadExceptionDetails}")
                commReadExceptionDetails=""
                downBalancerAndRebootSystem() 
                response.errorCode= ErrorCode.HostError.errorCodeInt
                response.errorMessage= ErrorCode.HostError.errorCodeMessage
                commReadExceptionCounter.set(0)
                
               return response

            }else{
                commReadExceptionDetails+=" logicalNetworkId [${logicalNetworkId}]  [${AlarmEmailService.getCurrentTimeCDMX()}]"+e.message.toString()+"<br>"
                //if(e.message.toString().lowercase().contains("time out")) {
                if(e is java.net.SocketTimeoutException) {
                    WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}] IT IS ONLY A TIMEOUT timeoutsCounter [$timeoutsCounter]")
                    // Restaurar isConnectedAndReady SOLO en contexto del hilo lector (noDisconnect==true).
                    // El logon usa receiveMessage() (noDisconnect=false): timeout ahi = handshake FALLIDO, NO marcar sano.
                    if (noDisconnect) isConnectedAndReady = true

                     // P0: si Prosa lleva > umbral sin mandar NADA (ni 0210 ni 0810), SACAR DEL BALANCEADOR.
                    // NO reconecta (no toca socket/streams) -> no toca la concurrencia de reconexion (N2).
                    val silence = System.currentTimeMillis() - lastHostMessageAtMs
                    if (noDisconnect && lastHostMessageAtMs != 0L && silence > silenceThresholdMs) {
                        WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] PROSA_SILENCE [${silence}ms] > [${silenceThresholdMs}ms] -> SACANDO DEL BALANCEADOR y reconectando")
                        downBalancer("PROSA_SILENCE [${silence}ms] logicalNetworkId [${logicalNetworkId}]")
                        reconnect("PROSA_SILENCE:${silence}ms", getHostWatcher())
                    }
                }else{
                   reconnect("RECEIVE_EXCEPTION:${e.javaClass.simpleName}", getHostWatcher())
                }
                response.errorCode= ErrorCode.HostError.errorCodeInt
                response.errorMessage= ErrorCode.HostError.errorCodeMessage
                return response
            }
           
             
        }
        if(bytesRead>0)
            getHostWatcher().debug( isoAscii,response,false)
        getHostWatcher().debug( "\n\n============logicalNetworkId [${logicalNetworkId}] = receiveMessage END ============= \n\n")
        isReceivingMessage =false
 
        response.errorCode= ErrorCode.Approved.errorCodeInt
        response.errorMessage= "logicalNetworkId [${logicalNetworkId}] SUCCESS RECEIVEING MESSAGE FROM ${hostType}"
        commReadExceptionCounter.set(0)
        lastHostMessageAtMs = System.currentTimeMillis()   // P0: oimos algo real de Prosa (incluye 0810 de echo)
         balancerDown = false   // re-armar: si Prosa vuelve a responder, un silencio futuro puede volver a sacar la instancia
        if(readingTimeout>0)
            socket.soTimeout = originalTimeOut

          

        return response

    }
    fun manageReceivedMessages(receivedMessagesList: List<Iso8583MetaData>): List<Iso8583MetaData> {
        val specialResponses = mutableListOf<Iso8583MetaData>()
        
        for (receivedMessage in receivedMessagesList) {
            val response = manageReceivedMessage(receivedMessage)
            specialResponses.add(response)
        }
        return specialResponses
    }
    fun manageReceivedMessage(guid:String,receivedMessage: Iso8583MetaData): Iso8583MetaData {
        var response= Iso8583MetaData()
        val transactionData=receivedMessage.transactionData
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage INI ")
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage IS_NULL : [${receivedMessage.transactionData==null}]")


        WatcherGlobalLog.commRaw(" logicalNetworkId [${logicalNetworkId}] [${guid}] manageReceivedMessage IS_NULL : [${receivedMessage.transactionData==null}]")
 


        if(receivedMessage.transactionData!=null){
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage ${receivedMessage.transactionData!!.systemTraceAuditNumber} " +
                    "rrn ${receivedMessage.transactionData!!.rrn} " +
                    "responseCode ${receivedMessage.transactionData!!.responseCode}" +
                    "transactionType ${receivedMessage.transactionData!!.transactionType}")


                    WatcherGlobalLog.commRaw(   "logicalNetworkId [${logicalNetworkId}] [${guid}]  manageReceivedMessage ${receivedMessage.transactionData!!.systemTraceAuditNumber} " +
                    "rrn ${receivedMessage.transactionData!!.rrn} " +
                    "responseCode ${receivedMessage.transactionData!!.responseCode}" +
                    "transactionType ${receivedMessage.transactionData!!.transactionType}")


        }

        if(transactionData==null){
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage txndata ISNULL")
            
            WatcherGlobalLog.commRaw(   "logicalNetworkId [${logicalNetworkId}] [${guid}]   manageReceivedMessage txndata ISNULL")


            response.errorCode=ErrorCode.EmptyResponce.errorCodeInt
            response.errorMessage= ErrorCode.EmptyResponce.errorCodeMessage
            return response
        }
        val wasLogon=transactionData.isLogOnRequestMessage(getHostWatcher())
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] wasLogon [$wasLogon] ")
            WatcherGlobalLog.commRaw(   "logicalNetworkId [${logicalNetworkId}] [${guid}]  wasLogon [$wasLogon]")
     
        if(wasLogon){ 
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOGON_OK," SE RECIBIO UN 0810 Y SE MANDO UN 0800 EXITOSAMENTE | DETAILS: [${receivedMessage.getDetails()}] ")

            response= sendLogOnResponse(transactionData.systemTraceAuditNumber,getHostWatcher())
            return response
        }
        val wasEcho=transactionData.isEchoTestRequestMessage(getHostWatcher())
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] wasEcho [$wasEcho] ")
 
        WatcherGlobalLog.commRaw(   "logicalNetworkId [${logicalNetworkId}] [${guid}]  wasEcho [$wasEcho] ")
 

        if(wasEcho) {
            getHostWatcher().debug(  "logicalNetworkId [${logicalNetworkId}] MESSAGE WAS ECHO TEST ")
            response = sendEchoTestResponse(transactionData.systemTraceAuditNumber, getHostWatcher())
            return response
        }
      //  if(receivedMessage.errorCode==0)
       // receivedMessages.add(receivedMessage)
        
        // Proteger el acceso a receivedMessages para evitar problemas de concurrencia
        //synchronized(receivedMessages) {
            //receivedMessages.add(receivedMessage)
       // }
        receivedMessage.receivedAtMs = System.currentTimeMillis()   // estampar tambien en esta via (hoy muerta) para que sea podable si se reactiva
        synchronized(receivedMessages) {
            receivedMessages.add(receivedMessage)
        }
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage   END ")

        WatcherGlobalLog.commRaw(   "logicalNetworkId [${logicalNetworkId}] [${guid}] manageReceivedMessage   END  ")

        return response
    }


    fun manageReceivedMessage(receivedMessage: Iso8583MetaData): Iso8583MetaData {
        var response= Iso8583MetaData()
        val transactionData=receivedMessage.transactionData
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage INI ")
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage IS_NULL : [${receivedMessage.transactionData!=null}]")

        if(receivedMessage.transactionData!=null){
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage ${receivedMessage.transactionData!!.systemTraceAuditNumber} " +
                    "rrn ${receivedMessage.transactionData!!.rrn} " +
                    "responseCode ${receivedMessage.transactionData!!.responseCode}" +
                    "transactionType ${receivedMessage.transactionData!!.transactionType}")
        }

        if(transactionData==null){
            getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage txndata ISNULL")
            response.errorCode=ErrorCode.EmptyResponce.errorCodeInt
            response.errorMessage= ErrorCode.EmptyResponce.errorCodeMessage
            return response
        }
        val wasLogon=transactionData.isLogOnRequestMessage(getHostWatcher())
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] wasLogon [$wasLogon] ")

        if(wasLogon){
            response= sendLogOnResponse(transactionData.systemTraceAuditNumber,getHostWatcher())
            return response
        }
        val wasEcho=transactionData.isEchoTestRequestMessage(getHostWatcher())
        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] wasEcho [$wasEcho] ")

        if(wasEcho) {
            getHostWatcher().debug(  "logicalNetworkId [${logicalNetworkId}] MESSAGE WAS ECHO TEST ")
            response = sendEchoTestResponse(transactionData.systemTraceAuditNumber, getHostWatcher())
            return response
        }
      //  if(receivedMessage.errorCode==0)
       // receivedMessages.add(receivedMessage)
        
        // Proteger el acceso a receivedMessages para evitar problemas de concurrencia
        //synchronized(receivedMessages) {
            //receivedMessages.add(receivedMessage)
        //}

        // NO descartar echo/logon 0810 aqui. Se encolan con timestamp y los huerfanos (echo interno fire-and-forget, respuestas
        // tardias) se expiran por TTL (pruneStaleReceivedMessages) -> tapa leak SIN romper el echo esperado.
        // Proteger el acceso a receivedMessages para evitar problemas de concurrencia
        receivedMessage.receivedAtMs = System.currentTimeMillis()
        synchronized(receivedMessages) {
            receivedMessages.add(receivedMessage)
        }

/* 
         try{

                if(receivedMessage.isoAscii!=""){
                      val rrn=receivedMessage.getRrn()
                      val terminalSerialNumber=receivedMessage.getTerminalSerialNumber()
                       val uuid=receivedMessage.getUUIDTxn()
                       val uuidBytes=StringUtils.hexStringToByteArray(uuid)
                    val isoAsciiBytes = StringUtils.asciiToByteArray(receivedMessage.isoAscii)
                      TransactionDBManager.updateIsoResponseByUuid(getHostWatcher(),uuidBytes,rrn,terminalSerialNumber,isoAsciiBytes,hostType)
                         getHostWatcher().debug( " updateIsoResponseByUuid uuid [${uuid}]  rrn [${rrn}]  terminalSerialNumber [${terminalSerialNumber}]   finalMessageClient [${receivedMessage.isoAscii}] hostType $[hostType] ")

                }     
                    
                }catch(ex:Exception){
                    handleException(ex)

                    val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
                     WatcherGlobalLog.dbError(" sendMessage updateIsoResponseByUuid EXCEPTION ${err}")

                     AlarmEmailService.sendAlarmEmail(
                             AlarmCause.DB_ERROR,
                                    " sendMessage updateIsoResponseByUuid EXCEPTION ${err}"
                       )
                } */

                   // La correlación (receivedMessages.add, arriba) ya ocurrió síncrona.
         // El guardado del ISO en BD se hace en otro hilo para NO frenar al lector.
         val watcher = getHostWatcher()   // capturar aquí: el ThreadLocal del Watcher no existe en el hilo async
         dbPersistExecutor.execute {
             try{
                 if(receivedMessage.isoAscii!=""){
                       val rrn=receivedMessage.getRrn()
                       val terminalSerialNumber=receivedMessage.getTerminalSerialNumber()
                       val uuid=receivedMessage.getUUIDTxn()
                       val uuidBytes=StringUtils.hexStringToByteArray(uuid)
                       val isoAsciiBytes = StringUtils.asciiToByteArray(receivedMessage.isoAscii)
                       TransactionDBManager.updateIsoResponseByUuid(watcher,uuidBytes,rrn,terminalSerialNumber,isoAsciiBytes,hostType)
                       watcher.debug( " updateIsoResponseByUuid uuid [${uuid}]  rrn [${rrn}]  terminalSerialNumber [${terminalSerialNumber}]   finalMessageClient [${receivedMessage.isoAscii}] hostType $[hostType] ")
                 }
             }catch(ex:Exception){
                 val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
                 WatcherGlobalLog.dbError(" updateIsoResponseByUuid EXCEPTION ${err}")
                 AlarmEmailService.sendAlarmEmail(AlarmCause.DB_ERROR, " updateIsoResponseByUuid EXCEPTION ${err}")
             }
         }


        getHostWatcher().debug(   "logicalNetworkId [${logicalNetworkId}] manageReceivedMessage   END ")
        return response
        }



    // 1) ===================== checkReceivedMessage =====================
    // overload legacy: sin RRN => match solo por STAN (echo/logon/KeyMAC/KeyPIN y callers muertos)
    fun checkReceivedMessage(systemTraceNumber: String, watcher: Watcher): Iso8583MetaData? =
        checkReceivedMessage(systemTraceNumber, "", watcher)

    fun checkReceivedMessage(systemTraceNumber: String, requestRrn: String, watcher: Watcher): Iso8583MetaData? {
        var found: Iso8583MetaData? = null

        // Buscar y eliminar de receivedMessages de forma segura
        /*synchronized(receivedMessages) {
            val it = receivedMessages.iterator()
            while (it.hasNext()) {
                val msg = it.next() ?: continue
                val td  = msg.transactionData
                if (td?.systemTraceAuditNumber == systemTraceNumber) {
                    it.remove()            // <- eliminación segura sin CME
                    found = msg
                    break
                }
            }
        }*/
        val staleCutoff = System.currentTimeMillis() - getAppHostSettingsData().clientWaitAnswerTimeOut * 2
        var stanRrnCollision = false
        synchronized(receivedMessages) {
            val it = receivedMessages.iterator()
            while (it.hasNext()) {
                val msg = it.next() ?: continue
                if (msg.receivedAtMs in 1L..staleCutoff) { onStaleOrphanPruned(msg, System.currentTimeMillis()); it.remove(); continue }
                val td  = msg.transactionData
                val stanMatch = td?.systemTraceAuditNumber == systemTraceNumber
                if (stanMatch && (requestRrn.isBlank() || td?.rrn?.trim() == requestRrn.trim())) {
                    it.remove()            // <- eliminación segura sin CME
                    found = msg
                    break
                } else if (stanMatch && requestRrn.isNotBlank()) {
                    stanRrnCollision = true   // STAN igual, RRN distinto: es de OTRA trx, no entregar
                }
            }
        }
        // log UNA sola vez (fuera del loop y solo si entregamos la correcta tras esquivar una colision).
        // El caso "colision sin respuesta correcta" se loguea al expirar el huerfano (onStaleOrphanPruned).
        if (stanRrnCollision && found != null) {
            WatcherGlobalLog.commError("logicalNetworkId [${logicalNetworkId}] AVOIDED_MISCORRELATION stan=$systemTraceNumber reqRrn=$requestRrn")
        }


        if (found != null) {
            watcher.debug("logicalNetworkId [${logicalNetworkId}] checkReceivedMessage REMOVE RESPONSE : systemTraceAuditNumber ${found!!.transactionData?.systemTraceAuditNumber}")
            // Importante: llamar fuera del lock de receivedMessages para evitar deadlocks cruzados
            removeRequestMessage(found!!, found, watcher)
        } else {
            watcher.debug("logicalNetworkId [${logicalNetworkId}] checkReceivedMessage CANNOT REMOVE RESPONSE : systemTraceAuditNumber $systemTraceNumber")
        }

        return found
    }

 

        // 3) ===================== removeRequestMessage =====================
    fun removeRequestMessage(
        isoMetaData: Iso8583MetaData,
        isoResponse: Iso8583MetaData?,
        watcher: Watcher
    ) {
        // Validaciones null-safe de claves de correlación (misma lógica)
        val rrn = isoMetaData.getRrn()
        val stn = isoMetaData.getSystemTraceNumber()
        if (rrn.isNullOrBlank() || stn.isNullOrBlank()) {
            watcher.debug("logicalNetworkId [${logicalNetworkId}] removeRequestMessage(): rrn/stn vacíos; no se elimina de la cola")
            return
        }

        watcher.debug("logicalNetworkId [${logicalNetworkId}] removeRequestMessage rrn=$rrn stn=$stn")

        try {
            // Status según response (igual que tu lógica)
            val respCode = isoResponse?.getResponseCode()
            isoMetaData.setTransactionStatus(
                when {
                    respCode == null -> TransactionStatus.HostTimeOut
                    respCode == "00" -> TransactionStatus.Approved
                    else -> TransactionStatus.Rejected
                }
            )

            var removed = false
            // Eliminar de requestMessages de forma segura
            synchronized(requestMessages) {
                val it = requestMessages.iterator()
                while (it.hasNext()) {
                    val m  = it.next()
                    val td = m.transactionData
                    if (td?.systemTraceAuditNumber == stn && td.rrn == rrn) {
                        it.remove()     // <- eliminación segura sin CME
                        removed = true
                        break
                    }
                }
            }

            if (removed) {
                watcher.debug("logicalNetworkId [${logicalNetworkId}] removeRequestMessage(): eliminado de requestMessages rrn=$rrn stn=$stn")
            } else {
                watcher.debug("logicalNetworkId [${logicalNetworkId}] removeRequestMessage(): no se encontró en requestMessages rrn=$rrn stn=$stn")
            }

        } catch (e: Exception) {
             handleException(e)

            val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"
            val currentReadExceptions = commReadExceptionCounter.incrementAndGet()

            AlarmEmailService.sendAlarmEmail(
                AlarmCause.COMM_ERROR,
                " logicalNetworkId [${logicalNetworkId}]  commReadExceptionCounter [$currentReadExceptions] [$err] " +
                "removeRequestMessage(isoMetaData: Iso8583MetaData, isoResponse:Iso8583MetaData?, watcher: Watcher){ ${e.message}"
            )

            if (currentReadExceptions >= commReadExceptionThreshold) {
                AlarmEmailService.sendAlarmEmail(
                    AlarmCause.SOCKET_READ_ERROR,
                    "logicalNetworkId [${logicalNetworkId}]  SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $currentReadExceptions INTENTOS \n ${getHostInfo()}\n $err"
                )
                downBalancer(
                    "logicalNetworkId [${logicalNetworkId}] SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $currentReadExceptions INTENTOS \n ${getHostInfo()}\n $err"
                )
                commReadExceptionDetails = ""
                downBalancerAndRebootSystem()
                commReadExceptionCounter.set(0)
            }

            watcher.error("logicalNetworkId [${logicalNetworkId}] ERROR removeRequestMessage ${e.message}")
        }
    }


/**
 * Registra un request para correlacionarlo con su respuesta.
 * Echoes y logons no tienen RRN, no se pueden correlacionar con removeRequestMessage,
 * y antes se acumulaban aquí para siempre (fuga de memoria) — por eso se excluyen.
 */
private fun trackRequest(isoMetaData: Iso8583MetaData) {
    val rrn = isoMetaData.getRrn()
    val stn = isoMetaData.getSystemTraceNumber()
    if (rrn.isNullOrBlank() || stn.isNullOrBlank()) return
    synchronized(requestMessages) {
        requestMessages.add(isoMetaData)
    }
}

    fun sendMessage(iso8583MetaData: Iso8583MetaData, watcher: Watcher): Iso8583MetaData {
        watcher.debug( "\n\n============= logicalNetworkId [${logicalNetworkId}] sendMessage WAITING INIT============= \n\n")
        watcher.debug( "logicalNetworkId [${logicalNetworkId}] hostResponseHardcode CONNECTED: [${AppSettings.getHostConfig(hostType).hostResponseHardcode}]")
          val rrn=iso8583MetaData.getRrn()

        WatcherGlobalLog.commDebug( rrn,"logicalNetworkId [${logicalNetworkId}] hostResponseHardcode CONNECTED: [${AppSettings.getHostConfig(hostType).hostResponseHardcode}]")

        if(AppSettings.getHostConfig(hostType).hostResponseHardcode){
            var errorcode=AppSettings.getHostConfig(hostType).getErrorCodeForTransaction(iso8583MetaData.transactionType)
            iso8583MetaData.errorCode=errorcode.errorCodeInt
            iso8583MetaData.errorMessage="$hostType "+errorcode.errorCodeMessage
            return iso8583MetaData
        }  
        watcher.debug( "logicalNetworkId [${logicalNetworkId}] sendMessage CONNECTED: [${socket.isConnected}]")

        if(!AppSettings.getHostConfig(hostType).hostIsActive()){
            iso8583MetaData.errorCode=ErrorCode.HostIsInactive.errorCodeInt
            iso8583MetaData.errorMessage="$hostType "+ErrorCode.HostIsInactive.errorCodeMessage
            return iso8583MetaData
        }  
       
        if(!socket.isConnected){
           watcher.debug( "logicalNetworkId [${logicalNetworkId}] TRYING RECONNECTING \n\n")
           watcher.debug( "logicalNetworkId [${logicalNetworkId}] TRYING RECONNECTING -->")
           WatcherGlobalLog.commRaw( "logicalNetworkId [${logicalNetworkId}] TRYING RECONNECTING -->")

            val connectResponse= connectToHost(watcher)
            if(connectResponse.errorCode!=0){
               watcher.debug( "logicalNetworkId [${logicalNetworkId}] ERROR RECONNECTING \n\n")
                iso8583MetaData.setTransactionStatus(TransactionStatus.Error)

                iso8583MetaData.errorMessage=connectResponse.errorMessage
                iso8583MetaData.errorCode=connectResponse.errorCode
                return iso8583MetaData
            }
        }
        watcher.debug( "\n\n============= logicalNetworkId [${logicalNetworkId}] sendMessage INIT============= \n\n")
        watcher.debug( "logicalNetworkId [${logicalNetworkId}] SENDING MESSAGE.... ")
        watcher.debug( iso8583MetaData.isoAscii,iso8583MetaData,true )
        watcher.debug( "logicalNetworkId [${logicalNetworkId}] COMM LOG OK ")
        val clientMessage=iso8583MetaData.getIsoByteArray()
        if (outputStream != null && dataInputStream != null) {

            inFlight.incrementAndGet()
 
            try {
                //  println("clientMessage: " + StringUtils.encodeHexString(clientMessage))

               watcher.debug( "logicalNetworkId [${logicalNetworkId}] appMessage: " + StringUtils.encodeHexString(clientMessage))
                WatcherGlobalLog.commRaw("logicalNetworkId [${logicalNetworkId}] SENT : ${iso8583MetaData.isoAscii}")
              
                val lenInt = clientMessage.size+1
                val sizeSent = Integer.toHexString(lenInt).padStart(4, '0')
                val intBytes = StringUtils.hexStringToByteArray(Integer.toHexString(lenInt).padStart(4, '0'))
                val finalMessageClient: ByteArray

                watcher.debug( "logicalNetworkId [${logicalNetworkId}] sizeHeader: " + sizeSent)

                finalMessageClient = ByteArray(2 + clientMessage.size + 1)

                for (i in 0..1) {
                    finalMessageClient[i] = intBytes[i]
                }
                var index = 2
                for (i in 0 until clientMessage.size) {

                    finalMessageClient[index] = clientMessage[i]
                    index++
                }


                finalMessageClient[finalMessageClient.size - 1] = 0x03

                val isNetworkManagement0800 =
                    iso8583MetaData.transactionData?.getMessageTypeString() == "0800" ||
                    iso8583MetaData.transactionData?.transactionType?.messageTypeRequest == "0800" ||
                    iso8583MetaData.transactionType.messageTypeRequest == "0800"

                if (isNetworkManagement0800) {
                    watcher.debug("logicalNetworkId [${logicalNetworkId}] Omitiendo cifrado Pandora y updateIsoRequestByUuid para mensaje MTI 0800")
                } else {
             try{
                      val terminalSerialNumber=iso8583MetaData.getTerminalSerialNumber()
                    val uuid=iso8583MetaData.getUUIDTxn()
                       val uuidBytes=StringUtils.hexStringToByteArray(uuid)
                    var metaData=ByteArray(0)
                     watcher.debug( " CHECKING_TXNDATA NULL?= [${iso8583MetaData.transactionData!=null}]")
                  WatcherGlobalLog.commDebug( rrn," CHECKING_TXNDATA NULL?= [${iso8583MetaData.transactionData!=null}]")

                    if(iso8583MetaData.transactionData!=null){

                        try{  
                         
                            var txnDBData=TransactionDBData()
                            WatcherGlobalLog.commDebug( rrn,"PREV_iso8583MetaData.transactionData!!")
                           BeanUtils.copyProperties(iso8583MetaData.transactionData!!,txnDBData)
                            //var transDto=TransactionDto()
                            //BeanUtils.copyProperties(iso8583MetaData.transactionData!!,transDto)
                            val jsonString =iso8583MetaData.transactionData!!.describeJSON()
                            //BeanUtils.copyProperties(transDto, txnDBData)
                            txnDBData.injectMetaDataStr(jsonString)   
                            WatcherGlobalLog.commDebug( rrn," transDto_jsonString =$jsonString ") 
                          //  WatcherGlobalLog.commDebug( rrn," iso8583MetaData.transactionData!! ${ iso8583MetaData.transactionData!!.describeJSON()}") 
                              //  watcher.debugObj(txnDBData)   
                             metaData=DBCipherManager.encryptTransactionData(txnDBData,watcher).metaData
                           // WatcherGlobalLog.commDebug( rrn," txnDBData =${GsonConfig.gson.toJson(txnDBData)}")
                            WatcherGlobalLog.commDebug( rrn," rrn =${txnDBData.rrn}")
 
                        }catch(ex:Exception){
                               handleException(ex) 

                               val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

                             watcher.error( " encryptTransactionData EXCEPTION  [${err}]")

                        }
                      

                    }

                    TransactionDBManager.updateIsoRequestByUuid(watcher,uuidBytes,rrn,terminalSerialNumber,metaData,finalMessageClient,hostType)
                     watcher.debug( " uuid [${uuid}] rrn [${rrn}]  terminalSerialNumber [${terminalSerialNumber}] finalMessageClient [${finalMessageClient}] hostType $[hostType] ")

                }catch(ex:Exception){
                   handleException(ex)

                    val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

                     AlarmEmailService.sendAlarmEmail(
                             AlarmCause.DB_ERROR,
                                    " sendMessage updateIsoRequestByUuid EXCEPTION ${err}"
                       )
                }
                }
                //     println("finalMessageClient: " + StringUtils.encodeHexString(finalMessageClient))
               // logger.commLog(this.hostType,"appMessage TCP: " + StringUtils.encodeHexString(finalMessageClient))
                iso8583MetaData.setTransactionStatus(TransactionStatus.SentToHost)
            

                synchronized(outputStream!!) {
                  
    
                    outputStream!!.write(finalMessageClient)
                    outputStream!!.flush() 
                }


                iso8583MetaData.setTransactionStatus(TransactionStatus.WaitingForResponse)
                trackRequest(iso8583MetaData)
                commWriteExceptionCounter.set(0)





                /*if(!AppSettings.getInstance().waitForReception) {
                    val receiveMessageResponse = receiveMessage(logger)
                    manageReceivedMessage(receiveMessageResponse, logger)
                    return receiveMessageResponse
                }*/

            }
          
            catch(e:Exception)
            {
                handleException(e)
                 val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"

                //AlarmEmailService.sendAlarmEmail(AlarmCause.COMM_ERROR,"logicalNetworkId [${logicalNetworkId}]   sendMessage(iso8583MetaData: Iso8583MetaData, watcher: Watcher): Iso8583MetaData {  ${err}")
              
              
                if(err.lowercase().contains("is null")||err.contains("nullpointerexception")){
                    val currentNpeCount = nullPointerExceptionCounter.incrementAndGet()
                    var isoAscii="ISO_ASCII"
                    if(iso8583MetaData!=null){
                         isoAscii=iso8583MetaData.isoAscii
                    }
                    AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_WRITE_ERROR,"  isoAscii [${isoAscii}]  nullPointerExceptionCounter [${currentNpeCount}]  EXCEPTION [${e.message.toString()}] RRN  [${iso8583MetaData.getRrn()}]   TSN [${iso8583MetaData.getTerminalSerialNumber()}]  ${err}")
                    if(currentNpeCount>=17){ 
                           AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_WRITE_ERROR," logicalNetworkId [${logicalNetworkId}]    SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE ${commWriteExceptionCounter.get()} INTENTOS \n ${getHostInfo()} \n ${commWriteExceptionDetails}   ${err}")
                            WatcherGlobalLog.commError( "REBOOT_DAEMON")
                            iso8583MetaData.errorMessage= ErrorCode.HostError.errorCodeMessage+" nullPointerExceptionCounter [${currentNpeCount}] logicalNetworkId [${logicalNetworkId}]  ${e.message}"
                            iso8583MetaData.errorCode= ErrorCode.HostError.errorCodeInt
                            downBalancerAndRebootSystem()     
                            commWriteExceptionCounter.set(0)
                            nullPointerExceptionCounter.set(0)
                            return iso8583MetaData     
                    }
                } else{
                    isSendingMessage=false 
                     val currentWriteExceptions = commWriteExceptionCounter.incrementAndGet()

                       if(currentWriteExceptions>=commWriteExceptionThreshold){ 
                            AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_WRITE_ERROR," logicalNetworkId [${logicalNetworkId}]    SE HA INTENTADO REESTABLECER LA CONECTIVIDAD PERO NO SE HA LOGRADO DESPUES DE $currentWriteExceptions INTENTOS \n ${getHostInfo()} \n ${commWriteExceptionDetails}   ${err}")
                            WatcherGlobalLog.commError( "REBOOT_DAEMON")
                            iso8583MetaData.errorMessage= ErrorCode.HostError.errorCodeMessage+"logicalNetworkId [${logicalNetworkId}]  ${e.message}"
                            iso8583MetaData.errorCode= ErrorCode.HostError.errorCodeInt
                            downBalancerAndRebootSystem()     
                            commWriteExceptionCounter.set(0)
                            return iso8583MetaData     
                        }
    
                 }
              
              
               watcher.error(  "logicalNetworkId [${logicalNetworkId}] sendMessage Exception [${e.message}]")
                iso8583MetaData.errorMessage= ErrorCode.ClientTimeOut.errorCodeMessage+" ${e.message}"
                iso8583MetaData.errorCode= ErrorCode.ClientTimeOut.errorCodeInt
                WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}] sendMessage Exception [${e.message}]")
                isConnectedAndReady=false
                if(e.message!=null)
                    iso8583MetaData.errorMessage=e.message!!
                else{
                    iso8583MetaData.errorMessage="EXCEPCION 03 MENSAJE_EXCEPCION ES NULO  logicalNetworkId [${logicalNetworkId}] "
                }
 
                commWriteExceptionDetails+=" logicalNetworkId [${logicalNetworkId}]  [${AlarmEmailService.getCurrentTimeCDMX()}]"+e.message.toString()+"<br>" +err 

                if(e.message.toString().contains("Broken pipe")){
                    WatcherGlobalLog.commError(  " logicalNetworkId [${logicalNetworkId}]  I AM GOING TO TRY TO FIX BROKEN PIPE")
                    WatcherGlobalLog.commError(  "logicalNetworkId [${logicalNetworkId}]  rebootByPipeBroken INIT ")
                  
                   
                      val currentBrokenPipeCount = brokenPipeCounter.getAndIncrement()
                      AlarmEmailService.sendAlarmEmail(AlarmCause.SOCKET_WRITE_ERROR," Hay un error Critico brokenPipeCounter [${currentBrokenPipeCount}], DESCRIPCION: [${e.message}]")
 
                    reconnect("SEND_BROKEN_PIPE", watcher)
                    WatcherGlobalLog.commError(  "logicalNetworkId [${logicalNetworkId}]  rebootByPipeBroken END ")
                }else{ 
                    reconnect("SEND_EXCEPTION:${e.javaClass.simpleName}", watcher)
                } 
 


                
                return iso8583MetaData
            }finally {
                inFlight.decrementAndGet()
            }
                    
        } else {
           watcher.debug(  " logicalNetworkId [${logicalNetworkId}] sendMessage HOST is DISCONNECTED")
            iso8583MetaData.errorCode= ErrorCode.ClientTimeOut.errorCodeInt
            iso8583MetaData.errorMessage= ErrorCode.ClientTimeOut.errorCodeMessage+" Streams are closed "
            WatcherGlobalLog.commError( "logicalNetworkId [${logicalNetworkId}]  sendMessage HOST is DISCONNECTED")
             
            isConnectedAndReady=false
            reconnect("SEND_STREAMS_CLOSED", watcher)
            return iso8583MetaData

        }
       watcher.debug( "\n\n=============logicalNetworkId [${logicalNetworkId}]  sendMessage END============= \n\n")


        return iso8583MetaData

    }






}
