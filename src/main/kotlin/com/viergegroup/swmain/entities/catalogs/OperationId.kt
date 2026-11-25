package com.viergegroup.swmain.entities.catalogs

import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.security.BankRsaKey
import com.viergegroup.crypto.security.DukptData
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.swmain.dbmodels.ClientActivityDBData
import com.viergegroup.swmain.dtos.clientactivity.ClientActivityDataListResponse
import com.viergegroup.swmain.dtos.clientactivity.ClientActivityFilterDto
import com.viergegroup.swmain.dtos.clientactivity.TotalClientActivityPagesResponse
import com.viergegroup.swmain.dtos.clientactivity.UpsertClientActivityDataResponse
import com.viergegroup.swmain.dtos.comm.CommHostRequestDto
import com.viergegroup.swmain.dtos.comm.ConnectToHostResponseDto
import com.viergegroup.swmain.dtos.comm.DisconnectHostResponseDto
import com.viergegroup.swmain.dtos.comm.HostStatusResponseDto
import com.viergegroup.swmain.dtos.config.ConfigResponse
import com.viergegroup.swmain.dtos.logs.*
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.dtos.config.JsonDto
import com.viergegroup.swmain.dtos.gateway.*
import com.viergegroup.swmain.dtos.iso.DescribeISOAsciiResponse
import com.viergegroup.swmain.dtos.iso.DescribeISORequestDto
import com.viergegroup.swmain.dtos.iso.ToIsoAsciiResponseDto
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import com.viergegroup.swmain.dtos.pandora.PandoraResponse
import com.viergegroup.swmain.dtos.shared.PagesResponse
import com.viergegroup.swmain.dtos.transactions.*
import com.viergegroup.swmain.dtos.vault.BankRsaKeyResponse
import com.viergegroup.swmain.dtos.vault.DukptDataResponse
import kotlin.reflect.KClass
import com.viergegroup.crypto.pandora.managers.VaultDtos.ReadKeyRequest
import com.viergegroup.crypto.pandora.managers.VaultDtos.CalculateHashResponse
import com.viergegroup.swmain.managers.AlarmCauseData
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ResponseReadClientCredData


import com.viergegroup.swmain.dbservices.SystemStatsDBService
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import com.viergegroup.swmain.dtos.sys.SystemStatsRequestDto
import com.viergegroup.swmain.dtos.sys.SystemStatsResponseDto
import com.viergegroup.swmain.dtos.sys.SystemStatsInsertRequestDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsMetaResDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsDBMetaDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsMetaReqDto

import kotlin.math.min


enum class OperationId(
    val operationId: Int,
    val operationName: String,
    val apiPath: String,
    val moduleId: ModuleId,
    val requestDtoClass: KClass<out Any>,
    val responseDtoClass: KClass<out Any>,
    val isAccessible:Boolean=true
) {
    Undefined(0XFFFF, "Undefined", "", ModuleId.Undefined, Any::class, Any::class,),
    All(0, "All", "", ModuleId.All, Any::class, Any::class),
    GetHostStatus(0x0101, "GetHostStatus", "/comm/host-status", ModuleId.Comm, CommHostRequestDto::class, HostStatusResponseDto::class),
    ConnectHost(0x0102, "ConnectHost", "/comm/connect-to-host", ModuleId.Comm, CommHostRequestDto::class, ConnectToHostResponseDto::class),
    DisconnectHost(0x0103, "DisconnectHost", "/comm/disconnect-host", ModuleId.Comm, CommHostRequestDto::class, DisconnectHostResponseDto::class),
 

    HostOperation(0x0104, "InitHosts", "", ModuleId.Comm, Any::class, Any::class,false),
    UsersCheck(0x0105, "UsersCheck", "/users/check", ModuleId.Comm, Any::class, Any::class),


    CommunicationTest(0x0106, "CommunicationTest", "/comm/communication-test", ModuleId.Comm, CommHostRequestDto::class, SendTransactionResponse::class),
    RebootSystem(0x0107, "RebootSystem", "/comm/reboot", ModuleId.Comm, CommHostRequestDto::class, ConnectToHostResponseDto::class),
    SwRestart(0x0108, "SwRestart", "/sw/restart", ModuleId.Comm, Any::class, Any::class),
    SwReboot(0x0109, "SwReboot", "/sw/reboot", ModuleId.Comm, Any::class, Any::class),
    FunKeys(0x010B, "FunKeys", "/comm/fun-keys", ModuleId.Comm, CommHostRequestDto::class, SendTransactionResponse::class),
    SendAlarm(0x010A, "SendAlarm", "/comm/send-alarm", ModuleId.Comm, AlarmCauseData::class, ConnectToHostResponseDto::class),

 
    Transaction(0x0201, "Transaction", "/transactions/send-transaction", ModuleId.Transactions, TransactionTerminalDto::class, SendTransactionResponse::class),
    SearchTxn2(0x0202, "KeyLoad", "", ModuleId.Transactions, TransactionTerminalDto::class, Any::class),
    SearchTransaction(0x0203, "SearchTransaction", "/transactions/search-txn", ModuleId.Transactions, TransactionTerminalDto::class,SearchTransactionResponse ::class),


    GatewayTransaction(0x0B01, "GatewayTransaction", "/gatewayevo/transaction", ModuleId.Gateway, GatewayTransactionDto::class, GatewayTransactionResponse::class),
     GatewayTransaction2(0x0B05, "GatewayTransaction2", "/", ModuleId.Gateway, GatewayTransactionDto::class, GatewayTransactionResponse::class),
     GatewayTransaction3(0x0B06, "GatewayTransaction3", "", ModuleId.Gateway, GatewayTransactionDto::class, GatewayTransactionResponse::class),

    EncodeB64(0x0B02, "EncodeB64", "/gateway/encode-base64", ModuleId.Gateway, TransactionTerminalDto::class, GatewayEncodeB64Response::class),
    DecodeB64(0x0B03, "DecodeB64", "/gateway/decode-base64", ModuleId.Gateway, GatewayBase64Dto::class, GatewayDecodeB64Response::class),
    GatewaySearchTransaction(0x0B04, "GatewaySearchTransaction", "/gateway/search-txn", ModuleId.Gateway, TransactionTerminalDto::class,SearchTransactionResponse ::class),
 
 

    GetLogs(0x0301, "GetLogs", "/logs/get-log-data-list", ModuleId.Logs, LogsFilterDto::class, LogsDataListResponse::class),
    GetTotalLogPages(0x0302, "GetTotalLogPages", "/logs/get-log-data-pages", ModuleId.Logs, LogsFilterDto::class, PagesResponse::class),
    GenerateLogs(0x0303, "GenerateLogs", "/logs/generate-logs", ModuleId.Logs, GenerateTxnsDto::class, String::class),
    SaveLog(0x0304, "SaveLog", "/logs/save", ModuleId.Logs, LogsDto::class, LogsResponseDto::class),
 

    BuildTokenEW(0x0401, "BuildTokenEW", "/pandora/build-tokenew", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
    InjectTokenEX(0x0402, "InjectTokenEX", "/pandora/inject-tokenex", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
    EncryptData(0x0403, "EncryptData", "/pandora/encrypt-data", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
    DecryptData(0x0404, "DecryptData", "/pandora/decrypt-data", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
    ReadDBKey(0x0405, "ReadDBKey", "/pandora/read-dbkey", ModuleId.Pandora, DBKeyRequestDto::class,  DBKeyResponseDto ::class),
    CalculateRsaHash(0x0406, "calculateRsaHash", "/pandora/calculate-rsa-hash", ModuleId.Pandora, VaultDtos.ReadKeyRequest::class,  VaultDtos.CalculateHashResponse ::class),



    EncryptPinBlock(0x0407, "EncryptPinBlock", "/pandora/encrypt-pinblock", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
     DecryptPinBlock(0x0408, "DecryptPinBlock", "/pandora/decrypt-pinblock", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
     TransferPinBlock(0x0408, "TransferPinBlock", "/pandora/transfer-pinblock", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),
     CalculatePekPin(0x0409, "CalculatePekPin", "/pandora/calculate-pek-pin", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),

     ComputePinKeys(0x040A, "ComputePinKeys", "/pandora/compute-pin-keys", ModuleId.Pandora, PandoraTransactionDto::class, PandoraResponse::class),

   
 
    SaveRSA(0x0501, "SaveRSA", "/vault/save-rsa", ModuleId.Vault, BankRsaKey::class, BankRsaKeyResponse::class),
    ReadRSA(0x0502, "ReadRSA", "/vault/read-rsa", ModuleId.Vault, VaultDtos.RequestReadRSAData::class, BankRsaKeyResponse::class),
   
    SaveClientCred(0x0508, "SaveClientCred", "/vault/save-client-credentials", ModuleId.Vault, VaultDtos.RequestReadClientCredData::class, VaultDtos.ResponseReadClientCredData::class),
    ReadClientCred(0x0509, "ReadClientCred", "/vault/read-client-credentials", ModuleId.Vault, VaultDtos.RequestReadClientCredData::class,  VaultDtos.ResponseReadClientCredData::class),
   
     
    SaveDUKPT(0x0503, "SaveDUKPT", "/vault/save-dukpt-data", ModuleId.Vault, DukptData::class, VaultDtos.DukptMetaResponseMin::class),


    ReadDUKPT(0x0504, "ReadDUKPT", "/vault/read-dukpt-data", ModuleId.Vault, VaultDtos.RequestReadDukptData::class, DukptDataResponse::class),
    SaveSecret(0x0505, "SaveSecret", "/vault/save-secret", ModuleId.Vault, VaultDtos.SecretValue::class, VaultDtos.SecretValueResponse::class),

    ReadSecret(0x0506, "ReadSecret", "/vault/read-secret", ModuleId.Vault, VaultDtos.ReadKeyRequest::class, VaultDtos.SecretValueResponse::class),
    VaultUnseal(0x0507, "VaultUnseal", "/vault/unseal", ModuleId.Vault,VaultDtos.UnsealRequest::class, VaultDtos.UnsealResponse::class),

    ReadDUKPTPIN(0x0508, "ReadDUKPTPIN", "/vault/read-dukpt-pin-data", ModuleId.Vault, VaultDtos.RequestReadDukptData::class, DukptDataResponse::class),
    GenerateKSN(0x050A, "GenerateKSN", "/vault/generate-ksn", ModuleId.Vault, VaultDtos.ReadKeyRequest::class, VaultDtos.SecretValueResponse::class),
 


    EncryptDUKPTHSM(0x0601, "EncryptDUKPTHSM", "/myhsm/encrypt-dukpt", ModuleId.MyHsm, MyHsmDto.DUKPTRequest::class, MyHsmDto.DUKPTResponse::class),
    DencryptDUKPTHSM(0x0602, "DencryptDUKPTHSM", "/myhsm/decrypt-dukpt", ModuleId.MyHsm, MyHsmDto.DUKPTRequest::class, MyHsmDto.DUKPTResponse::class),
  
    EncryptPinHSM(0x0604, "EncryptPinHSM", "/myhsm/encrypt-pin", ModuleId.MyHsm, MyHsmDto.PINHSMRequest::class, MyHsmDto.PINHSMResponse::class),
    DecryptPinHSM(0x0605, "DecryptPinHSM", "/myhsm/decrypt-pin", ModuleId.MyHsm, MyHsmDto.PINHSMRequest::class, MyHsmDto.PINHSMResponse::class),
  
    TransPinHSM(0x0606, "TransPinHSM", "/myhsm/trans-pin", ModuleId.MyHsm, MyHsmDto.PINHSMRequest::class, MyHsmDto.PINHSMResponse::class),
 
  
  
    TestHSM(0x0603, "TestHSM", "/myhsm/test", ModuleId.MyHsm, Any::class,Any::class),


    IsoParse(0x0701, "IsoParse", "/iso/to-transactiondata", ModuleId.Iso, DescribeISORequestDto::class, TransactionDto::class),
    IsoBuild(0x0702, "IsoBuild", "/iso/to-iso-ascii", ModuleId.Iso, TransactionRequestMinDto::class, ToIsoAsciiResponseDto::class),
    DescribeISO(0x0702, "DescribeISO", "/iso/describe-iso-ascii", ModuleId.Iso, DescribeISORequestDto::class, DescribeISOAsciiResponse::class),


    EmvParse(0x0801, "EmvParse", "", ModuleId.Emv, Any::class, Any::class),


    GetServerConfig(0x0901, "GetServerConfig", "/config/get-app-config", ModuleId.Config, JsonDto::class, ConfigResponse::class),
    SaveServerConfig(0x0902, "SaveServerConfig", "/config/save-app-config", ModuleId.Config, JsonDto::class, ConfigResponse::class),
    GetIsoDescriptor(0x0903, "GetIsoDescriptor", "/config/get-iso-desc", ModuleId.Config, JsonDto::class, ConfigResponse::class),
    SaveIsoDescriptor(0x0904, "SaveIsoDescriptor", "/config/save-iso-desc", ModuleId.Config, JsonDto::class, ConfigResponse::class),


    ReadLogFile(0x0A00, "ReadLogFile", "/db-logs/read-file-log", ModuleId.DB, FileLogRequestDto::class, FileLogResponseDto::class),
    GetClientActivityList(0x0A01, "GetClientActivityList", "/db-client-activity/list", ModuleId.DB, ClientActivityFilterDto::class, ClientActivityDataListResponse::class),
    GetTotalClientActivityPages(0x0A02, "GetTotalClientActivityPages", "/db-client-activity/pages", ModuleId.DB, ClientActivityFilterDto::class, TotalClientActivityPagesResponse::class),
    UpsertClientActivityData(0x0A03, "UpsertClientActivityData", "/db-client-activity/upsert", ModuleId.DB, ClientActivityDBData::class, UpsertClientActivityDataResponse::class),
    GetLogsList(0x0A04, "GetLogsList", "/db-logs/list", ModuleId.DB, LogsFilterDto::class, LogsDataListResponse::class),
    GetTotalLogsPages(0x0A05, "GetTotalLogsPages", "/db-logs/pages", ModuleId.DB, LogsFilterDto::class, TotalLogsPagesResponse::class),
    UpsertLogMetaData(0x0A06, "UpsertLogMetaData", "/db-logs/upsert", ModuleId.DB, LogsMetaDataDto::class, UpsertLogMetaDataResponse::class),
 
   
    GetTransactionsList(0x0A0A, "GetTransactionsList", "/db-transactions/list", ModuleId.DB, TransactionFilterDto::class, TransactionsListResponse::class),
    GetTotalTransactionPages(0x0A0B, "GetTotalTransactionPages", "/db-transactions/pages", ModuleId.DB, TransactionFilterDto::class, TotalTransactionPagesResponse::class),
    InsertTransactionData(0x0A0C, "InsertTransactionData", "/db-transactions/insert", ModuleId.DB, TransactionDBDataDto::class, InsertTransactionDataResponse::class),
    GetTransactionDetails(0x0A0D, "GetTransactionDetails", "/db-transactions/details", ModuleId.DB, TransactionDBDataDto::class, TransactionDetailsData::class),


    InsertSystemStats(0x0B0A, "InsertSystemStats", "/sys-stats/insert", ModuleId.DB, SystemStatsInsertRequestDto::class, SystemStatsResponseDto::class),
    GetStatsMinMax(0x0B0B, "GetStatsMinMax", "/sys-stats/min-max", ModuleId.DB, SystemStatsRequestDto::class, SystemStatsMinMaxDto::class),
    GetStatsList(0x0B0C, "GetStatsList", "/sys-stats/list", ModuleId.DB, SystemStatsRequestDto::class, SystemStatsResponseDto::class),
    GetTotalPages(0x0B0D, "GetTotalPages", "/sys-stats/pages", ModuleId.DB, SystemStatsRequestDto::class, SystemStatsResponseDto::class),

    TodayReport(0x0C01, "TodayReport", "/transactions-md/today-report", ModuleId.DB, TransactionsMetaReqDto::class, TransactionsMetaResDto::class),
    
 
  
  

 
    ;

    // Añade cualquier otra operación según sea necesario


    companion object{
        private val map = OperationId.values().associateBy(
            OperationId::operationName)

            private val mapApi = OperationId.values().associateBy(
                OperationId::apiPath)
    
        fun fromOperationName(operationName: String):OperationId {
            val result= map[operationName]
            if(result==null) return Undefined
            return result
        }
        fun fromApiPath(apiPath: String): OperationId {
            // Busca una coincidencia exacta
            mapApi[apiPath]?.let { return it }
        
            // Busca la coincidencia más cercana
            var closestMatch: OperationId? = null
            var minDistance = Int.MAX_VALUE
        
            mapApi.forEach { (path, operationId) ->
                val distance = levenshteinDistance(apiPath, path)
                if (distance < minDistance) {
                    minDistance = distance
                    closestMatch = operationId
                }
            }
        
            return closestMatch ?: OperationId.Undefined
        }
        
        // Función para calcular la distancia de Levenshtein
        private fun levenshteinDistance(s1: String, s2: String): Int {
            val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
            for (i in s1.indices) dp[i + 1][0] = i + 1
            for (j in s2.indices) dp[0][j + 1] = j + 1
        
            for (i in s1.indices) {
                for (j in s2.indices) {
                    dp[i + 1][j + 1] = if (s1[i] == s2[j]) {
                        dp[i][j]
                    } else {
                        min(dp[i][j] + 1, min(dp[i + 1][j] + 1, dp[i][j + 1] + 1))
                    }
                }
            }
        
            return dp[s1.length][s2.length]
        }

        var types= mutableListOf<String>()
        fun getNames(): MutableList<String>{
            if(types.size==0) {
                OperationId.values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }
    }

}
