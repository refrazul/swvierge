package com.shared.commons

import com.google.gson.GsonBuilder
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.LogLevel
import com.viergegroup.swmain.managers.AlarmRecipient
import com.viergegroup.swmain.managers.TelegramRecipient


class AppSettingsData {
    var serverName:String=""  
   //var dataPathLinux = ""
    var isDocumentVault = false 
    var logsGlobalLevel= listOf<String>() 
    var clearConsolePerOperation=false
    var maxLogMetaDataMessageLength = 50000
    var authorizerConfigurations= mutableListOf<AppAuthorizerSettingsData>()
    var hostConfigurations= mutableListOf<AppHostSettingsData>()
    var defaultAuthorizer= Authorizer.PROSA
    var enableNetworkLogs=false
    var minutesBetweenReboots = 11111117
    var emulateErrors=listOf<ErrorEmulateSettingData>()
    var canReboot = true
    var vaultDir = "/mnt/commonfiles-taptophone/vault"
    var initScript = "/home/ec2-user/iniswtrp.sh"
   
   var hsmEndpoints: List<HsmEndpoint> = listOf(
        HsmEndpoint("8.18.37.186", 16305),
        HsmEndpoint("131.226.218.61", 16605), 
        HsmEndpoint("131.226.218.61", 23405)
    )  
    var enableTelegramAlarms : Boolean=false
    var  telegramBotToken: String="8338000386:AAFuXQv7CCWSiChSj8LW4I725Dvz1Vy--FE"
    var telegramRecipients =listOf<TelegramRecipient>() // Lista de TelegramRecipient



    var restartServicesHour = 3   
    var restartServicesMinutes = 30  
    var isoExpressionsMapPath = "/isodescriptors/expressions_map.json"
    var responseCodeReverse = "68"
    var baseTransactionDataFilePath= "/transactions/openpay_base_transaction_data.json"
    var emvTagDescriptorsFilePath = "F"
    var pandoraLocal = false
    var swVGVersion=""
    var oauthPrimaryRedirectUri="http://localhost:8080/login/oauth2/code/api-client-oidc"
    var oauthSecondaryRedirectUri="http://localhost:8080/authorized"
    var oauthProviderUri="http://localhost:8080"
    var simulateHSM=false
    var maskPCI = true  

    var databaseEnabled=false
    var databaseUrl:String=""
    var databaseUsername:String=""
    var databasePassword:String=""
    var oauthClientSecret="L1L_a4a"
    var oauthApiClientId="api-client"
    var oauthScope="api.read"
    var pandoraUrl = ""
    var evoUrl = "https://testcodi.multipay.mx:28263/token"
    var evoClientId = "testClientId"
    var evoClientSecret = "testClientSecret"
     var evoKsid = "08D02"

    var vUrl = ""
    var isVaultConsole = false
    var vVersion = ""
    var vToken = ""


    var oauthScopes = mutableListOf(
        "api.read"
        ,"api.write"
    )

    var maxFileSize: Long = 40000L
    var echoServiceMaxWaitSeconds: Long = 0L

    var sendLogOnWakeUp = true
    var waitForReception = true

    var myHsmChainCrt = "/myhsm/CA_MYHSM_chain.crt"
    var myHsmMYHSMClient = "/myhsm/client_MYHSM.crt"
    var myHsmMYHSMKey = "/myhsm/client_MYHSM.key"

    var logsPath = "/logs"
    var dukptPath = "/dukpt" 
    var rsaPath = "/rsa"
    var defaultRsaFilePath = "/rsa/A000VIE1"
    var isoDescriptorsPath = "/isodescriptors/openpay_iso_descriptor.json"
    var dynamicParametersPath = "/dynamic/parameters.json"
    var deleteThresholdPercent : Double= 98.0
    var deleteDaysOld : Long= 7
    var  deleteRootDirectory: String=""
    var  deleteBucketDirectory: String=""
    var alarmsRecipients=listOf<AlarmRecipient>() // Lista de AlarmRecipient
   



    var commReadExceptionThreshold : Int= 7
    var commOpenExceptionThreshold : Int= 7
    var commWriteExceptionThreshold : Int= 7

    var enableAlarms : Boolean=false
    var alarmsEmail = ""
    var alarmsPwd = ""

    fun toJsonLog(): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        //  val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    } 
    fun toJson(): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        //  val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }
}