package com.viergegroup.swmain.managers

import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import com.shared.commons.AppSettings
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.entities.RequestEntity
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Date
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.BufferedReader;
import java.io.InputStreamReader;
import okhttp3.internal.toImmutableList

 
object AlarmEmailService {   
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private  var USERNAME =  AppSettings.getInstance().alarmsEmail// "notifications@viergegroup.com"
    private  var PASSWORD =  AppSettings.getInstance().alarmsPwd// "!2024V13rG3@05"
    private var RECIPIENTS =AppSettings.getInstance().alarmsRecipients
    private var enableAlarms =AppSettings.getInstance().enableAlarms
    private var serverName =AppSettings.getInstance().serverName
    //private val counterMap = mutableMapOf<AlarmCause, Int>()
    private val counterMap = java.util.concurrent.ConcurrentHashMap<AlarmCause, Int>()
    private val gson = Gson() 
    private val emailLogFile = File("${AppSettings.getInstance().dataPath}/email_log.txt") // Archivo JSON para guardar los registros
   // private var emailLog: MutableMap<String, Long> = loadEmailLog()
   private val emailLog: MutableMap<String, Long> = java.util.concurrent.ConcurrentHashMap<String, Long>().apply { putAll(loadEmailLog()) }
    private val lastRebootFile = "${AppSettings.getInstance().dataPath}/lastReboot.txt"
    
   
fun sendAlarmEmail(cause: AlarmCause, errorDescription: String) { 
    AlarmTelegramService.sendAlarmTelegram(cause,errorDescription)


    if (!enableAlarms) {
        WatcherGlobalLog.alarmsDebug("$cause Las alarmas están deshabilitadas. Correo no enviado.")
        return 
    }
    WatcherGlobalLog.alarmsDebug("$cause sendAlarmEmail INIT")
 
    RECIPIENTS = AppSettings.getInstance().alarmsRecipients

    //val counter = counterMap.getOrPut(cause) { 0 } + 1
    //counterMap[cause] = counter
    val counter = counterMap.merge(cause, 1) { old, _ -> old + 1 }!!
    val formattedCounter = counter.toString().padStart(3, '0')

    val currentTime = System.currentTimeMillis()
    var emailList = mutableListOf<String>() // Corrección aquí
    WatcherGlobalLog.alarmsDebug("$cause RECIPIENTS $RECIPIENTS ")

    if (RECIPIENTS.isNullOrEmpty()) {
        WatcherGlobalLog.alarmsDebug("$cause No hay destinatarios configurados para las alarmas. Correo no enviado.")
        return
    }

    /*try {
      
        if(cause!=AlarmCause.COMM_WARN&&cause!=AlarmCause.CLIENT_TIMEOUT){
            val lastSentTime = emailLog[cause.name] ?: 0L
            val elapsedTime = currentTime - lastSentTime
            val threshold = 2.7 * 60 * 1000
            if (elapsedTime < threshold) {
                WatcherGlobalLog.alarmsDebug("Correo ya enviado recientemente para la causa: $cause [${errorDescription}]. Correo no reenviado.")
                return 
            }
    
        }
    
    

    } catch (ex: Exception) {
        WatcherGlobalLog.alarmsDebug("$cause EX EN EMAILS ${ex.message.toString()}")
    }*/

    // Filtrar destinatarios para la causa de alarma
    val emails = RECIPIENTS.firstOrNull { it.AlarmCause == cause }?.emailList
    if (emails.isNullOrEmpty()) {
        WatcherGlobalLog.alarmsDebug("No se encontraron destinatarios para la alarma de tipo: $cause. Correo no enviado.")
        return
    }
    emailList = emails.toMutableList() // Corrección aquí

    try {
        val properties = Properties().apply {
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }
        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(USERNAME, PASSWORD)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(USERNAME))
            emailList.forEach { recipient ->
                addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
            }
            subject = "AB [${serverName}]-[${formattedCounter}]| ${cause.title}"
            setContent(
                """
                <html>
                    <body>
                        <h1>${serverName}</h1>
                        <h2>${cause.title}</h2>
                        <p><b>Hora detectada (CDMX):</b> ${getCurrentTimeCDMX()}</p>
                        <h3>${cause.message}</h3>
                        <hr/>
                        <b>Detalles:</b>
                        <p>$errorDescription</p>
                        <hr/>
                       <p>SSH IP:<b> ${getSshIp()}</b></p>
                    </body>
                </html>
                """.trimIndent(),
                "text/html"
            )
        }

        Transport.send(message)
        emailLog[cause.name] = currentTime
        saveEmailLog()
        WatcherGlobalLog.alarmsDebug("$cause Correo enviado exitosamente a : ${emailList.joinToString(", ")}")
    } catch (e: MessagingException) {
        WatcherGlobalLog.alarmsDebug("$cause Error al enviar el correo: ${e.message}")
    }
}

    private fun loadEmailLog(): MutableMap<String, Long> {
        try{
            return if (emailLogFile.exists()) {
                val type = object : TypeToken<MutableMap<String, Long>>() {}.type
                gson.fromJson(emailLogFile.readText(), type) ?: mutableMapOf()
            } else {
                mutableMapOf()
            }
        }catch(ex:Exception){
            WatcherGlobalLog.alarmsDebug("loadEmailLog_EX ${ex.message}")
            return  mutableMapOf()
        }
       
    }
/* 
    private fun saveEmailLog() {
        emailLogFile.writeText(gson.toJson(emailLog))
    } */
            private val emailLogLock = Any()
        private fun saveEmailLog() {
            synchronized(emailLogLock) {
                emailLogFile.writeText(gson.toJson(emailLog))
            }
        }
    fun getCurrentTimeCDMX(): String {
        val cdmxTimeZone = TimeZone.getTimeZone("America/Mexico_City") // Zona horaria de CDMX
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
            timeZone = cdmxTimeZone
        }
        return dateFormat.format(Date()) // Devuelve la hora actual formateada
    }

    /* 
   private fun getSshIp(): String {
        return try {
            // Ejecutar el comando 'who' para obtener información de las sesiones activas
            val process = Runtime.getRuntime().exec("who")
            val reader = process.inputStream.bufferedReader()
    
            reader.useLines { lines ->
                lines.forEach { line ->
                    // Ejemplo de salida de `who`:
                    // usuario    pts/0        2024-12-05 12:00 (192.168.1.100)
                    if (line.contains("(")) {
                        val start = line.indexOf("(") + 1
                        val end = line.indexOf(")")
                        return line.substring(start, end)
                    }
                }
            }
            "SIN IP"
        } catch (e: Exception) {
            e.printStackTrace()
            e.message.toString()
        }
    }*/
     private fun getSshIp(): String {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec("who")
        val ip = process.inputStream.bufferedReader().useLines { lines ->
            lines.firstOrNull { it.contains("(") }
                ?.let { it.substring(it.indexOf("(") + 1, it.indexOf(")")) }
        }
        process.waitFor()
        ip ?: "SIN IP"
    } catch (e: Exception) {
        e.printStackTrace()
        e.message.toString()
    } finally {
        process?.destroy()
    }
}
 
}

class AlarmCauseData:RequestEntity(){
    var alarmCause:AlarmCause=AlarmCause.SYSTEM_RESTART
    var errorDescription:String=""
 
} 



enum class AlarmCause(val title: String, val message: String) {
  // --- ERRORES DE SOCKET (todos con nivel en el título) ---
    SOCKET_BROKEN_PIPE(
        "[CRITICAL] SOCKET BROKEN PIPE",
        "El socket fue cerrado por el servidor durante escritura. Requiere reinicio de servicio y revisión de túnel IPSec (Flo-Flo)."
    ),
    SOCKET_CONNECTION_RESET(
        "[HIGH] SOCKET CONNECTION RESET",
        "El servidor remoto cerró la conexión bruscamente o reinició. Puede requerir cierre de sockets y verificación de red IPSec. o AUTORIZADOR"
    ),    
    TRY_TO_CONNECT(   
        "INTENTANDO CONECTARSE AL AUTORIZADOR",
        ""
    ), 
    TRY_TO_SEND_TXN(   
        "INTENTANDO ENVIAR TRANSACCION PERO NO ESTA CONECTADO AL AUTORIZADOR",
        ""
    ),
    SOCKET_CLOSED(
        "[MEDIUM-HIGH] SOCKET CLOSED",
        "El servidor remoto cerró la conexión. Puede requerir cierre de sockets y verificación de red IPSec o AUTORIZADOR"
    ), 
    SOCKET_READ_TIMEOUT( 
        "[MEDIUM] SOCKET READ TIMEOUT",
        "No hubo respuesta en el tiempo esperado; posible latencia, congestión o bloqueo remoto."
    ),
    SOCKET_CONNECT_TIMEOUT(
        "[HIGH] SOCKET CONNECT TIMEOUT",
        "No se pudo establecer la conexión dentro del límite configurado. Revisar conectividad o túnel IPSec (Flo-Flo)."
    ),
    SOCKET_BIND_ERROR(
        "[HIGH] SOCKET BIND ERROR",
        "El puerto local está en uso o bloqueado por otro proceso. Se recomienda liberar puerto y reiniciar servicio."
    ),
    UNKNOWN_HOST(
        "[HIGH] SOCKET UNKNOWN HOST",
        "No se pudo resolver el nombre del host (DNS/hosts). Revisar configuración o túnel IPSec (Flo-Flo)."
    ),
    NETWORK_UNREACHABLE(
        "[HIGH] SOCKET NETWORK UNREACHABLE",
        "No hay ruta hacia el servidor. Verificar firewall, routing o túnel IPSec (Flo-Flo)."
    ),
    GENERIC_COMM_ERROR(
        "[MEDIUM-HIGH] SOCKET COMMUNICATION ERROR",
        "Error inesperado en la comunicación del socket. Puede requerir reinicio de sesión o revisión de red."
    )

    ,
    SYSTEM_RESTART(
        "SE REINICIARA EL SERVICIO EN LOS PROXIMOS MINUTOS ",
        "" 
    ), 
    RECONNECT_OK(
        "RECONEXION EXITOSA ",
        "" 
    ), 
    RECONNECT_FAIL(
        "RECONEXION FALLIDA ",
        "" 
    ), 
    ECHO_REQUEST(
        "SE HA MANDANDO UN ECHO ",
        "" 
    ), 
    ECHO_ERROR(
        "ERROR EN ECHO",
        "" 
    ), 



    


    SYSTEM_INITIALIZED(
        "INICIALIZACIÓN DEL SERVICIO",
        "El sistema ha sido <b>inicializado</b>."
    ), 
    SYSTEM_STOPPED(
        "DETENCION DEL SERVICIO",
        "El sistema ha sido <b>detenido</b>."
    ),
   HOST_COMM_INITIALIZED(
        "COMUNICACION CON HOST INICIALIZANDO",
        ""
    ),
    CONNECTION_ERROR(
        "ERROR DE CONEXIÓN",
        "Se produjo un <b>error al abrir la conexión</b>."
    ),
    SOCKET_WRITE_ERROR(   
        "ERROR DE ESCRITURA EN EL CANAL (SOCKET)",
        "Se produjo un <b>error al escribir en el conexión</b>."
    ),  
    
    COMM_WARN(   
        "WARNING DE CONEXION",
        "Esto es solo un WARNING , NO HACE FALTA REALIZAR NINGUNA ACCION."
    ), 
    COMM_ERROR(   
        "ERROR DE CONEXION NO CONTROLADO",
        ""
    ),COMM_TIMEOUT(   
        "ERROR DE TIMEOUT",
        ""
    ), 
    LOG_ERROR(   
        "ERROR DE LOG",
        "Se produjo un <b>error al escribir en logs</b>."
    ), 

    ISO_ERROR(
        "ERROR EN ISO8583", 
        "."
    ),    
    SOCKET_READ_ERROR(
        "ERROR DE LECTURA EN EL CANAL (SOCKET)",
        "Se produjo un <b>error al leer desde el socket</b>."
    ),    
       CLIENT_TIMEOUT(
        "ERROR DE TIMEOUT DE CLIENTE",
        "Se produjo un <b>timeout al mandar una solicitud al utorizador</b>."
    ),   
    SOCKET_OK(
        "CONEXION ESTABLECIDA",
        "Se ha conectado  <b>EXITOSAMENTE</b>."
    ),  
    DISK_SPACE_ERROR(
        "SE INICIARA EL PROCESO DE MOVER LOGS",
        "Se ejecutara el script sh para mover logs a bucket"
    ),  
    DB_ERROR(
        "Error de Base de Datos En Transaccion",
        "Ocurrio un error En Db Transaccional"
    ),  
    DISK_SPACE_FINISHED( 
        "EL PROCESO DE MOVIMIENTO DE LOGS HA FINALIZADO",
        "EL PROCESO DE MOVIMIENTO DE LOGS HA FINALIZADO"
    ),
    DISK_SPACE_DANGER(
        "ESPACIO EN DISCO ES CRITICO",
        "EL ESPACIO DEL DISCO ES CRITICO"
    ),  

    CPU_WARNING(
        "ADVERTENCIA: USO DE CPU ALTO",
        "El <b>uso de la CPU</b> ha superado el umbral establecido."
    ),
    RAM_WARNING(
        "ADVERTENCIA: USO DE RAM ALTO",
        "El <b>uso de la memoria RAM</b> ha superado el umbral establecido."
    ),
    NETWORK_WARNING(
        "ADVERTENCIA: USO DE RED ALTO",
        "El <b>uso de la red</b> ha alcanzado un nivel crítico."
    )
    ,
    TXNS_MIN_WARNING(
        "ADVERTENCIA: VARIAS TRANSACCIONES POR MINUTO RECHAZADAS",
        "El <b>numero de transacciones rehazadas</b> ha alcanzado un nivel crítico."
    )
    , ENDPOINT_ERROR(
        "HAY UN ERROR NO CONTROLADO DEL ENDPOINT ",
        ""
    )
    , 
    TXNS_DAY_WARNING(
        "ADVERTENCIA: VARIAS TRANSACCIONES POR DIA RECHAZADAS",
        "El <b>numero de transacciones rehazadas</b> ha alcanzado un nivel crítico."
    )
 ,
    BALANCER_DOWN(
        "SE INTENTARA DAR DE BAJA LA INSTANCIA DEL BALANCEADOR,LEER LOS DETALLES DE ESTE CORREO, EN CASO DE QUE NO SE HAYA DADO DE BAJA CORRECTAMENTE FAVOR DE VERIFICARLO A MANO",
        "" 
    ) 
   ,
    BALANCER_UP(
        "VERIFICANDO QUE LA INSTANCIA ESTE EN EL BALANCEADOR , EN CASO DE QUE NO ESTE ARRIBA FAVOR DE VERIFICARLO A MANO",
        ""
    )  
   ,
    BALANCER_ERROR(
        "ADVERTENCIA: ERROR AL DAR DE BAJA LA INSTANCIA DEL BALANCEADOR",
        ""
    )
  ,
    REVERSE_ERROR(
        "ERROR EN REVERSO",
        ""
    ) 
  ,
    LOGON_OK(
        "LOGON EXITOSO",
        ""
    ) ,
      LOGON_REQUEST(
        "LOGON REQUEST",
        ""
    ) ,
    LOGON_ERROR(
        "ERROR EN LOGON",
        ""
    )
 

} 

data class AlarmRecipient(
    var AlarmCause: AlarmCause ,
    var emailList: List<String> 
    
) 

