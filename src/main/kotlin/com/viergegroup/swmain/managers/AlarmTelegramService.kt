package com.viergegroup.swmain.managers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shared.commons.AppSettings
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import okhttp3.*
import java.io.File
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit
  
object AlarmTelegramService {
//curl "https://api.telegram.org/bot8338000386:AAFuXQv7CCWSiChSj8LW4I725Dvz1Vy--FE/getUpdates"
    // === Config ===
    private val gson = Gson()
    private val enableAlarms = AppSettings.getInstance().enableTelegramAlarms
    private val serverName = AppSettings.getInstance().serverName
    private val botToken = AppSettings.getInstance().telegramBotToken
    private var telegramRecipients = AppSettings.getInstance().telegramRecipients // List<TelegramRecipient>
    private val dataPath = AppSettings.getInstance().dataPath
    private val apiBase = "https://api.telegram.org/bot$botToken"

     fun executeSniffer(): String {
        val snifferScript = "/home/ec2-user/runsniff.sh"  // tu script
        val snifferDir = File(AppSettings.getInstance().dataPath) // carpeta donde vive el script
        return try { 
            val processBuilder =  ProcessBuilder("sudo", "/bin/bash", snifferScript)
            processBuilder.directory(snifferDir)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            WatcherGlobalLog.alarmsDebug("Starting sniffer using [$snifferScript] in dir [$snifferDir]")
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            if (exitCode == 0) {
                val msg = "Sniffer started successfully. Output: $output"
                WatcherGlobalLog.alarmsDebug(msg)
                msg
            } else {
                val msg = "Error starting sniffer: exit code $exitCode. Output: $output"
                WatcherGlobalLog.alarmsDebug(msg)
                msg
            }
        }  catch (e: Exception) {
            e.printStackTrace()
            val msg = "Unexpected error starting sniffer: ${e.message}"
            WatcherGlobalLog.alarmsDebug(msg)
            msg
        }
    }


    // Opcionales en AppSettings:
    // telegramBotName: String (sin @), p.ej. "vierge_bot"
    // telegramAdminChatId: Long? (para avisarte cuando no haya destinatarios resolubles)
    private fun getBotUsernameFromConfig(): String =
        (AppSettings.getInstance().telegramBotName ?: "").trim()
    private fun getAdminChatId(): Long? =
        try { AppSettings.getInstance().telegramAdminChatId } catch (_: Throwable) { null }

    // === HTTP ===
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    // === Estado ===
    private val counterMap = mutableMapOf<AlarmCause, Int>()
    private val tgLogFile = File("$dataPath/telegram_log.json")
    private var tgLog: MutableMap<String, Long> = loadJsonMap(tgLogFile)

    // Cache username -> chatId
    private val usernamesCacheFile = File("$dataPath/usernames_cache.json")
    private var usernamesCache: MutableMap<String, Long> = loadJsonMap(usernamesCacheFile)

    fun sendAlarmTelegram(cause: AlarmCause, errorDescription: String) {
        if (!enableAlarms) {
            WatcherGlobalLog.alarmsDebug("$cause Alarmas Telegram deshabilitadas. Mensaje no enviado.")
            return
        }
        if (botToken.isNullOrBlank()) {
            WatcherGlobalLog.alarmsDebug("$cause Bot token vacío. Mensaje no enviado.")
            return
        }

        // refrescar config por si cambia en runtime
        telegramRecipients = AppSettings.getInstance().telegramRecipients
        WatcherGlobalLog.alarmsDebug("recipients(cfg): ${gson.toJson(telegramRecipients)}")

        val rec = telegramRecipients.firstOrNull { it.alarmCause == cause }
        val list = rec?.recipients ?: emptyList()
         
        // chatIds ya conocidos (del JSON)
        val baseChatIds: List<Long> = list.mapNotNull { it.chatId }

        // usernames a resolver (del JSON)
        val usernames: List<String> = list.mapNotNull { it.username }
            .filter { it.isNotBlank() }

        // 1) Resolver usernames (cache + getChat)
        val resolvedFromUsernames = resolveUsernamesToChatIds(usernames)

        var chatIds = (baseChatIds + resolvedFromUsernames).distinct()

        // 2) Si sigue vacío, intenta cosechar desde getUpdates (si ya hablaron al bot)
        if (chatIds.isEmpty() && usernames.isNotEmpty()) {
            harvestChatIdsFromUpdates(usernames)
            val again = usernames.mapNotNull { usernamesCache[normalizeUsername(it)] }
            chatIds = (baseChatIds + again).distinct()
        }

        // 3) Si aún vacío: generar links de invitación y (opcional) avisar al admin
        if (chatIds.isEmpty()) {
            val inviteLinks = buildInviteLinksFor(usernames)
            WatcherGlobalLog.alarmsDebug(
                "$cause Sin destinatarios resolubles. Pidan iniciar el bot:\n${inviteLinks.joinToString("\n")}"
            )
            getAdminChatId()?.let { admin ->
                val notice = buildString {
                    append("[$serverName] $cause sin destinatarios resolubles.\n")
                    append("Usuarios pendientes:\n")
                    inviteLinks.forEach { append("- $it\n") }
                }
                sendMessageText(admin, notice)
            }
            return
        }

        val counter = counterMap.getOrPut(cause) { 0 } + 1
        counterMap[cause] = counter
        val formattedCounter = counter.toString().padStart(3, '0')

        val text = buildPlainTextMessage(
            titleCounter = formattedCounter,
            cause = cause,
            errorDescription = errorDescription
        )

        var successCount = 0
        chatIds.forEach { chatId ->
            val ok = sendMessageText(chatId, text)
            if (ok) successCount++
        }

        if (successCount > 0) {
            tgLog[cause.name] = System.currentTimeMillis()
            saveJsonMap(tgLogFile, tgLog)
            WatcherGlobalLog.alarmsDebug("$cause Telegram enviado a $successCount/${chatIds.size} destinatarios.")
        } else {
            WatcherGlobalLog.alarmsDebug("$cause Fallo al enviar Telegram a todos los destinatarios.")
        }
    }

    // ---------- Helpers de invitación ----------
    private fun buildInviteLinksFor(usernames: List<String>): List<String> {
        val bot = getBotUsernameFromConfig()
        return usernames.map { raw ->
            val u = raw.trim().removePrefix("@")
            if (bot.isEmpty()) {
                "username:$u -> (Config faltante: telegramBotName)"
            } else {
                val startParam = URLEncoder.encode(u, StandardCharsets.UTF_8.name())
                "https://t.me/$bot?start=$startParam"
            }
        }
    }

    // ---------- Cosecha de chat_id desde getUpdates ----------
    private fun harvestChatIdsFromUpdates(targetUsernames: List<String>) {
        if (targetUsernames.isEmpty()) return
        val wanted = targetUsernames.map { it.trim().removePrefix("@").lowercase() }.toSet()

        val req = Request.Builder()
            .url("$apiBase/getUpdates")
            .get().build()

        try {
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    WatcherGlobalLog.alarmsDebug("getUpdates HTTP ${resp.code}: ${resp.body?.string()?.take(400)}")
                    return
                }
                val body = resp.body?.string().orEmpty()
                val items = """"message"\s*:\s*\{.*?"from"\s*:\s*\{.*?"username"\s*:\s*"([^"]+)".*?"chat"\s*:\s*\{.*?"id"\s*:\s*(-?\d+)""".toRegex(RegexOption.DOT_MATCHES_ALL)
                var found = 0
                items.findAll(body).forEach { m ->
                    val uname = m.groupValues.getOrNull(1)?.lowercase() ?: return@forEach
                    val cid = m.groupValues.getOrNull(2)?.toLongOrNull() ?: return@forEach
                    if (uname in wanted) {
                        val key = "@$uname"
                        usernamesCache[key] = cid
                        found++
                        WatcherGlobalLog.alarmsDebug("Harvested chatId $cid para $key")
                    }
                }
                if (found > 0) saveJsonMap(usernamesCacheFile, usernamesCache)
            }
        } catch (e: Exception) {
            WatcherGlobalLog.alarmsDebug("getUpdates EX: ${e.message}")
        }
    }

    // ---------- Resolución de usernames ----------
    private fun resolveUsernamesToChatIds(usernames: List<String>): List<Long> {
        val result = mutableListOf<Long>()
        usernames.forEach { raw ->
            val uname = normalizeUsername(raw)
            if (uname.isEmpty()) return@forEach

            // 1) Cache primero
            val cached = usernamesCache[uname]
            if (cached != null) {
                result.add(cached)
                return@forEach
            }

            // 2) Intentar getChat con @username
            // Usuarios privados: solo funcionará si YA iniciaron el bot;
            // Grupos/canales con @ público: funciona.
            val chatId = getChatIdByUsername(uname)
            if (chatId != null) {
                usernamesCache[uname] = chatId
                result.add(chatId)
            } else {
                WatcherGlobalLog.alarmsDebug("No se pudo resolver username $uname. ¿Inició el bot con /start?")
            }
        }
        if (result.isNotEmpty()) saveJsonMap(usernamesCacheFile, usernamesCache)
        return result
    }

    private fun normalizeUsername(u: String?): String {
        if (u.isNullOrBlank()) return ""
        val trimmed = u.trim()
        return if (trimmed.startsWith("@")) trimmed else "@$trimmed"
    }

    private fun getChatIdByUsername(usernameAt: String): Long? {
        val url = "$apiBase/getChat?chat_id=${usernameAt}"
        val req = Request.Builder().url(url).get().build()
        return try {
            http.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    WatcherGlobalLog.alarmsDebug("getChat $usernameAt HTTP ${resp.code}: ${body.take(200)}")
                    return null
                }
                val idRegex = """"id"\s*:\s*(-?\d+)""".toRegex()
                val m = idRegex.find(body) ?: return null
                m.groupValues.getOrNull(1)?.toLongOrNull()
            }
        } catch (e: Exception) {
            WatcherGlobalLog.alarmsDebug("getChat EX $usernameAt: ${e.message}")
            null
        }
    }

    // ---------- Helpers de mensaje ----------
    private fun buildPlainTextMessage(
        titleCounter: String,
        cause: AlarmCause,
        errorDescription: String
    ): String {
        val time = getCurrentTimeCDMX()
        val sshIp = getSshIp()
        val maxDetails = 1500
        val details = if (errorDescription.length > maxDetails)
            errorDescription.take(maxDetails) + "…" else errorDescription

        return buildString {
            append("AB [$serverName]-[$titleCounter] | ${cause.title}\n")
            append("Hora(CDMX): $time\n")
            append("Causa: ${cause.name}\n")
            append("Msg: ${stripNewlines(cause.message)}\n")
            append("Detalles: ${stripNewlines(details)}\n")
            append("SSH IP: $sshIp")
        }
    }

    private fun stripNewlines(s: String?): String =
        (s ?: "").replace("\r", " ").replace("\n", " ").trim()

    private fun getCurrentTimeCDMX(): String {
        val tz = TimeZone.getTimeZone("America/Mexico_City")
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply { timeZone = tz }
        return fmt.format(Date())
    }

    private fun getSshIp(): String {
        return try {
            val proc = Runtime.getRuntime().exec("who")
            proc.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
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
            e.message?.toString() ?: "SIN IP"
        }
    }

    // ---------- Envío ----------
    private fun sendMessageText(chatId: Long, text: String): Boolean {
        val chunks = text.chunked(4096)
        var allOk = true
        chunks.forEach { part ->
            val body = FormBody.Builder()
                .add("chat_id", chatId.toString())
                .add("text", part)
                .build()
            val req = Request.Builder().url("$apiBase/sendMessage").post(body).build()
            try {
                http.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        allOk = false
                        WatcherGlobalLog.alarmsDebug("sendMessage chatId=$chatId HTTP ${resp.code} body=${bodyStr.take(400)}")
                    }
                }
            } catch (e: Exception) {
                allOk = false
                WatcherGlobalLog.alarmsDebug("sendMessage EX chatId=$chatId: ${e.message}")
            }
        }
        return allOk
    }

    // ---------- JSON util ----------
    private fun loadJsonMap(file: File): MutableMap<String, Long> {
        return try {
            if (file.exists()) {
                val type = object : TypeToken<MutableMap<String, Long>>() {}.type
                gson.fromJson(file.readText(), type) ?: mutableMapOf()
            } else mutableMapOf()
        } catch (ex: Exception) {
            WatcherGlobalLog.alarmsDebug("loadJsonMap_EX ${ex.message}")
            mutableMapOf()
        }
    }

    private fun saveJsonMap(file: File, map: MutableMap<String, Long>) {
        try {
            file.writeText(gson.toJson(map))
        } catch (ex: Exception) {
            WatcherGlobalLog.alarmsDebug("saveJsonMap_EX ${ex.message}")
        }
    }
}

// === Modelos / Config ===

data class TelegramRecipient(
    var alarmCause: AlarmCause,
    var recipients: List<TgRecipient> = emptyList()
)

data class TgRecipient(
    val username: String? = null,  // opcional, p.ej. "rolildm" (con o sin @ en JSON)
    val chatId: Long? = null       // opcional, p.ej. 7453839707
)

