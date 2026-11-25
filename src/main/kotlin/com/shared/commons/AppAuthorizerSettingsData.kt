package com.shared.commons
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class AppAuthorizerSettingsData {
    var authorizerId: Authorizer = Authorizer.PROSA
    var isEnabled = false
    var defaultDataPath = "/home/ec2-user/swvierge/data"
    var logsPath = "$defaultDataPath/logs"
    var dukptPath = "$defaultDataPath/dukpt"
    var defaultRsaFilePath = "$defaultDataPath/rsa/A000VIE1"
    var dynamicParametersPath = "$defaultDataPath/dynamic/parameters.json"
   // var baseTransactionDataFilePath= "/transactions/openpay_base_transaction_data.json"
    var isoExpressionsMapPath = "$defaultDataPath/isodescriptors/expressions_map.json"
    var isoDescriptorsPath = "$defaultDataPath/isodescriptors/expressions_map.json"
    var rsaPath = "$defaultDataPath/rsa"
    var isLinux=false

    
    var mustInjectExternEx = false
     fun getUpdatedFields(updated: AppAuthorizerSettingsData): String {
        val updatedFields = mutableListOf<String>()
        val properties = AppAuthorizerSettingsData::class.memberProperties
        val original = this
        for (property in properties) {
            val originalValue = (property as KProperty1<AppAuthorizerSettingsData, *>).get(original)
            val updatedValue = property.get(updated)
            if (originalValue != updatedValue) {
                updatedFields.add("${property.name}: $originalValue => $updatedValue")
            }
        }
        return updatedFields.joinToString(separator = "\n")
    }


    fun injectDataPath(dataPath:String,isLinux:Boolean){
      //  val dateStr: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
        println("injectDataPath : ${dataPath}  isLinux $isLinux")

        this.defaultDataPath=dataPath
        this.isLinux=isLinux
        dukptPath=dataPath+dukptPath
        defaultRsaFilePath=dataPath+defaultRsaFilePath
        dynamicParametersPath=dataPath+dynamicParametersPath
    //    baseTransactionDataFilePath=dataPath+baseTransactionDataFilePath
        logsPath=dataPath+logsPath
        isoExpressionsMapPath=dataPath+isoExpressionsMapPath
        rsaPath=dataPath+rsaPath

        isoDescriptorsPath=dataPath+isoDescriptorsPath

        if (!isLinux) {
            dukptPath= dukptPath.replace("/", "\\")
            defaultRsaFilePath = defaultRsaFilePath.replace("/", "\\")
            dynamicParametersPath = dynamicParametersPath.replace("/", "\\")
          //  baseTransactionDataFilePath = baseTransactionDataFilePath.replace("/", "\\")
            logsPath = logsPath.replace("/", "\\")
            isoExpressionsMapPath = isoExpressionsMapPath.replace("/", "\\")
            isoDescriptorsPath = isoDescriptorsPath.replace("/", "\\")
            rsaPath = rsaPath.replace("/", "\\")
            isoExpressionsMapPath = isoExpressionsMapPath.replace("/", "\\")

        }
        println("dukptPath : ${dukptPath} ")
        println("defaultRsaFilePath : ${defaultRsaFilePath} ")
        println("dynamicParametersPath : ${dynamicParametersPath} ")
       // println("baseTransactionDataFilePath : ${baseTransactionDataFilePath} ")
        println("isoDescriptorsPath : ${isoDescriptorsPath} ")
        println("rsaPath : ${rsaPath} ")
        println("isoExpressionsMapPath : ${isoExpressionsMapPath} ")



    }
}