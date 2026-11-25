package com.viergegroup.swmain.managers

import com.google.gson.GsonBuilder
import com.shared.commons.*
import com.viergegroup.crypto.pandora.managers.PandoraManager
import com.viergegroup.crypto.pandora.managers.VaultDocumentManager
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.pandora.managers.VaultDtos.CalculateHashResponse
import com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ReadKeyRequest
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.crypto.security.*
import com.viergegroup.crypto.security.DerivatedKeysHsmData
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import com.viergegroup.swmain.dtos.pandora.DukptDataDto
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtosgsonadapters.DBKeyResponseDtoGSONAdapter
import com.viergegroup.swmain.dtosgsonadapters.PandoraTransactionDtoGSONAdapter
import com.viergegroup.swmain.dtosgsonadapters.TransactionDtoGSONAdapter
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.springframework.beans.BeanUtils

class PandoraneoManager() {
    // val baseUrl="https://127.0.0.1:8097"
    //  val baseUrl="https://internal-prod-vault-1768334386.us-east-1.elb.amazonaws.com:8097"
    var baseUrl = "http://ec2-54-83-160-70.compute-1.amazonaws.com:8097"
    //  val baseUrl=  "https://switchvgprod.viergegroup.com"
    var authToken = ""
    private val gsonPrint =
            GsonBuilder()
                    //  .registerTypeAdapter(DBKeyDto.Request::class.java,
                    // DBKeyDtoGSONAdapter.Request())
                    .registerTypeAdapter(
                            DBKeyResponseDto::class.java,
                            DBKeyResponseDtoGSONAdapter()
                    )
                    .registerTypeAdapter(
                            PandoraTransactionDto::class.java,
                            PandoraTransactionDtoGSONAdapter()
                    )
                    .registerTypeAdapter(TransactionDto::class.java, TransactionDtoGSONAdapter())
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                    .setPrettyPrinting()
                    .create()
    //  val dateFormat = SimpleDateFormat(pattern)
    private var watcher = WatcherContext.getInstance()
    private var authTokenBearer: String = ""

    val gsonBuilder = GsonBuilder()

    private val gsonAPI =
            GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").setPrettyPrinting().create()

    init {
        baseUrl = AppSettings.getInstance().pandoraUrl
        WatcherGlobalLog.info("pandoraUrl: $baseUrl ")
    }
    /////////////////////////////////////////////// FUNs
    // /////////////////////////////////////////////////////////

    fun calculateRsaHash(dto: VaultDtos.ReadKeyRequest): VaultDtos.CalculateHashResponse {
        watcher.debug(
                " calculateRsaHash 1  pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().calculateRsaHash(dto)
        }
        var response = VaultDtos.CalculateHashResponse()
        val resapi =
                doApiRequest(
                        "pandora/calculate-rsa-hash",
                        "POST",
                        dto,
                        VaultDtos.CalculateHashResponse::class.java
                )

        if (resapi.data != null) {
            watcher.debug("readRsa COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data))
            response = resapi.data
            watcher.debug("JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readRsa  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun unseal(dto: VaultDtos.UnsealRequest): VaultDtos.UnsealResponse {
        watcher.debug(" unseal pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().unseal(dto)
        }
        var response = VaultDtos.UnsealResponse()
        val resapi = doApiRequest("vault/unseal", "POST", dto, VaultDtos.UnsealResponse::class.java)
        if (resapi.data != null) {
            watcher.debug("unseal COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data))
            response = resapi.data
            watcher.debug("unseal JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("encryptData  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun decryptWithDUKPT(request: MyHsmDto.DUKPTRequest): MyHsmDto.DUKPTResponse {
        watcher.debug(
                " decryptWithDUKPT pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return MyHsmManager().decryptWithDUKPT(request)
        }
        var response = MyHsmDto.DUKPTResponse()
        val resapi =
                doApiRequest(
                        "myhsm/decrypt-dukpt",
                        "POST",
                        request,
                        MyHsmDto.DUKPTResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun encryptWithDUKPT(request: MyHsmDto.DUKPTRequest): MyHsmDto.DUKPTResponse {
        watcher.debug(
                " encryptWithDUKPT pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return MyHsmManager().encryptWithDUKPT(request)
        }
        var response = MyHsmDto.DUKPTResponse()
        val resapi =
                doApiRequest(
                        "myhsm/encrypt-dukpt",
                        "POST",
                        request,
                        MyHsmDto.DUKPTResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun encryptPIN(request: MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        watcher.debug(
                " encryptWithDUKPT pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return MyHsmManager().encryptPIN(request)
        }
        var response = MyHsmDto.PINHSMResponse()
        val resapi =
                doApiRequest(
                        "myhsm/encrypt-dukpt",
                        "POST",
                        request,
                        MyHsmDto.PINHSMResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun decryptPIN(request: MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        watcher.debug(
                " encryptWithDUKPT pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return MyHsmManager().decryptPIN(request)
        }
        var response = MyHsmDto.PINHSMResponse()
        val resapi =
                doApiRequest(
                        "myhsm/encrypt-dukpt",
                        "POST",
                        request,
                        MyHsmDto.PINHSMResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun translatePIN(request: MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        watcher.debug(
                " encryptWithDUKPT pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return MyHsmManager().translatePIN(request)
        }
        var response = MyHsmDto.PINHSMResponse()
        val resapi =
                doApiRequest(
                        "myhsm/encrypt-dukpt",
                        "POST",
                        request,
                        MyHsmDto.PINHSMResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun generateKsn(dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        watcher.debug(" readSecret pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultDocumentManager.generateOrIncrementKsn(dto)
        }
        var response = VaultDtos.SecretValueResponse()
        val resapi =
                doApiRequest(
                        "vault/generate-ksn",
                        "POST",
                        dto,
                        VaultDtos.SecretValueResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "readSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("readSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun saveSecret(dto: VaultDtos.SecretValue): VaultDtos.SecretValueResponse {
        watcher.debug(" saveSecret pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().saveKeyValue(dto)
        }
        var response = VaultDtos.SecretValueResponse()
        val resapi =
                doApiRequest(
                        "vault/save-secret",
                        "POST",
                        dto,
                        VaultDtos.SecretValueResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun computeKeys(dto: PandoraTransactionDto): DukptDataDto {
        watcher.debug(" computeKeys pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return com.viergegroup.crypto.pandora.managers.DukptManager().computeKeys(dto)
        }
        var response = DukptDataDto()
        val resapi = doApiRequest("pandora/compute-keys", "POST", dto, DukptDataDto::class.java)
        if (resapi.data != null) {
            watcher.debug(
                    "computeKeys COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("computeKeys JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("computeKeys  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun readSecret(dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        watcher.debug(" readSecret pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().readKeyValue(dto)
        }
        var response = VaultDtos.SecretValueResponse()
        val resapi =
                doApiRequest(
                        "vault/read-secret",
                        "POST",
                        dto,
                        VaultDtos.SecretValueResponse::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "readSecret COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("readSecret JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readSecret  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun saveDukptData(dto: DukptData): VaultDtos.DukptMetaResponseMin {
        watcher.debug(" saveDukptData pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().saveDukptData(dto)
        }
        var response = VaultDtos.DukptMetaResponseMin()
        val resapi =
                doApiRequest(
                        "vault/save-dukpt-data",
                        "POST",
                        dto,
                        VaultDtos.DukptMetaResponseMin::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "saveDukptData COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("saveDukptData JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("saveDukptData  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun readDukptData(dto: VaultDtos.RequestReadDukptData): DukptData {
        watcher.debug(" readDukptData pandoralocal=> [${AppSettings.getInstance().pandoraLocal}] ")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().readDukptData(dto)
        }
        var response = DukptData()
        val resapi = doApiRequest("vault/read-dukpt-data", "POST", dto, DukptData::class.java)
        if (resapi.data != null) {
            watcher.debug(
                    "readDukptData COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("readDukptData JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readDukptData  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun saveClientCredentials(dto: ClientCredData): ClientCredData {
        val watcher = WatcherContext.getInstance()
        var bytesAscii = ByteArray(0)

        try {
            bytesAscii = StringUtils.asciiToHexByteArrayNeo(dto.data)
            watcher.debug(" bytesAscii ex=> [${StringUtils.encodeHexString(bytesAscii)}]")
            WatcherGlobalLog.debug(" bytesAscii ex=> [${StringUtils.encodeHexString(bytesAscii)}]")
        } catch (ex: Exception) {
            watcher.error(" saveClientCredentials ex=> [${ex.message}]")
            WatcherGlobalLog.error(" saveClientCredentials ex=> [${ex.message}]")
        }
        try {
            dto.data = StringUtils.encodeHexString(RSAUtils.encryptClientCred(bytesAscii, watcher))
            watcher.debug("  dto.data=> [${ dto.data}]")
            WatcherGlobalLog.debug("  dto.data=> [${ dto.data}]")
        } catch (ex: Exception) {
            watcher.error(" saveClientCredentials ex=> [${ex.message}]")
            WatcherGlobalLog.error(" saveClientCredentials ex=> [${ex.message}]")
        }

        WatcherGlobalLog.debug(
                " saveClientCredentials pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        watcher.debug(
                " saveClientCredentials pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )
        // Si se usa el entorno local, delegar al manejador local.
        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().saveClientCredentials(dto)
        }

        val response = ClientCredData()
        val resapi =
                doApiRequest(
                        "vault/save-client-credentials",
                        "POST",
                        dto,
                        ClientCredData::class.java
                )

        if (resapi.data != null) {
            watcher.debug(
                    "saveClientCredentials COMSUME SUCCESS: ${GsonBuilder().create().toJson(resapi.data)}"
            )
            response.data = resapi.data.data
            response.expirationDateTime = resapi.data.expirationDateTime
            response.errorCode = 0
            response.errorMessage = "SUCCESS"
        } else if (resapi.error != null) {
            watcher.error(
                    "saveClientCredentials ERROR: ${GsonBuilder().create().toJson(resapi.error)}"
            )
            response.errorMessage = "${ErrorCode.VaultError.errorCodeMessage} ${resapi.error}"
            response.errorCode = ErrorCode.VaultError.errorCodeInt
        }

        return response
    }

    fun readClientCredentials(dto: VaultDtos.RequestReadClientCredData): ClientCredData {
        val watcher = WatcherContext.getInstance()
        watcher.debug(
                " readClientCredentials pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        WatcherGlobalLog.debug(
                " readClientCredentials pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        // Si se usa el entorno local, delegar al manejador local.
        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().readClientCredentials(dto)
        }

        val response = ClientCredData()
        val resapi =
                doApiRequest(
                        "vault/read-client-credentials",
                        "POST",
                        dto,
                        ClientCredData::class.java
                )

        if (resapi.data != null) {
            watcher.debug(
                    "readClientCredentials COMSUME SUCCESS: ${GsonBuilder().create().toJson(resapi.data)}"
            )
            response.data = resapi.data.data
            response.expirationDateTime = resapi.data.expirationDateTime
            response.errorCode = 0
            response.errorMessage = "SUCCESS"
        } else if (resapi.error != null) {
            watcher.error(
                    "readClientCredentials ERROR: ${GsonBuilder().create().toJson(resapi.error)}"
            )
            response.errorMessage = "${ErrorCode.VaultError.errorCodeMessage} ${resapi.error}"
            response.errorCode = ErrorCode.VaultError.errorCodeInt
        }

        return response
    }

    fun saveRsa(dto: BankRsaKey): BankRsaKey {
        watcher.debug(" saveRsa pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().saveRsaKey(dto)
        }
        var response = BankRsaKey()
        val resapi = doApiRequest("vault/save-rsa", "POST", dto, BankRsaKey::class.java)
        if (resapi.data != null) {
            watcher.debug(
                    "readDukptData COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("readDukptData JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readDukptData  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }
    fun readRsa(dto: VaultDtos.RequestReadRSAData): BankRsaKey {
        watcher.debug(" readRsa pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().readRsaKey(dto)
        }
        var response = BankRsaKey()
        val resapi = doApiRequest("vault/read-rsa", "POST", dto, BankRsaKey::class.java)
        if (resapi.data != null) {
            watcher.debug("readRsa COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data))
            response = resapi.data
            watcher.debug("readRsa JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readRsa  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun encryptData(dtoOrg: TransactionDto): PandoraTransactionDto {
        watcher.debug(" encryptData 0 pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        val dto = PandoraTransactionDto()
        BeanUtils.copyProperties(dtoOrg, dto) 
 
        watcher.debug(" dto.iosVersionId=> [${dto.iosVersionId}]")
        watcher.debug(" dto.iosTransactionId=> [${StringUtils.maskString(dto.iosTransactionId)}]")
        watcher.debug(" dto.iosKeyData=> [${StringUtils.maskString(dto.iosKeyData)}]")
        watcher.debug(" dto.iosPinData=> [${StringUtils.maskString(dto.iosPinData)}]")
        watcher.debug(" dto.iosCardholderData=> [${StringUtils.maskString(dto.iosCardholderData)}]")
        watcher.debug(" dto.iosVersionId=> [${dto.iosVersionId}]")
        watcher.debug(" dto.iosMerchantId=> [${dto.iosMerchantId}]")
        watcher.debug(" dto.bankName=> [${dto.bankName}]")

        return encryptData(dto)
    }
    fun encryptData(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" encryptData 1  pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().encryptData(dto)
        }
        var response = PandoraTransactionDto()

        watcher.debug(" dto.iosVersionId=> [${StringUtils.maskString(dto.iosVersionId)}]")
        watcher.debug(" dto.iosTransactionId=> [${StringUtils.maskString(dto.iosTransactionId)}]")
        watcher.debug(" dto.iosKeyData=> [${StringUtils.maskString(dto.iosKeyData)}]")
        watcher.debug(" dto.iosPinData=> [${StringUtils.maskString(dto.iosPinData)}]")
        watcher.debug(" dto.iosCardholderData=> [${StringUtils.maskString(dto.iosCardholderData)}]")
        watcher.debug(" dto.iosVersionId=> [${StringUtils.maskString(dto.iosVersionId)}]")
        watcher.debug(" dto.iosMerchantId=> [${StringUtils.maskString(dto.iosMerchantId)}]")

        val resapi =
                doApiRequest("pandora/encrypt-data", "POST", dto, PandoraTransactionDto::class.java)

        if (resapi.data != null) {
            watcher.debug("readRsa COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data))
            response = resapi.data
            watcher.debug("JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readRsa  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun readDbKey(dto: DBKeyRequestDto): DBKeyResponseDto {
        watcher.debug(" readDbKey pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return VaultSecureManager().readDbKeyFull(dto)
        }
        var response = DBKeyResponseDto()
        watcher.debug("PANDORA readDbKey")
        val resapi = doApiRequest("pandora/read-dbkey", "POST", dto, DBKeyResponseDto::class.java)
        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("readDbKey JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("readDbKey  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }

        return response
    }

    fun decryptRsa(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" decryptRsa pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().decryptRsa(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA decryptRsa")
        val resapi =
                doApiRequest("pandora/decrypt-rsa", "POST", dto, PandoraTransactionDto::class.java)
        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("decryptRsa JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptRsa  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }

        return response
    }
    fun keyLoad(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" keyLoad pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().keyLoad(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("keyLoad PANDORA readDbKey")
        val resapi = doApiRequest("pandora/keyload", "POST", dto, PandoraTransactionDto::class.java)
        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("keyLoad  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }

        return response
    }

    fun decryptDukpt(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" decryptDukpt pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().keyLoad(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA readDbKey")
        val resapi =
                doApiRequest(
                        "pandora/generate-rsa-keys",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )
        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("decryptDukpt JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptDukpt  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }

        return response
    }

    fun generateRsaKeys(dto: PandoraTransactionDto): KeyPairData {
        watcher.debug(" generateRsaKeys pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().generateRsaKeys(dto)
        }
        var response = KeyPairData()
        watcher.debug("PANDORA readDbKey")
        val resapi = doApiRequest("pandora/generate-rsa-keys", "POST", dto, KeyPairData::class.java)
        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("generateRsaKeys JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("generateRsaKeys  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            //    response.errorMessage= ErrorCode.VaultError.errorCodeMessage+" "+resapi.error
            //   response.errorCode= ErrorCode.VaultError.errorCodeInt
            return response
        }

        return response
    }

    fun decryptData(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" decryptData pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().decryptData(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA decryptData")

        val resapi =
                doApiRequest("pandora/decrypt-data", "POST", dto, PandoraTransactionDto::class.java)

        if (resapi.data != null) {

            response = resapi.data

            watcher.debug("decryptData JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptData  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun transferPinBlock(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(
                " transferPinBlock pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]"
        )

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().transferPinBlock(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA transferPinBlock")

        val resapi =
                doApiRequest(
                        "pandora/transfer-pinblock",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )

        if (resapi.data != null) {

            response = resapi.data

            watcher.debug("transferPinBlock JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("transferPinBlock  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun decryptPinBlock(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" decryptPinBlock pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().decryptPinBlock(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA decryptPinBlock")

        val resapi =
                doApiRequest(
                        "pandora/decrypt-pinblock",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )

        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("decryptPinBlock JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptPinBlock  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun calculatePinPek(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" decryptPinBlock pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().calculatePinPek(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA decryptPinBlock")

        val resapi =
                doApiRequest(
                        "pandora/calculate-pek-pin",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )

        if (resapi.data != null) {
            response = resapi.data
            watcher.debug("decryptPinBlock JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptPinBlock  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun computePinKeys(dto: PandoraTransactionDto): DerivatedKeysHsmData {
        watcher.debug(" decryptPinBlock pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().computePinKeys(dto)
        }
        var response = DerivatedKeysHsmData()
        watcher.debug("PANDORA decryptPinBlock")

        val resapi =
                doApiRequest(
                        "pandora/compute-pin-keys",
                        "POST",
                        dto,
                        DerivatedKeysHsmData::class.java
                )
        if (resapi.data != null) {
            response = resapi.data!!
            watcher.debug("decryptPinBlock JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("decryptPinBlock  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            //  response.errorMessage= ErrorCode.VaultError.errorCodeMessage+" "+resapi.error
            //   response.errorCode= ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun encryptPinBlock(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" encryptPinBlock pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().encryptPinBlock(dto)
        }
        var response = PandoraTransactionDto()
        watcher.debug("PANDORA encryptPinBlock")

        val resapi =
                doApiRequest(
                        "pandora/encrypt-pinblock",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )

        if (resapi.data != null) {

            response = resapi.data

            watcher.debug("encryptPinBlock JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("encryptPinBlock  ERROR: " + gsonPrint.toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    /* fun decryptData( dtoOrg: TransactionTerminalDto): PandoraTransactionDto {



        val dto= PandoraTransactionDto()
        BeanUtils.copyProperties(dtoOrg,dto)
        return decryptData(dto)
    }
    fun decryptData( dtoOrg: TransactionDto): PandoraTransactionDto {
        val dto= PandoraTransactionDto()
        BeanUtils.copyProperties(dtoOrg,dto)
        return decryptData(dto)
    }*/

    fun buildTokenEW(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" buildTokenEW pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().buildTokenEW(dto)
        }
        var response = PandoraTransactionDto()

        val resapi =
                doApiRequest(
                        "pandora/build-tokenew",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )
        if (resapi.data != null) {
            watcher.debug(
                    "buildTokenEW COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("buildTokenEW  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        watcher.debug("buildTokenEW LOGS \n [\n " + response.logData + "]\n")

        return response
    }
    fun buildTokenEW(dtoOrg: TransactionDto): PandoraTransactionDto {
        watcher.debug(" buildTokenEW pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")

        val dto = PandoraTransactionDto()
        BeanUtils.copyProperties(dtoOrg, dto)
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().buildTokenEW(dto)
        }
        return buildTokenEW(dto)
    }

    fun injectTokenEX(dto: PandoraTransactionDto, authToken: String): PandoraTransactionDto {
        //  watcher= WatcherContext.getInstance()
        watcher.debug(" injectTokenEX 0 pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().injectTokenEX(dto)
        }
        this.authTokenBearer = authToken

        var response = PandoraTransactionDto()
        val resapi =
                doApiRequest(
                        "pandora/inject-tokenex",
                        "POST",
                        dto,
                        PandoraTransactionDto::class.java
                )

        if (resapi.data != null) {
            watcher.debug(
                    "injectTokenEX COMSUME SUCCESS: " + GsonBuilder().create().toJson(resapi.data)
            )
            response = resapi.data
            watcher.debug("JSON_RESPONSE: " + gsonPrint.toJson(response))
        } else if (resapi.error != null) {
            watcher.error("injectTokenEX  ERROR: " + GsonBuilder().create().toJson(resapi.data))
            watcher.debug("\n")
            response.errorMessage = ErrorCode.VaultError.errorCodeMessage + " " + resapi.error
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun injectTokenEX(dtoOrg: TransactionDto, authToken: String): PandoraTransactionDto {
        watcher.debug(" injectTokenEX 1 pandoralocal=> [${AppSettings.getInstance().pandoraLocal}]")
        val dto = PandoraTransactionDto()
        BeanUtils.copyProperties(dtoOrg, dto)
        if (AppSettings.getInstance().pandoraLocal) {
            return PandoraManager().injectTokenEX(dto)
        }
        return injectTokenEX(dto, authToken)
    }

    /////////////////////////////////////////////// ALL
    // /////////////////////////////////////////////////////////

    fun <T> doApiRequest(
            endpoint: String,
            methodType: String,
            data: Any,
            responseType: Class<T>
    ): Response<T> {
        val url = URL("$baseUrl/$endpoint")
        val protocolHttpps = (baseUrl.startsWith("https"))

        watcher.debug("doApiRequest_URL $baseUrl/$endpoint")

        val trustAllCerts =
                arrayOf<TrustManager>(
                        object : X509TrustManager {
                            override fun checkClientTrusted(
                                    chain: Array<out java.security.cert.X509Certificate>?,
                                    authType: String?
                            ) {}
                            override fun checkServerTrusted(
                                    chain: Array<out java.security.cert.X509Certificate>?,
                                    authType: String?
                            ) {}
                            override fun getAcceptedIssuers():
                                    Array<java.security.cert.X509Certificate> = arrayOf()
                        }
                )

        val maxRetries = 3
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                var authTokenApi = watcher.getAuthToken()
                 //  watcher.debug("authTokenApi $authTokenApi")
                 //  watcher.debug("this.authTokenBearer ${this.authTokenBearer}") 
 
                if (protocolHttpps) {
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.sslSocketFactory = sslContext.socketFactory
                    connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
                    connection.requestMethod = methodType
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.connectTimeout = 17000 // Reducido a 30 segundos
                    connection.readTimeout = 17000

                    if (this.authTokenBearer != "") {
                        watcher.debug("using authTokenBearer")
                        connection.setRequestProperty("Authorization", authTokenBearer)
                    } else {
                        connection.setRequestProperty("Authorization", authTokenApi)
                    }

                    connection.doOutput = true

                    if (methodType != "GET") {
                        val jsonInputString = gsonAPI.toJson(data)
                        val wr = OutputStreamWriter(connection.outputStream)
                        wr.write(jsonInputString)
                        wr.flush()
                        val jsonInputStringPrint = gsonPrint.toJson(data)
                        watcher.debug(
                                "doApiRequest URL: [$baseUrl/$endpoint] JSON_REQUEST: ${jsonInputStringPrint}"
                        )
                    }

                    val responseCode = connection.responseCode
                    val response: T =
                            if (responseCode == HttpURLConnection.HTTP_OK ||
                                            responseCode == HttpURLConnection.HTTP_CREATED
                            ) {
                                val reader =
                                        BufferedReader(InputStreamReader(connection.inputStream))
                                val jsonstr = reader.readLine()
                                watcher.debug(
                                        "doApiRequest URL: [$baseUrl/$endpoint] JSON_RESPONSE ${jsonstr}"
                                )
                                gsonAPI.fromJson(jsonstr, responseType)
                            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                                return Response(data = null, error = null)
                            } else {
                                throw Exception("SECURE SERVICES ERROR P-HSM-VAULT: $responseCode")
                            }

                    connection.disconnect()
                    return Response(data = response, error = null)
                } else {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = methodType
                    connection.setRequestProperty("Content-Type", "application/json")
                     if (this.authTokenBearer != "") {
                        watcher.debug("using authTokenBearer")
                        connection.setRequestProperty("Authorization", authTokenBearer)
                    } else {
                        connection.setRequestProperty("Authorization", authTokenApi)
                    }

                    connection.doOutput = true
                    connection.connectTimeout = 17000 // Reducido a 30 segundos
                    connection.readTimeout = 17000

                    if (methodType != "GET") {
                        val jsonInputString = gsonAPI.toJson(data)
                        val wr = OutputStreamWriter(connection.outputStream)
                        wr.write(jsonInputString)
                        wr.flush()
                        val jsonInputStringPrint = gsonPrint.toJson(data)
                        watcher.debug(
                                "doApiRequest URL: [$baseUrl/$endpoint] JSON_REQUEST: ${jsonInputStringPrint}"
                        )
                    }

                    val responseCode = connection.responseCode
                    val response: T =
                            if (responseCode == HttpURLConnection.HTTP_OK ||
                                            responseCode == HttpURLConnection.HTTP_CREATED
                            ) {
                                val reader =
                                        BufferedReader(InputStreamReader(connection.inputStream))
                                val jsonstr = reader.readLine()
                                watcher.debug(
                                        "doApiRequest URL: [$baseUrl/$endpoint] JSON_RESPONSE ${jsonstr}"
                                )
                                gsonAPI.fromJson(jsonstr, responseType)
                            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                                return Response(data = null, error = null)
                            } else {
                                throw Exception(
                                        "doApiRequest Error al obtener la respuesta. Código de estado: $responseCode"
                                )
                            }

                    connection.disconnect()
                    return Response(data = response, error = null)
                }
            } catch (e: Exception) {
                lastException = e
                watcher.error("Attempt ${attempt + 1} failed: ${e.message}")
                attempt++
                if (attempt < maxRetries) {
                    Thread.sleep(1000) // Esperar 1 segundo antes de reintentar
                }
            }
        }

        watcher.error("All $maxRetries attempts failed.")
        return Response(
                data = null,
                error = "SECURE SERVICES ERROR P-HSM-VAULT: ${lastException?.message}"
        )
    }
}

data class Response<T>(val data: T?, val error: String?)
