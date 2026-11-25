package com.shared.commons

import com.viergegroup.swmain.managers.AlarmRecipient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.LogLevel
import com.viergegroup.swmain.entities.catalogs.OperationId

import org.springframework.beans.BeanUtils
import java.io.*
import java.util.* 
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
 import com.viergegroup.swmain.managers.TelegramRecipient

class AppSettings {

    interface IAppSettings { 
        fun onConfigurationUpdated() 
    }  

    var defaultAuthorizer = Authorizer.PROSA
    var dataPathLinux = "/etc/swvierge/data/" //@LILI 
    var dataPathMicrosoft = "C:\\swvierge"
    var dataPath = dataPathLinux 
    var restartServicesHour = 3  
    var restartServicesMinutes = 30  
    var maskPCI = true  
    var deleteThresholdPercent : Double= 98.0
    var deleteDaysOld : Long= 7
    var enableAlarms : Boolean=false
    var enableTelegramAlarms : Boolean=false
    var  telegramBotToken: String=""
    var telegramRecipients =listOf<TelegramRecipient>() // Lista de AlarmRecipient
    var hsmEndpoints: List<HsmEndpoint> = listOf(
        HsmEndpoint("8.18.37.186", 16305),
        HsmEndpoint("131.226.218.61", 16605),
        HsmEndpoint("131.226.218.61", 23405)
    ) 
     var  telegramBotName: String="vierge_bot"
     var  telegramAdminChatId: Long=7777777

    var  deleteRootDirectory: String=""
    var  deleteBucketDirectory: String=""
    var initScript = "/home/ec2-user/iniswtrp.sh"

    var maxLogMetaDataMessageLength = 50000  
    var responseCodeReverse = "68"
    var myHsmChainCrt = "$dataPathLinux/myhsm/myhsmca.jks"
    var myHsmMYHSMClient = "$dataPathLinux/myhsm/client_MYHSM.jks"
    var myHsmMYHSMKey = "$dataPathLinux/myhsm/client_MYHSM.key"
    var vaultDir = "/mnt/commonfiles-taptophone/vault"

    var waitForReception = true
    var sendLogOnWakeUp = true
    var showOnlyISOMessaging = true
    var canReboot = true

    var calculateKCVRsa = true
    var dukptEncryptionByParts = false
    var encryptDB = true
    var hexPaddingChar = "F"

    var useTDESPadding = false
    var vUrl = ""
    var isVaultConsole = false
    var vVersion = ""
    var vToken = ""
    var swVGVersion = ""
    var validateRequests=false
    var commReadExceptionThreshold : Int= 7
    var commOpenExceptionThreshold : Int= 7
    var commWriteExceptionThreshold : Int= 7
    
    var oauthPrimaryRedirectUri = "http://localhost:8080/login/oauth2/code/api-client-oidc"
    var oauthSecondaryRedirectUri = "http://localhost:8080/authorized"
    var oauthProviderUri = "http://localhost:8080"
    var alarmsRecipients=listOf<AlarmRecipient>() // Lista de AlarmRecipient
    var alarmsEmail = ""
    var alarmsPwd = ""

    

    var clientWaitAnswerTimeOut: Long = 60000
    var clientWaitDeltaSeconds: Long = 1000
    var echoServiceMaxWaitSeconds: Long = 0L
 
    var configPath = "$dataPath/config.json"
    var maxFileSize: Long = 40000L
    var forceReverses = false
    var forceDeleteReverses = false
    var pandoraLocal = false
    var isDocumentVault = false

    var forceIsoTakeDefaultValues = false
    var emvTagDescriptorsFilePath = "F"
 
    var hostIp = ""
    var hostPort = 0
    var serverName : String="Dev"

    var isLinuxServer = true

    var pandoraUrl = ""
    var evoUrl = "https://testcodi.multipay.mx:28263/token"
    var evoClientId = "testClientId"
    var evoClientSecret = "testClientSecret"
     var evoKsid = "08D02"

     


    var resourcesPath = "C:\\Git\\switch prosa\\src\\main\\resources\\"
    var sendDynamicValues = false
    var databaseEnabled=false
    var simulateHSM=false
    var databaseUrl:String=""
    var databaseUsername:String=""
    var databasePassword:String="" 
    var data: AppSettingsData = AppSettingsData()
    var logsGlobalLevel= listOf<String>()
    var clearConsolePerOperation=false
    var minutesBetweenReboots = 11111117

    fun logGlobalIsActive(logLevel: LogLevel):Boolean{
        return logsGlobalLevel.contains(logLevel.code)
    }
 
     fun getBasePath(): String {
        return if (System.getProperty("os.name").toLowerCase().contains("win")) {
            dataPathMicrosoft
        } else {
            dataPathLinux
        }
    } 
    fun initialize(){
    
        try {
            while (isReadingAppSettings)
                isReadingAppSettings = true

            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            printlnWithColor("OS_NAME $osName", ANSI_ORANGE)
            val isMicrosoftOS = osName.contains("windows") || osName.contains("microsoft")
            if (isMicrosoftOS) {
                dataPath = dataPathMicrosoft
                configPath = "$dataPathMicrosoft/config.json"
                configPath = configPath.replace("/", "\\")
            } else{

                dataPath = dataPathLinux
                configPath = "$dataPathLinux/config.json" 
            }
            isLinuxServer=!isMicrosoftOS
            printlnWithColor("isMicrosoftOS ${isMicrosoftOS} isLinuxServer $isLinuxServer",ANSI_ORANGE)
            printlnWithColor("READ_FROM $dataPath", ANSI_ORANGE)
            printlnWithColor("READ_FROM $configPath", ANSI_ORANGE)

            var json = String()
            data = AppSettingsData()
            try {
                val myObj = File(configPath)
                if (myObj.exists()) {
                    lastModificationTime = myObj.lastModified()
                    val scanner = Scanner(myObj)
                    while (scanner.hasNextLine()) {
                        json += scanner.nextLine()
                    }
                    scanner.close()
                }
                val newdata = Gson().fromJson(json, AppSettingsData::class.java)
                printlnWithColor(getUpdatedFields(newdata), ANSI_VIOLET)
                data=newdata
                serverName=data.serverName
            } catch (ex: Exception) {
                printlnWithColor("ERROR_READING_DATA_CONFIG  ${ex.message} \n $dataPath", ANSI_ORANGE)
            }
            updatePaths()
            BeanUtils.copyProperties(data, this)
            initializeAuthorizerConfigurations()
            initializeHostConfigurations()
            configPathStatic = configPath
        } catch (ex: Exception) {
            printlnWithColor("config ex " + ex.message, ANSI_ORANGE)
        }
        printlnWithColor("SETTINGS READED SUCCESS", ANSI_ORANGE)


        isReadingAppSettings = false
        appSettings = this



        printlnWithColor("\nGLOBAL LOGS LEVELS:\n", ANSI_VIOLET)

        var activeLogs=""
        for(value in LogLevel.values()){
            if(logGlobalIsActive(value)){
                activeLogs+="$value,"
            }

        }

        printlnWithColor("$activeLogs", ANSI_VIOLET)

        for(host in HostType.values()){
            activeLogs=""
            printlnWithColor("\nHOST LOGS LEVELS ($host):\n", ANSI_VIOLET)
            for(value in LogLevel.values()){
                if(getHostConfig(host).logHostIsActive(value)){
                    activeLogs+="$value,"

                }
            }
            printlnWithColor("[$activeLogs]", ANSI_VIOLET)

        }


        for(host in HostType.values()){
            activeLogs=""
            printlnWithColor("\nOPERATION LOGS LEVELS ($host):\n", ANSI_VIOLET)
            for(value in LogLevel.values()){
                if(getHostConfig(host).logsOperationIsActive(value)){
                    activeLogs+="$value,"

                }
            }
            printlnWithColor("[$activeLogs]", ANSI_VIOLET)

        }

    
    }
    private constructor() {
    
        initialize()

    }
    private constructor(dataPathLinux:String,dataPathWin:String) {
        this. dataPathLinux=dataPathLinux
        this.  dataPathMicrosoft=dataPathWin
        printlnWithColor("constructor_dataPathLinux [${this.dataPathLinux}]", ANSI_ORANGE)
        printlnWithColor("constructor_dataPathMicrosoft [${this.dataPathMicrosoft}]", ANSI_ORANGE)
        initialize()

    }

    private fun updatePaths() {
        data.logsPath = dataPath + data.logsPath
        data.dukptPath = dataPath + data.dukptPath
        data.rsaPath = dataPath + data.rsaPath
        data.emvTagDescriptorsFilePath = dataPath + data.emvTagDescriptorsFilePath
        data.myHsmChainCrt = dataPath + data.myHsmChainCrt
        data.myHsmMYHSMClient = dataPath + data.myHsmMYHSMClient
        data.myHsmMYHSMKey = dataPath + data.myHsmMYHSMKey
        data.defaultRsaFilePath = dataPath + data.defaultRsaFilePath
        data.isoDescriptorsPath = dataPath + data.isoDescriptorsPath
        data.isoExpressionsMapPath = dataPath + data.isoExpressionsMapPath
        data.dynamicParametersPath = dataPath + data.dynamicParametersPath
        data.baseTransactionDataFilePath = dataPath + data.baseTransactionDataFilePath
        defaultAuthorizerStatic = data.defaultAuthorizer

        if (!isLinuxServer) {
            data.myHsmChainCrt = data.myHsmChainCrt.replace("/", "\\")
            data.myHsmMYHSMClient = data.myHsmMYHSMClient.replace("/", "\\")
            data.myHsmMYHSMKey = data.myHsmMYHSMKey.replace("/", "\\")
            data.emvTagDescriptorsFilePath = data.emvTagDescriptorsFilePath.replace("/", "\\")
            data.dynamicParametersPath = data.dynamicParametersPath.replace("/", "\\")
            data.isoExpressionsMapPath = data.isoExpressionsMapPath.replace("/", "\\")
            data.baseTransactionDataFilePath = data.baseTransactionDataFilePath.replace("/", "\\")
            data.logsPath = data.logsPath.replace("/", "\\")
            data.isoDescriptorsPath = data.isoDescriptorsPath.replace("/", "\\")
            data.dukptPath = data.dukptPath.replace("/", "\\")
            data.defaultRsaFilePath = data.defaultRsaFilePath.replace("/", "\\")
            data.rsaPath = data.rsaPath.replace("/", "\\")
            isLinuxServer = false
        }
    }

    private fun initializeAuthorizerConfigurations() {
        println("initializeAuthorizerConfigurations")
        for (authorizer in Authorizer.values()) {
            try {
                val data = data.authorizerConfigurations.firstOrNull { it.authorizerId == authorizer }
                if (data != null) {
                    data.injectDataPath(dataPath, isLinuxServer)
                    authorizerConfigurationMap[authorizer] = data
                }
            } catch (e: Exception) {
                printlnWithColor("ERROR INITIALIZING APP SETTINGS OF $authorizer: ${e.message}", ANSI_ORANGE)
            }
        }
    }

    private fun initializeHostConfigurations() {
        for (hostType in HostType.values()) {
            try {
                val data = data.hostConfigurations.firstOrNull { it.hostName == hostType.hostTypeName }
                if (data != null) {
                    val authorizerConf = authorizerConfigurationMap[hostType.authorizer]
                    if (authorizerConf != null) {
                        data.injectDataPath(dataPath, authorizerConf.logsPath, authorizerConf.isEnabled, isLinuxServer)
                        hostConfigurationMap[hostType] = data
                    } else {
                        printlnWithColor("ERROR AUTHORIZER \"${hostType.authorizer}\" CONF IS NOT DEFINED", ANSI_ORANGE)
                    }
                }
            } catch (e: Exception) {
                printlnWithColor("ERROR INITIALIZING APP SETTINGS OF $hostType: ${e.message}", ANSI_ORANGE)
            }
        }
    }


    private fun getUpdatedFields(updated: AppSettingsData): String {
        val updatedFields = mutableListOf<String>()
        val properties = AppSettingsData::class.memberProperties
        val original = data // Suponiendo que `data` es una instancia de `AppSettingsData`

        for (property in properties) {
            val originalValue = (property as KProperty1<AppSettingsData, *>).get(original)
            val updatedValue = property.get(updated)

            if (originalValue != updatedValue) {
                if (originalValue is List<*> && updatedValue is List<*>) {
                    for (i in originalValue.indices) {
                        val originalItem = originalValue[i]
                        val updatedItem = updatedValue.getOrNull(i)
                        when (originalItem) {
                            is AppAuthorizerSettingsData -> {
                                updatedFields.addAll(originalItem.getUpdatedFields(updatedItem as AppAuthorizerSettingsData).lines().map { "${property.name}[$i].$it" })
                            }
                            is AppHostSettingsData -> {
                                updatedFields.addAll(originalItem.getUpdatedFields(updatedItem as AppHostSettingsData).lines().map { "${property.name}[$i].$it" })
                            }
                            else -> {
                                updatedFields.add("${property.name}[$i]: $originalItem => $updatedItem")
                            }
                        }
                    }
                } else {
                    updatedFields.add("${property.name}: $originalValue => $updatedValue")
                }
            }
        }

        return updatedFields.joinToString(separator = "\n")
    }

    companion object {
        var isReadingAppSettings = false
        var configPathStatic = "/home/ubuntu/switchprosa/data/config.json"
        private var appSettings: AppSettings? = null
        val hostConfigurationMap = mutableMapOf<HostType, AppHostSettingsData>()
        val authorizerConfigurationMap = mutableMapOf<Authorizer, AppAuthorizerSettingsData>()
        var defaultAuthorizerStatic = Authorizer.PROSA
        var pandoraVersion = "1.0.7"

        private var listeners = mutableListOf<IAppSettings>()

        private const val ANSI_RESET = "\u001B[0m"
        private const val ANSI_ORANGE = "\u001B[33m" // An approximation using yellow
        private const val ANSI_VIOLET = "\u001B[35m" // ANSI escape code for magenta (which is a form of violet)

        fun printlnWithColor(message: String, color: String) {
           println("$color$message$ANSI_RESET")
        }

        fun getAuthorizerConfig(hostType: HostType): AppAuthorizerSettingsData {
            return getAuthorizerConfig(hostType.authorizer)
        }

        fun getAuthorizerConfig(authorizer: Authorizer): AppAuthorizerSettingsData {
            val authId = authorizer
            if (authId == Authorizer.Undefined) {
                defaultAuthorizerStatic = authId
            }
            if (appSettings == null || configWasUpdated()) appSettings = AppSettings()
            return authorizerConfigurationMap[authId] ?: AppAuthorizerSettingsData()
        }
        fun getInstance(dataPathLinux:String,dataPathWin:String): AppSettings {
            if (appSettings == null || configWasUpdated()) appSettings = AppSettings(dataPathLinux,dataPathWin)
          //  appSettings!!.dataPathLinux=dataPathLinux
          //  appSettings!!.dataPathMicrosoft=dataPathWin
          //  appSettings!!. configPath = "${appSettings!!.getBasePath()}/config.json"
          //  printlnWithColor("init_configPath [${appSettings!!.configPath}]", ANSI_ORANGE)

            return appSettings!!
        }

        fun getInstance(): AppSettings {
            if (appSettings == null || configWasUpdated()) appSettings = AppSettings()
            return appSettings!!
        }

        fun getHostsList(): List<AppHostSettingsData> {
            val listHost = mutableListOf<AppHostSettingsData>()
            getInstance()
            for ((_, item) in hostConfigurationMap) {
                listHost.add(item)
            }
            return listHost
        }

        fun getHostConfig(hostType: HostType): AppHostSettingsData {
            if (appSettings == null || configWasUpdated()) appSettings = AppSettings()
            return hostConfigurationMap[hostType] ?: AppHostSettingsData()
        }    

        fun init(dataPathLinux:String,dataPathWin:String) {
            printlnWithColor("init_dataPathLinux [${dataPathLinux}]", ANSI_ORANGE)
            printlnWithColor("init_dataPathWin [${dataPathWin}]", ANSI_ORANGE)
            printlnWithColor("APPSETTINGS IS INITIALIZING....", ANSI_ORANGE)
            getInstance(dataPathLinux,dataPathWin)
        }

        private var lastModificationTime: Long = 0

        fun addListener(listener: IAppSettings) {
            listeners.add(listener)
        }

        fun notifyReconfiguration() {
            for (listener in listeners) {
                listener.onConfigurationUpdated()
            }
        }

        fun configWasUpdated(): Boolean {
            val myObj = File(configPathStatic)
            val reconfigurated = if (myObj.exists()) {
                myObj.lastModified() != lastModificationTime
            } else false

            if (reconfigurated) notifyReconfiguration()

            return reconfigurated
        }
    }

    fun saveSettingsData(data: AppSettingsData): AppSettingsData {
        val dataPath = configPath
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(data)
            val writer = PrintWriter(dataPath)
            writer.print(jsonString)
            writer.close()
            this.data = data
        } catch (ex: Exception) {
            printlnWithColor("EXCEPTION: " + ex.message, ANSI_ORANGE)
        }
        return data
    }
}
