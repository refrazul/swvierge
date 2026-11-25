


package com.viergegroup.evopayment.dtos
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.dtos.comm.CommHostRequestDto
import  com.shared.commons.ErrorCode
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import 	com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.entities.catalogs.TransactionType
 import  com.viergegroup.swmain.entities.catalogs.PointOfServiceEntryMode
 import com.shared.commons.GsonConfig
import com.shared.commons.AppSettings
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.shared.commons.DynamicISOFields

 import com.viergegroup.swmain.entities.catalogs.CardType
import com.shared.commons.StringUtils 
object SwVierge2EvoMapper {



 

     // ======================= SALE → CONFIRM =======================
    fun saleRsp2Confirm(reference:String,req: EvoPaymentResponseDto, watcher: Watcher): EvoConfirmRequestDto {
        watcher.debug("Iniciando mapeo saleRsp2Confirm: reference $reference  \n ${GsonConfig.gson.toJson(req)}")

        val src = req.SaleResponse // Tiene AnswerKey, PublicAnswerKey, Company/System/Branch/POS/Reference
        val dto = EvoConfirmRequestDto().apply {
            Confirm.RequestType = "Confirm"
            Confirm.Reference =  reference
            //   Confirm.AnswerKey = src.AnswerKey.ifBlank { src.PublicAnswerKey }
            Confirm.CompanyIdentification = src.CompanyIdentification
            Confirm.SystemIdentification = src.SystemIdentification
            Confirm.BranchIdentification = src.BranchIdentification
            Confirm.POSIdentification = src.POSIdentification
        }

        watcher.debug("Mapped saleRsp2Confirm  reference $reference → ${GsonConfig.gson.toJson(dto)}")
        return dto
    }

    // ======================= RETURN → CONFIRM =======================
    fun returnRsp2Confirm(reference:String,req: EvoReturnResponseDto, watcher: Watcher): EvoConfirmRequestDto {
        watcher.debug("Iniciando mapeo returnRsp2Confirm:reference $reference  \n ${GsonConfig.gson.toJson(req)}")

        val src = req.ReturnResponse // Tiene AnswerKey, PublicAnswerKey, Company/System/Branch/POS/Reference
        val dto = EvoConfirmRequestDto().apply {
            Confirm.RequestType = "Confirm"
            Confirm.Reference = reference 
            //   Confirm.AnswerKey = src.AnswerKey.ifBlank { src.PublicAnswerKey }
            Confirm.CompanyIdentification = src.CompanyIdentification
            Confirm.SystemIdentification = src.SystemIdentification
            Confirm.BranchIdentification = src.BranchIdentification
            Confirm.POSIdentification = src.POSIdentification
        }

        watcher.debug("Mapped returnRsp2Confirm NEO  reference $reference → ${GsonConfig.gson.toJson(dto)}")
        return dto
    }

    // ======================= VOID → CONFIRM =======================
    fun voidRsp2Confirm(reference:String,req: EvoVoidResponseDto, watcher: Watcher): EvoConfirmRequestDto {
        watcher.debug("Iniciando mapeo voidRsp2Confirm: reference $reference  \n ${GsonConfig.gson.toJson(req)}")

        val src = req.VoidResponse // Tiene AnswerKey, PublicAnswerKey, Company/System/Branch/POS/Reference
        val dto = EvoConfirmRequestDto().apply {
            Confirm.RequestType = "Confirm"
            Confirm.Reference = reference
           //    Confirm.AnswerKey = src.AnswerKey.ifBlank { src.PublicAnswerKey }
            Confirm.CompanyIdentification = src.CompanyIdentification
            Confirm.SystemIdentification = src.SystemIdentification
            Confirm.BranchIdentification = src.BranchIdentification
            Confirm.POSIdentification = src.POSIdentification
        }

        watcher.debug("Mapped voidRsp2Confirm  reference $reference → ${GsonConfig.gson.toJson(dto)}")
        return dto
    }





      // ======================= VOID =======================
      
        fun txnDto2VoidDto(txn: TransactionDto, watcher: Watcher): EvoVoidRequestDto {
            watcher.debug("Iniciando mapeo txnDto2VoidDto para txn: ${GsonConfig.gson.toJson(txn)}")

            val req = EvoVoidRequestDto()
            val void = req.Void 

            void.Reference = txn.rrn ?: ""
            watcher.debug("void.Reference → '${void.Reference}'")

            void.CompanyIdentification = txn.companyIdentification ?: ""
            watcher.debug("void.CompanyIdentification → '${void.CompanyIdentification}'")

            void.SystemIdentification = txn.systemIdentification ?: ""
            watcher.debug("void.SystemIdentification → '${void.SystemIdentification}'")

            void.BranchIdentification = txn.branchIdentification ?: ""
            watcher.debug("void.BranchIdentification → '${void.BranchIdentification}'")

            void.POSIdentification = txn.posIdentification ?: ""
            watcher.debug("void.POSIdentification → '${void.POSIdentification}'")

            void.OrigReference = txn.originalRrn ?: ""
            watcher.debug("void.OrigReference → '${void.OrigReference}'")
          //  void.OrigReference = txn.rrn ?: ""
   
            //void.Reference = DynamicISOFields.getNewRRN(txn.hostType)  //txn.rrn ?: ""
          
         
           // void.Sequence=   void.Reference 

           //   watcher.debug("  void.Reference getNewRRN '${  void.Reference }'")

            //watcher.debug("void.Sequence → '${void.Sequence}'")
            watcher.debug("txnDto2VoidDto_originalRrn  DynamicISOFields.getNewRRN(txn.hostType : ${txn.originalRrn} rrn: ${txn.rrn}")

            watcher.debug("Mapped VoidRequest completo → ${GsonConfig.gson.toJson(req)}")

            return req
        }


       
        



        fun  comm2KeepAliveDto(dtoOrg:CommHostRequestDto): EvoKeepAliveDto {
            val dto = EvoKeepAliveDto()
            dto.KeepAlive.RequestType = "KeepAlive"  // Valor fijo como en el ejemplo original
            dto.KeepAlive.POSIdentification = dtoOrg.posIdentification
            dto.KeepAlive.BranchIdentification = dtoOrg.branchIdentification
            dto.KeepAlive.CompanyIdentification = dtoOrg.companyIdentification
            dto.KeepAlive.SystemIdentification = dtoOrg.systemIdentification
            return dto
        }


        fun evoKeepAliveToTxnResponse(evo: EvoKeepAliveResponseDto): SendTransactionResponse {
            val response = SendTransactionResponse()
            val dto = response.transactionResponse

            val detail = evo.KeepAliveResponse 

            // Mapeo general
            dto.responseCode = detail.ResponseCode.toString()
            dto.responseCodeAuthorizer = detail.ResponseCode.toString()
            dto.additionalResponseDate = detail.ResponseMessage
           

            return response
        }



     /* fun txnDto2ReverseDto(txnDto: TransactionDto,watcher:Watcher): EvoSaleRequestDto {
             val request=txnDto2PaymentDto(txnDto,watcher)
             request.Sale.IsReverse="1"
            return request 
       }*/ 
 


      fun txnDto2Return(txnDto: TransactionDto,watcher:Watcher): EvoReturnRequestDto {
        val newDto = EvoReturnRequestDto()
        val dtoSaleEvo = newDto.Return
        val inputTokens = dtoSaleEvo.InputTokens
        watcher.debug("txnDto2PaymentDto INI ")

       var amountFormatted: String=""
         dtoSaleEvo.Amount =StringUtils.parseAmountToDouble( txnDto.transactionAmount,watcher)
       //  dtoSaleEvo.TipAmount =StringUtils.parseAmountToDouble( txnDto.otherAmount,watcher)

            // ========== Mapeo directo ==========
        // dtoSaleEvo.EmvTags = txnDto.emvData
        watcher.debug("txnDto.emvData              [${txnDto.emvData}]")
        //watcher.debug("dtoSaleEvo.EmvTags              [${dtoSaleEvo.EmvTags}]")

        dtoSaleEvo.CardNumber = txnDto.cardNumber
        watcher.debug("txnDto.cardNumber              [${txnDto.cardNumber}]")

        if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Contactless){
             dtoSaleEvo.CardReadMode="L"
        }
         if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Inserted){
             dtoSaleEvo.CardReadMode="C"
        } 
     //   dtoSaleEvo.CardReadMode = txnDto.pointOfServiceEntryMode.toString().substring(0, 1)
        watcher.debug("dtoSaleEvo.CardReadMode        [${dtoSaleEvo.CardReadMode}]")

        dtoSaleEvo.SystemIdentification = txnDto.systemIdentification
        watcher.debug("dtoSaleEvo.SystemIdentification  [${dtoSaleEvo.SystemIdentification}]")

        dtoSaleEvo.CompanyIdentification = txnDto.companyIdentification
        watcher.debug("dtoSaleEvo.CompanyIdentification [${dtoSaleEvo.CompanyIdentification}]")

        dtoSaleEvo.BranchIdentification = txnDto.branchIdentification
        watcher.debug("dtoSaleEvo.BranchIdentification  [${dtoSaleEvo.BranchIdentification}]")

        dtoSaleEvo.POSIdentification = txnDto.posIdentification
        watcher.debug("dtoSaleEvo.POSIdentification     [${dtoSaleEvo.POSIdentification}]")
 
        dtoSaleEvo.Reference =txnDto.rrn //  DynamicISOFields.getNewRRN(txnDto.hostType)  //txnDto.rrn 

        watcher.debug("dtoSaleEvo.Reference_getNewRRN [${dtoSaleEvo.Reference}]")

        dtoSaleEvo.CurrencyCode = txnDto.transactionCurrencyCode.ifEmpty { "484" }
        watcher.debug("dtoSaleEvo.CurrencyCode        [${dtoSaleEvo.CurrencyCode}]")
 
        dtoSaleEvo.InputTokens=  buildEvoInputTokens(txnDto,dtoSaleEvo.Amount,watcher)
         dtoSaleEvo.OrigReference = txnDto.originalRrn ?: ""
        watcher.debug("dtoSaleEvo.OrigReference        [${dtoSaleEvo.OrigReference}]")
        // txnDto.originalRrn= dtoSaleEvo.OrigReference 
         watcher.debug("txnDto.originalRrn        [${txnDto.originalRrn}]")

         //   txnDto.rrn=dtoSaleEvo.Reference 
        watcher.debug("new_refund_rrn      [${   txnDto.rrn}]")

         return newDto
      }

      
    fun txnDto2ReverseDtoV2(txnDto: TransactionDto,watcher:Watcher): EvoSaleReverseRequestDto {
        val newDto = EvoSaleReverseRequestDto()
        val dtoSaleEvo = newDto.Sale
       // val inputTokens = dtoSaleEvo.InputTokens     

        watcher.debug("txnDto2ReverseDtoV2 INI ")

       var amountFormatted: String=""
       
        watcher.debug("txnDto.emvData              [${txnDto.emvData}]")
     

        dtoSaleEvo.SystemIdentification = txnDto.systemIdentification
        watcher.debug("dtoSaleEvo.SystemIdentification  [${dtoSaleEvo.SystemIdentification}]")

        dtoSaleEvo.CompanyIdentification = txnDto.companyIdentification
        watcher.debug("dtoSaleEvo.CompanyIdentification [${dtoSaleEvo.CompanyIdentification}]")

        dtoSaleEvo.BranchIdentification = txnDto.branchIdentification
        watcher.debug("dtoSaleEvo.BranchIdentification  [${dtoSaleEvo.BranchIdentification}]")

        dtoSaleEvo.POSIdentification = txnDto.posIdentification
        watcher.debug("dtoSaleEvo.POSIdentification     [${dtoSaleEvo.POSIdentification}]")
         watcher.debug("txnDto.rrn          [${txnDto.rrn }]")
         watcher.debug("txnDto.originalRrn          [${txnDto.originalRrn }]")

        dtoSaleEvo.Reference = txnDto.rrn 
     
        watcher.debug("dtoSaleEvo.Reference           [${dtoSaleEvo.Reference}]")
        watcher.debug("txnDto.originalRrn           [${txnDto.originalRrn}]")
        watcher.debug("txnDto.rrn           [${txnDto.rrn}]")

      

         return newDto
      }

  fun txnDto2ReturnReverseDtoV2(txnDto: TransactionDto,watcher:Watcher): EvoReturnReverseRequestDto {
        val newDto = EvoReturnReverseRequestDto()
        val dtoReturnEvo = newDto.Return
       // val inputTokens = dtoReturnEvo.InputTokens     

        watcher.debug("txnDto2ReverseDtoV2 INI ")

       var amountFormatted: String=""
       
        watcher.debug("txnDto.emvData              [${txnDto.emvData}]")
     

        dtoReturnEvo.SystemIdentification = txnDto.systemIdentification
        watcher.debug("dtoReturnEvo.SystemIdentification  [${dtoReturnEvo.SystemIdentification}]")

        dtoReturnEvo.CompanyIdentification = txnDto.companyIdentification
        watcher.debug("dtoReturnEvo.CompanyIdentification [${dtoReturnEvo.CompanyIdentification}]")

        dtoReturnEvo.BranchIdentification = txnDto.branchIdentification
        watcher.debug("dtoReturnEvo.BranchIdentification  [${dtoReturnEvo.BranchIdentification}]")

        dtoReturnEvo.POSIdentification = txnDto.posIdentification
        watcher.debug("dtoReturnEvo.POSIdentification     [${dtoReturnEvo.POSIdentification}]")
         watcher.debug("txnDto.rrn          [${txnDto.rrn }]")
         watcher.debug("txnDto.originalRrn          [${txnDto.originalRrn }]")

        dtoReturnEvo.Reference = txnDto.rrn 
     
        watcher.debug("dtoReturnEvo.Reference           [${dtoReturnEvo.Reference}]")
        watcher.debug("txnDto.originalRrn           [${txnDto.originalRrn}]")
        watcher.debug("txnDto.rrn           [${txnDto.rrn}]")

      
   
         return newDto
      }

           
    fun txnDto2SaleTipDto(txnDto: TransactionDto,watcher:Watcher): EvoSaleTipRequestDto {
        val newDto = EvoSaleTipRequestDto()
        val dtoSaleEvo = newDto.Sale
        val inputTokens = dtoSaleEvo.InputTokens
        watcher.debug("txnDto2PaymentDto INI ")

       var amountFormatted: String=""
         dtoSaleEvo.Amount =StringUtils.parseAmountToDouble( txnDto.transactionAmount,watcher)
         dtoSaleEvo.TipAmount =StringUtils.parseAmountToDouble( txnDto.otherAmount,watcher)

            // ========== Mapeo directo ==========
        // dtoSaleEvo.EmvTags = txnDto.emvData
        watcher.debug("txnDto.emvData              [${txnDto.emvData}]")
        //watcher.debug("dtoSaleEvo.EmvTags              [${dtoSaleEvo.EmvTags}]")

        dtoSaleEvo.CardNumber = txnDto.cardNumber
        watcher.debug("txnDto.cardNumber              [${txnDto.cardNumber}]")

        if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Contactless){
             dtoSaleEvo.CardReadMode="L"
        }
         if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Inserted){
             dtoSaleEvo.CardReadMode="C"
        } 
     //   dtoSaleEvo.CardReadMode = txnDto.pointOfServiceEntryMode.toString().substring(0, 1)
        watcher.debug("dtoSaleEvo.CardReadMode        [${dtoSaleEvo.CardReadMode}]")

        dtoSaleEvo.SystemIdentification = txnDto.systemIdentification
        watcher.debug("dtoSaleEvo.SystemIdentification  [${dtoSaleEvo.SystemIdentification}]")

        dtoSaleEvo.CompanyIdentification = txnDto.companyIdentification
        watcher.debug("dtoSaleEvo.CompanyIdentification [${dtoSaleEvo.CompanyIdentification}]")

        dtoSaleEvo.BranchIdentification = txnDto.branchIdentification
        watcher.debug("dtoSaleEvo.BranchIdentification  [${dtoSaleEvo.BranchIdentification}]")

        dtoSaleEvo.POSIdentification = txnDto.posIdentification
        watcher.debug("dtoSaleEvo.POSIdentification     [${dtoSaleEvo.POSIdentification}]")
 
        dtoSaleEvo.Reference = txnDto.rrn 
        watcher.debug("dtoSaleEvo.Reference           [${dtoSaleEvo.Reference}]")

        dtoSaleEvo.CurrencyCode = txnDto.transactionCurrencyCode.ifEmpty { "484" }
        watcher.debug("dtoSaleEvo.CurrencyCode        [${dtoSaleEvo.CurrencyCode}]")

        dtoSaleEvo.InputTokens=  buildEvoInputTokens(txnDto,dtoSaleEvo.Amount,watcher)

         return newDto
      }


    fun txnDto2PaymentDto(txnDto: TransactionDto,watcher:Watcher): EvoSaleRequestDto {
        val newDto = EvoSaleRequestDto()
        val dtoSaleEvo = newDto.Sale
        val inputTokens = dtoSaleEvo.InputTokens
        watcher.debug("txnDto2PaymentDto INI ")

       var amountFormatted: String=""
         dtoSaleEvo.Amount =StringUtils.parseAmountToDouble( txnDto.transactionAmount,watcher)

            // ========== Mapeo directo ==========
        // dtoSaleEvo.EmvTags = txnDto.emvData
        watcher.debug("txnDto.emvData              [${txnDto.emvData}]")
        //watcher.debug("dtoSaleEvo.EmvTags              [${dtoSaleEvo.EmvTags}]")

        dtoSaleEvo.CardNumber = txnDto.cardNumber
        watcher.debug("txnDto.cardNumber              [${txnDto.cardNumber}]")

        if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Contactless){
             dtoSaleEvo.CardReadMode="L"
        }
         if(txnDto.pointOfServiceEntryMode==PointOfServiceEntryMode.Inserted){
             dtoSaleEvo.CardReadMode="C"
        } 
     //   dtoSaleEvo.CardReadMode = txnDto.pointOfServiceEntryMode.toString().substring(0, 1)
        watcher.debug("dtoSaleEvo.CardReadMode        [${dtoSaleEvo.CardReadMode}]")

        dtoSaleEvo.SystemIdentification = txnDto.systemIdentification
        watcher.debug("dtoSaleEvo.SystemIdentification  [${dtoSaleEvo.SystemIdentification}]")

        dtoSaleEvo.CompanyIdentification = txnDto.companyIdentification
        watcher.debug("dtoSaleEvo.CompanyIdentification [${dtoSaleEvo.CompanyIdentification}]")

        dtoSaleEvo.BranchIdentification = txnDto.branchIdentification
        watcher.debug("dtoSaleEvo.BranchIdentification  [${dtoSaleEvo.BranchIdentification}]")

        dtoSaleEvo.POSIdentification = txnDto.posIdentification
        watcher.debug("dtoSaleEvo.POSIdentification     [${dtoSaleEvo.POSIdentification}]")
 
        dtoSaleEvo.Reference = txnDto.rrn 
        watcher.debug("dtoSaleEvo.Reference           [${dtoSaleEvo.Reference}]")

        dtoSaleEvo.CurrencyCode = txnDto.transactionCurrencyCode.ifEmpty { "484" }
        watcher.debug("dtoSaleEvo.CurrencyCode        [${dtoSaleEvo.CurrencyCode}]")

          dtoSaleEvo.InputTokens=  buildEvoInputTokens(txnDto,dtoSaleEvo.Amount,watcher)

         return newDto
      }




     fun formatIssApplDataHex(value: String, lengthStr: String): String {
        val keepLen = lengthStr.toIntOrNull() ?: return value

        // Si el length a conservar es mayor o igual al tamaño, no hacemos nada
        if (keepLen >= value.length) return value

        // 1) Tomamos solo los primeros keepLen caracteres
        val prefix = value.substring(0, keepLen)

        // 2) Calculamos cuántos caracteres faltan para llegar al tamaño original
        val padLen = value.length - keepLen

        // 3) Rellenamos el resto con 'F'
        return prefix + "F".repeat(padLen)
    } 

       fun buildEvoInputTokens(
            txnDto: TransactionDto,
            amount: Double,              // reemplaza a dtoSaleEvo.Amount
            watcher: Watcher
        ): EvoInputTokensDto {

            val inputTokens = EvoInputTokensDto()
            watcher.debug("=== buildEvoInputTokens: START ===")
            watcher.debug("Txn snapshot → ${GsonConfig.gson.toJson(txnDto)}")
            watcher.debug("Amount for capabilities logic → $amount")

            // ===================== B2 =====================
            if (txnDto.bitmapB2.isNullOrEmpty()) txnDto.bitmapB2 = "7FF9"
            if (txnDto.userField1B2.isNullOrEmpty()) txnDto.userField1B2 = "0000"
            if (txnDto.criptoInfoData.isNullOrEmpty()) txnDto.criptoInfoData = "80"
            if (txnDto.tvr.isNullOrEmpty()) txnDto.tvr = "0080008000"
            if (txnDto.otherAmount.isNullOrEmpty()) txnDto.otherAmount = "000000000000"
            if (txnDto.atc.isNullOrEmpty()) txnDto.atc = "0000"
            if (txnDto.terminalCountryCode.isNullOrEmpty()) txnDto.terminalCountryCode = "484"
            if (txnDto.transactionCurrencyCode.isNullOrEmpty()) txnDto.transactionCurrencyCode = "484"
            if (txnDto.emvTransactionType.isNullOrEmpty()) txnDto.emvTransactionType = "00"
            if (txnDto.issApplDataLength.isNullOrEmpty()) txnDto.issApplDataLength = "0036"
            if (txnDto.issApplData.isNullOrEmpty()) txnDto.issApplData =
                "0106A00009400000000000000000000000000000000000000000000000000000" 

                // -------- LÓGICA CORRECTA: SUBSTRING + PAD CON 'F' --------
            txnDto.issApplData = formatIssApplDataHex(
                txnDto.issApplData,
                txnDto.issApplDataLength      // longitud en BYTES (ej: "0036")
            )

            if (txnDto.transactionType==TransactionType.Refund) txnDto.emvTransactionType = "20"


            watcher.debug("=== TokenB2 VALUES  ===")
            watcher.debug("bitmapB2                  [${txnDto.bitmapB2}]")
            watcher.debug("userField1B2              [${txnDto.userField1B2}]")
            watcher.debug("criptoInfoData            [${txnDto.criptoInfoData}]")
            watcher.debug("tvr                       [${txnDto.tvr}]")
            watcher.debug("arqc                      [${txnDto.arqc}]")
            watcher.debug("transactionAmount         [${txnDto.transactionAmount}]")
            watcher.debug("otherAmount               [${txnDto.otherAmount}]")
            watcher.debug("aip                       [${txnDto.aip}]")
            watcher.debug("atc                       [${txnDto.atc}]")
            watcher.debug("terminalCountryCode       [${txnDto.terminalCountryCode}]")
            watcher.debug("transactionCurrencyCode   [${txnDto.transactionCurrencyCode}]")
            watcher.debug("emvLocalTransactionDate   [${txnDto.emvLocalTransactionDate}]")
            watcher.debug("emvTransactionType        [${txnDto.emvTransactionType}]")
            watcher.debug("unpredictableNumber       [${txnDto.unpredictableNumber}]")
            watcher.debug("issApplDataLength         [${txnDto.issApplDataLength}]")
            watcher.debug("issApplData               [${txnDto.issApplData}]")

            inputTokens.TokenB2 =
                txnDto.bitmapB2 +
                txnDto.userField1B2 +
                txnDto.criptoInfoData +
                txnDto.tvr +
                txnDto.arqc +
                txnDto.transactionAmount +
                txnDto.otherAmount +
                txnDto.aip +
                txnDto.atc +
                txnDto.terminalCountryCode +
                txnDto.transactionCurrencyCode +
                txnDto.emvLocalTransactionDate +
                txnDto.emvTransactionType +
                txnDto.unpredictableNumber +
                txnDto.issApplDataLength +
                txnDto.issApplData

            watcher.debug("=== TokenB2 FINAL STRING ===")
            watcher.debug("TokenB2: [${inputTokens.TokenB2}]")
            watcher.debug("TokenB2 Length: ${inputTokens.TokenB2.length}")

            // ===================== B3 =====================
            if (txnDto.bitmapB3.isNullOrEmpty()) txnDto.bitmapB3 = "CF00"

            val serial = txnDto.terminalSerialNumber
            if (serial.length >= 8) {
                val last8 = serial.takeLast(8)
                txnDto.emvTermSerialNumber = last8
                watcher.debug("emvTermSerialNumber (últimos 8): [$last8]")
            } else {
                txnDto.emvTermSerialNumber = serial.padStart(8, '0')
                watcher.debug("emvTermSerialNumber (rellenado con ceros): [${txnDto.emvTermSerialNumber}]")
            }

            if (txnDto.emvTerminalCapabilities.isNullOrEmpty() || txnDto.emvTerminalCapabilities == "00000000")
                txnDto.emvTerminalCapabilities = "00080800"

            watcher.debug("""dfName.contains("A000000003") [${txnDto.dfName.contains("A000000003")}]""")

            if (txnDto.dfName.contains("A000000003")) {
                watcher.debug("""txnDto.dfName.contains("A000000003")  AMOUNT=>[$amount]""")
                // mastercard 000808 ↔ 002808 si amount > 1000
                txnDto.emvTerminalCapabilities = if (amount > 1000.00) "00280800" else "00080800"
            }

            if (txnDto.userField1B3.isNullOrEmpty()) txnDto.userField1B3 = "0000"
            if (txnDto.userField2B3.isNullOrEmpty()) txnDto.userField2B3 = "00000000"
            if (txnDto.emvTerminalType.isNullOrEmpty()) txnDto.emvTerminalType = "22"
            if (txnDto.applicationVersionNumber.isNullOrEmpty()) txnDto.applicationVersionNumber = "008C"
            if (txnDto.cvmResults.isNullOrEmpty()) txnDto.cvmResults = "1E0300"
            if (txnDto.dfNameLength.isNullOrEmpty()) txnDto.dfNameLength = "0014"
            if (txnDto.dfName.isNullOrEmpty()) txnDto.dfName = "A0000000041010000000000000000000"

               
            val len = txnDto.dfNameLength.toIntOrNull() ?: 0
            txnDto.dfNameLength = (len * 2).toString().padStart(4, '0')
               
            watcher.debug("=== TokenB3 VALUES ===")
            watcher.debug("bitmapB3                 [${txnDto.bitmapB3}]")
            watcher.debug("emvTermSerialNumber      [${txnDto.emvTermSerialNumber}]")
            watcher.debug("emvTerminalCapabilities  [${txnDto.emvTerminalCapabilities}]")
            watcher.debug("userField1B3             [${txnDto.userField1B3}]")
            watcher.debug("userField2B3             [${txnDto.userField2B3}]")
            watcher.debug("emvTerminalType          [${txnDto.emvTerminalType}]")
            watcher.debug("applicationVersionNumber [${txnDto.applicationVersionNumber}]")
            watcher.debug("cvmResults               [${txnDto.cvmResults}]")
            watcher.debug("dfNameLength             [${txnDto.dfNameLength}]")
            watcher.debug("dfName                   [${txnDto.dfName}]")

            inputTokens.TokenB3 =
                txnDto.bitmapB3 +
                txnDto.emvTermSerialNumber +
                txnDto.emvTerminalCapabilities +
                txnDto.userField1B3 +
                txnDto.userField2B3 +
                txnDto.emvTerminalType +
                txnDto.applicationVersionNumber +
                txnDto.cvmResults +
                txnDto.dfNameLength +
                txnDto.dfName

            watcher.debug("=== TokenB3 FINAL STRING ===")

             watcher.debug("bitmapB3                 [${txnDto.bitmapB3}]")
            watcher.debug("emvTermSerialNumber      [${txnDto.emvTermSerialNumber}]")
            watcher.debug("emvTerminalCapabilities  [${txnDto.emvTerminalCapabilities}]")
            watcher.debug("userField1B3             [${txnDto.userField1B3}]")
            watcher.debug("userField2B3             [${txnDto.userField2B3}]")
            watcher.debug("emvTerminalType          [${txnDto.emvTerminalType}]")
            watcher.debug("applicationVersionNumber [${txnDto.applicationVersionNumber}]")
            watcher.debug("cvmResults               [${txnDto.cvmResults}]")
            watcher.debug("dfNameLength             [${txnDto.dfNameLength}]")
            watcher.debug("dfName                   [${txnDto.dfName}]")



            watcher.debug("TokenB3: [${inputTokens.TokenB3}]")
            watcher.debug("TokenB3 Length: ${inputTokens.TokenB3.length}")

            // ===================== B4 =====================
            if (txnDto.posEntryMode.isNullOrEmpty()) txnDto.posEntryMode = "072"

            txnDto.posEntryMode = when (txnDto.pointOfServiceEntryMode) {
                PointOfServiceEntryMode.Inserted     -> "051" // Chip
                PointOfServiceEntryMode.Contactless  -> "071" // CTLS
                else                                 -> "00"
            }
            watcher.debug("posEntryMode → [${txnDto.posEntryMode}]")

            if (txnDto.terminalEntryCapabilities.isNullOrEmpty()) txnDto.terminalEntryCapabilities = "3"
            if (txnDto.lastEmvStat.isNullOrEmpty()) txnDto.lastEmvStat = "1"
            if (txnDto.dataSuspect.isNullOrEmpty()) txnDto.dataSuspect = "0"
            if (txnDto.applPanSeqNum.isNullOrEmpty()) txnDto.applPanSeqNum = "01"
            if (txnDto.devInfo.isNullOrEmpty()) txnDto.devInfo = "1E0300"
            if (txnDto.rsnOnlyCode.isNullOrEmpty()) txnDto.rsnOnlyCode = "1508"
            if (txnDto.arqcVerify.isNullOrEmpty()) txnDto.arqcVerify = "0"
            if (txnDto.isoRCIndicator.isNullOrEmpty()) txnDto.isoRCIndicator = "0"

            watcher.debug("=== TokenB4 VALUES===")
            watcher.debug("posEntryMode                 [${txnDto.posEntryMode}]")
            watcher.debug("terminalEntryCapabilities    [${txnDto.terminalEntryCapabilities}]")
            watcher.debug("lastEmvStat                  [${txnDto.lastEmvStat}]")
            watcher.debug("dataSuspect                  [${txnDto.dataSuspect}]")
            watcher.debug("applPanSeqNum                [${txnDto.applPanSeqNum}]")
            watcher.debug("devInfo                      [${txnDto.devInfo}]")
            watcher.debug("rsnOnlyCode                  [${txnDto.rsnOnlyCode}]")
            watcher.debug("arqcVerify                   [${txnDto.arqcVerify}]")
            watcher.debug("isoRCIndicator               [${txnDto.isoRCIndicator}]")

            inputTokens.TokenB4 =
                txnDto.posEntryMode +
                txnDto.terminalEntryCapabilities +
                txnDto.lastEmvStat +
                txnDto.dataSuspect +
                txnDto.applPanSeqNum +
                txnDto.devInfo +
                txnDto.rsnOnlyCode +
                txnDto.arqcVerify +
                txnDto.isoRCIndicator

            watcher.debug("TokenB4 FINAL: [${inputTokens.TokenB4}]")
            watcher.debug("TokenB4 Length: ${inputTokens.TokenB4.length}")

            // ===================== EZ =====================
            if (txnDto.failCipherCounter.isNullOrEmpty()) txnDto.failCipherCounter = "00"
            if (txnDto.flagTrack2.isNullOrEmpty() || txnDto.flagTrack2 == "0") txnDto.flagTrack2 = "1"
            if (txnDto.cvvFlag.isNullOrEmpty() || txnDto.cvvFlag == "A") txnDto.cvvFlag = "0"
            if (txnDto.clearCvvLength.isNullOrEmpty()) txnDto.clearCvvLength = "00"
            if (txnDto.track1Flag.isNullOrEmpty()) txnDto.track1Flag = "0"

            watcher.debug("pointOfServiceEntryMode [${txnDto.pointOfServiceEntryMode}]")
            txnDto.readCardMode = when (txnDto.pointOfServiceEntryMode) {
                PointOfServiceEntryMode.Inserted     -> "05"
                PointOfServiceEntryMode.Contactless  -> "07"
                else                                 -> "00"
            }
            watcher.debug("readCardMode → [${txnDto.readCardMode}]")

            watcher.debug("=== TokenEZ VALUES ===")
            watcher.debug("keySerialNumber   [${txnDto.keySerialNumber}]")
            watcher.debug("cipherCounter     [${txnDto.cipherCounter}]")
            watcher.debug("failCipherCounter [${txnDto.failCipherCounter}]")
            watcher.debug("flagTrack2        [${txnDto.flagTrack2}]")
            watcher.debug("readCardMode      [${txnDto.readCardMode}]")
            watcher.debug("track2Length      [${txnDto.track2Length}]")
            watcher.debug("cvvFlag           [${txnDto.cvvFlag}]")
            watcher.debug("clearCvvLength    [${txnDto.clearCvvLength}]")
            watcher.debug("track1Flag        [${txnDto.track1Flag}]")
            watcher.debug("cipherData        [${txnDto.cipherData}]")
            watcher.debug("lastPan4Digits    [${txnDto.lastPan4Digits}]")
            watcher.debug("crc32EZ           [${txnDto.crc32EZ}]")

            inputTokens.TokenEZ =
                txnDto.keySerialNumber +
                txnDto.cipherCounter +
                txnDto.failCipherCounter +
                txnDto.flagTrack2 +
                txnDto.readCardMode +
                txnDto.track2Length +
                txnDto.cvvFlag +
                txnDto.clearCvvLength +
                txnDto.track1Flag +
                txnDto.cipherData +
                txnDto.lastPan4Digits +
                txnDto.crc32EZ

            watcher.debug("TokenEZ FINAL: [${inputTokens.TokenEZ}]")
            watcher.debug("TokenEZ Length: ${inputTokens.TokenEZ.length}")

            // ===================== ES =====================
            if (txnDto.terminalSoftwareVersion.isNullOrEmpty()) txnDto.terminalSoftwareVersion = "SMV810v05_02".padEnd(20, ' ')
            if (txnDto.terminalSerialNumber.isNullOrEmpty()) txnDto.terminalSerialNumber = "UNKNOWN_SERIAL".padEnd(20, ' ')
            if (txnDto.cipherConfiguration.isNullOrEmpty()) txnDto.cipherConfiguration = "5"
            if (txnDto.localBinsTableIdPrimary.isNullOrEmpty()) txnDto.localBinsTableIdPrimary = "00000000"
            if (txnDto.localBinsTableIdSecondary.isNullOrEmpty()) txnDto.localBinsTableIdSecondary = "00000000"
            if (txnDto.binsTableVersion.isNullOrEmpty()) txnDto.binsTableVersion = "00"
            if (txnDto.flagRequestNewKey.isNullOrEmpty())
                txnDto.flagRequestNewKey = if (txnDto.transactionType == TransactionType.KeyLoad) "1" else "0"

            watcher.debug("=== TokenES VALUES  ===")
            watcher.debug("terminalSoftwareVersion   [${txnDto.terminalSoftwareVersion}]")
            watcher.debug("terminalSerialNumber      [${txnDto.terminalSerialNumber}]")
            watcher.debug("cipherConfiguration       [${txnDto.cipherConfiguration}]")
            watcher.debug("localBinsTableIdPrimary   [${txnDto.localBinsTableIdPrimary}]")
            watcher.debug("localBinsTableIdSecondary [${txnDto.localBinsTableIdSecondary}]")
            watcher.debug("binsTableVersion          [${txnDto.binsTableVersion}]")
            watcher.debug("flagRequestNewKey         [${txnDto.flagRequestNewKey}]")

            val tokenES =
                txnDto.terminalSoftwareVersion.padEnd(20, '0') +
                txnDto.terminalSerialNumber.padEnd(20, '0') +
                txnDto.cipherConfiguration.padStart(1, '0') +
                txnDto.localBinsTableIdPrimary.padEnd(8, '0') +
                txnDto.localBinsTableIdSecondary.padEnd(8, '0') +
                txnDto.binsTableVersion.padStart(2, '0') +
                txnDto.flagRequestNewKey.padStart(1, '0')

            inputTokens.TokenES = tokenES

            watcher.debug("=== TokenES FINAL STRING ===")
            watcher.debug("TokenES: [${inputTokens.TokenES}]")
            watcher.debug("TokenES Length: ${inputTokens.TokenES.length}")

            // ===================== CZ =====================
            if (txnDto.formFactrInd.length > 8) txnDto.formFactrInd = txnDto.formFactrInd.substring(0, 8)
            if (txnDto.atc.isNullOrEmpty()) txnDto.atc = "005C"
            if (txnDto.formFactrInd.isNullOrEmpty()) txnDto.formFactrInd = "20700000"
            if (txnDto.userFieldACICZ.isBlank()) txnDto.userFieldACICZ = " ".repeat(28)

            watcher.debug("=== TokenCZ values ===")
            watcher.debug("atc (9F36)           [${txnDto.atc}]")
            watcher.debug("formFactrInd (9F6E)  [${txnDto.formFactrInd}]")
            watcher.debug("userFieldACICZ       [${txnDto.userFieldACICZ}]")

            if (txnDto.cardType == CardType.MASTERCARD) {
                try {
                    val tag9F6E = txnDto.formFactrInd
                    watcher.debug("buildTokenCZ is Master")

                    val countryCode9f6e = tag9F6E.substring(0, 4)
                    watcher.debug("buildTokenCZ country code: $countryCode9f6e")

                    val uniqueIdentifier9f6e = tag9F6E.substring(4, 8)
                    watcher.debug("buildTokenCZ uniqueIdentifier: $uniqueIdentifier9f6e")

                    val bcd = StringUtils.stringToBCD(uniqueIdentifier9f6e)
                    val firstByteUniqueIdentifier = bcd[0]
                    val isMsbSet = (firstByteUniqueIdentifier.toInt() and 0x80) != 0
                    watcher.debug("buildTokenCZ is set MSB: $isMsbSet")

                    if (!isMsbSet) {
                        val deviceType = txnDto.formFactrInd.substring(4, 6)
                        watcher.debug("deviceType original: $deviceType")

                        val deviceTypeHex = deviceType.toCharArray()
                            .joinToString("") { it.code.toString(16).uppercase().padStart(2, '0') }
                        watcher.debug("deviceType en ASCII HEX: $deviceTypeHex")

                        txnDto.formFactrInd = deviceTypeHex + "0000"
                        watcher.debug("nuevo formFactrInd: ${txnDto.formFactrInd}")
                    }
                } catch (ex: Exception) {
                    watcher.debug("EXCEPTION (CZ Master): ${ex.message}")
                    txnDto.formFactrInd = "30300000"
                }
            }

            watcher.debug("FORMATTED formFactrInd → [${txnDto.formFactrInd}]")

            val tokenCZ = txnDto.atc + txnDto.formFactrInd + txnDto.userFieldACICZ
            inputTokens.TokenCZ = tokenCZ

            watcher.debug("=== TokenCZ FINAL STRING ===")
            watcher.debug("TokenCZ: [${inputTokens.TokenCZ}]")
            watcher.debug("TokenCZ Length: ${inputTokens.TokenCZ.length}")

            watcher.debug("=== buildEvoInputTokens: END ===")
            return inputTokens
        } 



  

        fun resPayment2TxnResDto(
                txnDtoOrig: TransactionDto,
                evoRes: EvoPaymentResponseDto,
                watcher:Watcher
            ): TransactionResponse {
                watcher.debug("=== resPayment2TxnResDto START ===")
                watcher.debug("txnDtoOrig (entrada) → ${GsonConfig.gson.toJson(txnDtoOrig)}")
                watcher.debug("evoRes (entrada) → ${GsonConfig.gson.toJson(evoRes)}")

                val newDto = TransactionResponse()
                newDto.transactionRequest = txnDtoOrig

                val sale = evoRes.SaleResponse
                watcher.debug("SaleResponse → ${GsonConfig.gson.toJson(sale)}")

                val resDto = TransactionDto()

                // --- Datos básicos de la respuesta ---
                resDto.responseCode =sale.ResponseCode.toString().replace("-1","0").padStart(2, '0')
                watcher.debug("responseCode inicial => ${resDto.responseCode}")

                if(AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode){
                    resDto.responseCode ="00"
                    watcher.debug("responseCode sobrescrito por hostResponseHardcode → ${resDto.responseCode}")
                }
                txnDtoOrig.responseCode= resDto.responseCode
                watcher.debug("txnDtoOrig.responseCode → ${txnDtoOrig.responseCode}")

                val errorCode=ErrorCode.fromResponseCode(resDto.responseCode)
                resDto.errorCodeEnum=errorCode
                resDto.errorCode=errorCode.errorCodeInt
                resDto.errorMessage=errorCode.errorCodeMessage +sale.ResponseMessage  
                watcher.debug("errorCodeEnum → ${resDto.errorCodeEnum}")
                watcher.debug("errorCode → ${resDto.errorCode}")
                watcher.debug("errorMessage → ${resDto.errorMessage}")

                newDto.errorCodeEnum=errorCode
                newDto.errorCode=errorCode.errorCodeInt
                newDto.errorMessage=errorCode.errorCodeMessage

                resDto.responseCodeAuthorizer =resDto.responseCode  
                resDto.errorMessage = sale.ResponseMessage ?: ""
                resDto.authorizationIdResponse = sale.AuthCode ?: ""
                 val authorizationIdResponse = Iso8583MetaData.generateAuthId(txnDtoOrig.hostType)
 
                 if(AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode){
                  resDto.authorizationIdResponse = authorizationIdResponse
                    watcher.debug("authorizationIdResponse sobrescrito resDto.authorizationIdResponse =>${resDto.authorizationIdResponse}")
                }  
                 txnDtoOrig.authorizationIdResponse =  resDto.authorizationIdResponse

                resDto.cardAcceptorTerminalId = sale.TerminalID ?: ""
                resDto.cardAcceptorIdCode = sale.MerchantID ?: ""
                resDto.cardAcceptorNameAndLocation = sale.PlanDescription.ifEmpty {
                    sale.CardDescription ?: ""
                }
                resDto.systemTraceAuditNumber = sale.TerminalTrace.toString()
                resDto.settlementDate = sale.TransmitionDateTime?.substringBefore(" ") ?: ""
                resDto.transactionAmount = String.format("%.2f", sale.Amount ?: 0.0)
                resDto.cardNumber = sale.CardNumber ?: ""
                resDto.transactionCurrencyCode = sale.CurrencyCode.toString()
               // resDto.rrn = sale.AuthRRN ?: ""

                watcher.debug("responseCodeAuthorizer => ${resDto.responseCodeAuthorizer}")
                watcher.debug("additionalResponseDate => ${resDto.additionalResponseDate}")
                watcher.debug("authorizationIdResponse => ${resDto.authorizationIdResponse} HARDCODE:[${authorizationIdResponse}]")
                watcher.debug("cardAcceptorTerminalId =>${resDto.cardAcceptorTerminalId}")
                watcher.debug("cardAcceptorIdCode =>  ${resDto.cardAcceptorIdCode}")
                watcher.debug("cardAcceptorNameAndLocation => ${resDto.cardAcceptorNameAndLocation}")
                watcher.debug("systemTraceAuditNumber=> ${resDto.systemTraceAuditNumber}")
                watcher.debug("settlementDate=>  ${resDto.settlementDate}")
                watcher.debug("transactionAmount=> ${resDto.transactionAmount}")
                watcher.debug("cardNumber => ${resDto.cardNumber}")
                watcher.debug("transactionCurrencyCode=> ${resDto.transactionCurrencyCode}")
                watcher.debug("rrn (AuthRRN) => ${resDto.rrn}")

                // --- Si necesitas emvTagsMap ---
                resDto.rrn= sale.Reference ?: ""
                resDto.fiid=sale.AnswerKey ?: ""
                txnDtoOrig.fiid=sale.AnswerKey ?: ""
                watcher.debug("rrn (Reference) → ${resDto.rrn}")
                watcher.debug("fiid → ${resDto.fiid}")
                watcher.debug("txnDtoOrig.fiid → ${txnDtoOrig.fiid}")

                // --- Software versión del POS ---
                resDto.terminalSoftwareVersion = sale.Configuration.Company.Name ?: ""
                watcher.debug("terminalSoftwareVersion → ${resDto.terminalSoftwareVersion}")

                // --- Estado de la transacción ---
                resDto.transactionStatus = if (resDto.responseCode == "00") {
                    TransactionStatus.Approved
                } else {
                    TransactionStatus.Rejected 
                }
                watcher.debug("transactionStatus → ${resDto.transactionStatus}")

                newDto.transactionResponse = resDto
                watcher.debug("transactionResponse final → ${GsonConfig.gson.toJson(resDto)}")
                watcher.debug("=== resPayment2TxnResDto END ===")
                newDto.transactionRequest = txnDtoOrig

                return newDto
            }


            fun resReturn2TxnResDto(
                txnDtoOrig: TransactionDto,
                evoRes: EvoReturnResponseDto,
                watcher: Watcher
            ): TransactionResponse {
                watcher.debug("=== resReturn2TxnResDto START ===")
                watcher.debug("txnDtoOrig (entrada) → ${GsonConfig.gson.toJson(txnDtoOrig)}")
                watcher.debug("evoRes (entrada) → ${GsonConfig.gson.toJson(evoRes)}")

                val newDto = TransactionResponse() 
                newDto.transactionRequest = txnDtoOrig

                val ret = evoRes.ReturnResponse
                watcher.debug("ReturnResponse → ${GsonConfig.gson.toJson(ret)}")

                val resDto = TransactionDto()

                // --- Datos básicos de la respuesta ---
              //  resDto.responseCode = ret.AuthResultCode ?: ""

                resDto.responseCode =ret.ResponseCode.toString().replace("-1","0") .padStart(2, '0')
 
                watcher.debug("responseCode inicial → ${resDto.responseCode}")

                if (AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode) {
                    resDto.responseCode = "00"
                    watcher.debug("responseCode sobrescrito por hostResponseHardcode → ${resDto.responseCode}")
                }
                txnDtoOrig.responseCode = resDto.responseCode
                watcher.debug("txnDtoOrig.responseCode → ${txnDtoOrig.responseCode}")

                val errorCode = ErrorCode.fromResponseCode(resDto.responseCode)
                resDto.errorCodeEnum = errorCode
                resDto.errorCode = errorCode.errorCodeInt
                resDto.errorMessage = errorCode.errorCodeMessage+ret.ResponseMessage
                watcher.debug("errorCodeEnum → ${resDto.errorCodeEnum}")
                watcher.debug("errorCode → ${resDto.errorCode}")
                watcher.debug("errorMessage → ${resDto.errorMessage}")

                newDto.errorCodeEnum = errorCode
                newDto.errorCode = errorCode.errorCodeInt
                newDto.errorMessage = errorCode.errorCodeMessage

                resDto.responseCodeAuthorizer =resDto.responseCode 

               
                txnDtoOrig.responseCodeAuthorizer =   resDto.responseCodeAuthorizer

                resDto.additionalResponseDate = ret.ResponseMessage ?: ""
                resDto.authorizationIdResponse = ret.AuthCode ?: ""
               if(AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode){
                    resDto.authorizationIdResponse = Iso8583MetaData.generateAuthId(txnDtoOrig.hostType)
                    watcher.debug("authorizationIdResponse sobrescrito resDto.authorizationIdResponse → ${resDto.authorizationIdResponse}")
                }  

                  txnDtoOrig.authorizationIdResponse  =resDto.authorizationIdResponse

                resDto.cardAcceptorTerminalId = ret.TerminalID ?: ""
                resDto.cardAcceptorIdCode = ret.MerchantID ?: ""
                resDto.cardAcceptorNameAndLocation = ret.PlanDescription.ifEmpty { ret.CardDescription ?: "" }
                resDto.systemTraceAuditNumber = ret.TerminalTrace.toString()
                resDto.settlementDate = ret.TransmitionDateTime.substringBefore(" ")
                // Amount viene como String en este DTO
                run {
                    val amt = (ret.Amount ?: "0").toDoubleOrNull() ?: 0.0
                    resDto.transactionAmount = String.format("%.2f", amt)
                }
                resDto.cardNumber = ret.CardNumber ?: ""
                resDto.transactionCurrencyCode = ret.CurrencyCode.toString()
              //  resDto.rrn = ret.AuthRRN ?: ""

                watcher.debug("responseCodeAuthorizer → ${resDto.responseCodeAuthorizer}")
                watcher.debug("additionalResponseDate → ${resDto.additionalResponseDate}")
                watcher.debug("authorizationIdResponse → ${resDto.authorizationIdResponse}")
                watcher.debug("cardAcceptorTerminalId → ${resDto.cardAcceptorTerminalId}")
                watcher.debug("cardAcceptorIdCode → ${resDto.cardAcceptorIdCode}")
                watcher.debug("cardAcceptorNameAndLocation → ${resDto.cardAcceptorNameAndLocation}")
                watcher.debug("systemTraceAuditNumber → ${resDto.systemTraceAuditNumber}")
                watcher.debug("settlementDate → ${resDto.settlementDate}")
                watcher.debug("transactionAmount → ${resDto.transactionAmount}")
                watcher.debug("cardNumber → ${resDto.cardNumber}")
                watcher.debug("transactionCurrencyCode → ${resDto.transactionCurrencyCode}")
                watcher.debug("rrn (AuthRRN) → ${resDto.rrn}")
             
                // --- Campos adicionales simi lares a la función de Sale ---
               // resDto.rrn = txnDtoOrig.originalRrn ?: ""
                //txnDtoOrig.rrn = resDto.rrn ?: ""
                watcher.debug(" txnDtoOrig.rrn → ${ txnDtoOrig.rrn}")
                
                resDto.fiid = ret.AnswerKey ?: ""
                txnDtoOrig.fiid = ret.AnswerKey ?: ""
                watcher.debug("rrn (Reference) → ${resDto.rrn}")
                watcher.debug("fiid → ${resDto.fiid}")
                watcher.debug("txnDtoOrig.fiid → ${txnDtoOrig.fiid}")

                // --- Software versión del POS ---
               // resDto.terminalSoftwareVersion = ret.Configuration.Company.Name ?: ""
                watcher.debug("terminalSoftwareVersion → ${resDto.terminalSoftwareVersion}")

                // --- Estado de la transacción ---
                resDto.transactionStatus = if (resDto.responseCode== "00") {
                    TransactionStatus.Approved
                } else {
                    TransactionStatus.Rejected
                }
                watcher.debug("transactionStatus → ${resDto.transactionStatus}")

                newDto.transactionResponse = resDto
                watcher.debug("transactionResponse final (Return) → ${GsonConfig.gson.toJson(resDto)}")
                watcher.debug("=== resReturn2TxnResDto END ===")

                return newDto
            }

            fun resVoid2TxnResDto(
                txnDtoOrig: TransactionDto,
                evoRes: EvoVoidResponseDto,
                watcher: Watcher
            ): TransactionResponse {
                watcher.debug("=== resVoid2TxnResDto START ===")
                watcher.debug("txnDtoOrig (entrada) → ${GsonConfig.gson.toJson(txnDtoOrig)}")
                watcher.debug("evoRes (entrada) → ${GsonConfig.gson.toJson(evoRes)}")

                val newDto = TransactionResponse()
                newDto.transactionRequest = txnDtoOrig

                val vd = evoRes.VoidResponse
                watcher.debug("VoidResponse → ${GsonConfig.gson.toJson(vd)}")

                val resDto = TransactionDto()

                // --- Datos básicos de la respuesta ---
               // resDto.responseCode = vd.AuthResultCode ?: ""

                resDto.responseCode = vd.ResponseCode.toString().replace("-1","0").padStart(2, '0')

                watcher.debug("responseCode inicial → ${resDto.responseCode} ${vd.ResponseCode }")

                if (AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode) {
                    resDto.responseCode = "00"
                    watcher.debug("responseCode sobrescrito por hostResponseHardcode → ${resDto.responseCode}")
                }
                txnDtoOrig.responseCode = resDto.responseCode
                watcher.debug("txnDtoOrig.responseCode → ${txnDtoOrig.responseCode}")

                val errorCode = ErrorCode.fromResponseCode(resDto.responseCode)
                resDto.errorCodeEnum = errorCode
                resDto.errorCode = errorCode.errorCodeInt
                resDto.errorMessage = errorCode.errorCodeMessage+vd.ResponseMessage


                
                watcher.debug("errorCodeEnum → ${resDto.errorCodeEnum}")
                watcher.debug("errorCode → ${resDto.errorCode}")
                watcher.debug("errorMessage → ${resDto.errorMessage}")

                newDto.errorCodeEnum = errorCode
                newDto.errorCode = errorCode.errorCodeInt
                newDto.errorMessage = errorCode.errorCodeMessage

                resDto.responseCodeAuthorizer = resDto.responseCode 
                resDto.additionalResponseDate = vd.ResponseMessage ?: ""
                resDto.authorizationIdResponse = vd.AuthCode ?: ""
                txnDtoOrig.authorizationIdResponse  = vd.AuthCode ?: ""
                if(AppSettings.getHostConfig(txnDtoOrig.hostType).hostResponseHardcode){
                    resDto.authorizationIdResponse = Iso8583MetaData.generateAuthId(txnDtoOrig.hostType)
                    watcher.debug("authorizationIdResponse sobrescrito resDto.authorizationIdResponse → ${resDto.authorizationIdResponse}")
                }
                txnDtoOrig.responseCodeAuthorizer =   resDto.responseCodeAuthorizer


                resDto.cardAcceptorTerminalId = vd.TerminalID ?: ""
                resDto.cardAcceptorIdCode = vd.MerchantID ?: ""
                resDto.cardAcceptorNameAndLocation = vd.PlanDescription.ifEmpty { vd.CardDescription ?: "" }
                resDto.systemTraceAuditNumber = vd.TerminalTrace.toString()
                resDto.settlementDate = vd.TransmitionDateTime.substringBefore(" ")
                // Amount viene como String en este DTO
                run {
                    val amt = (vd.Amount ?: "0").toDoubleOrNull() ?: 0.0
                    resDto.transactionAmount = String.format("%.2f", amt)
                }
                resDto.cardNumber = vd.CardNumber ?: ""
                resDto.transactionCurrencyCode = vd.CurrencyCode.toString()
            //    resDto.rrn = vd.AuthRRN ?: ""

                watcher.debug("responseCodeAuthorizer → ${resDto.responseCodeAuthorizer}")
                watcher.debug("additionalResponseDate → ${resDto.additionalResponseDate}")
                watcher.debug("authorizationIdResponse → ${resDto.authorizationIdResponse}")
                watcher.debug("cardAcceptorTerminalId → ${resDto.cardAcceptorTerminalId}")
                watcher.debug("cardAcceptorIdCode → ${resDto.cardAcceptorIdCode}")
                watcher.debug("cardAcceptorNameAndLocation → ${resDto.cardAcceptorNameAndLocation}")
                watcher.debug("systemTraceAuditNumber → ${resDto.systemTraceAuditNumber}") 
                watcher.debug("settlementDate → ${resDto.settlementDate}")
                watcher.debug("transactionAmount → ${resDto.transactionAmount}")
                watcher.debug("cardNumber → ${resDto.cardNumber}")
                watcher.debug("transactionCurrencyCode → ${resDto.transactionCurrencyCode}")
                watcher.debug("rrn (AuthRRN) → ${resDto.rrn}")

                // --- Campos adicionales similares a la función de Sale ---
            //    resDto.rrn = txnDtoOrig.rrn ?: ""
                resDto.fiid = vd.AnswerKey ?: ""
                txnDtoOrig.fiid = vd.AnswerKey ?: ""
                watcher.debug("rrn (Reference) → ${resDto.rrn}")
                watcher.debug("fiid → ${resDto.fiid}")
                watcher.debug("txnDtoOrig.fiid → ${txnDtoOrig.fiid}")

                // --- Software versión del POS ---
             //   resDto.terminalSoftwareVersion = vd.Configuration.Company.Name ?: ""
                watcher.debug("terminalSoftwareVersion → ${resDto.terminalSoftwareVersion}")

                // --- Estado de la transacción ---
                resDto.transactionStatus = if (resDto.responseCode== "00") {
                    TransactionStatus.Approved
                } else {
                    TransactionStatus.Rejected
                }
                watcher.debug("transactionStatus → ${resDto.transactionStatus}")

                newDto.transactionResponse = resDto
                watcher.debug("transactionResponse final (Void) → ${GsonConfig.gson.toJson(resDto)}")
                watcher.debug("=== resVoid2TxnResDto END ===")

                return newDto 
            }


}
