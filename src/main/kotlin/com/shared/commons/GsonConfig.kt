package com.shared.commons 

import com.google.gson.GsonBuilder
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dtos.gateway.GatewayResponseMeta
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import com.viergegroup.swmain.dtos.pandora.PandoraResponse
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponseDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.dtos.gateway.GatewayRequestDto
import com.viergegroup.swmain.dtos.vault.BankRsaKeyResponse
import com.viergegroup.swmain.dtos.vault.DukptDataResponse
import com.viergegroup.swmain.dtosgsonadapters.*
import java.time.LocalDateTime
import com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters.*
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionDto
 
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionInstanceDto

object GsonConfig { 
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
       // .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())

        .create()
 
    val gsonLogs = GsonBuilder()
        .setPrettyPrinting() 
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(BankRsaKeyResponse::class.java, BankRsaKeyResPCIGSONAdapter())
        .registerTypeAdapter(DukptDataResponse::class.java, DukptDataResponseGSONAdapter())
        .registerTypeAdapter(DBKeyResponseDto::class.java, DBKeyResPCIDtoGSONAdapter())
         
        .registerTypeAdapter(VaultDtos.DukptMetaResponseMin::class.java, DukptMetaResponseMinGSONAdapter())
        .registerTypeAdapter(MyHsmDto.DUKPTResponse::class.java, DUKPTResponseGSONAdapter())
        .registerTypeAdapter(GatewayResponseMeta::class.java, MetaDataPCIGSONAdapter())
        .registerTypeAdapter(PandoraResponse::class.java, PandoraResPCIGSONAdapter())
        .registerTypeAdapter(PandoraTransactionDto::class.java, PandoraTxnPCIDtoGSONAdapter())
        .registerTypeAdapter(TransactionDBData::class.java, TransactionDBDataPCIGSONAdapter())
        .registerTypeAdapter(TransactionDto::class.java, TransactionDtoPCIGSONAdapter())  
        .registerTypeAdapter(TransactionResponseDto::class.java, TransactionResponseDtoGSONResponse())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTermDtoPCIGSONAdapter())
        .registerTypeAdapter(LogsResponseDtoGSONAdapter::class.java, LogsResponseDtoGSONAdapter()) 
        .registerTypeAdapter(GatewayTransactionInstanceDto::class.java, GatewayTransactionInstancePCIGSONAdaptDto())  
        .registerTypeAdapter(GatewayTransactionDto::class.java, GatewayTransactionPCIGsonAdapterDto())  
        .registerTypeAdapter(GatewayRequestDto::class.java, GatewayTxnReqPCIGsonAdapterDto())  

        .registerTypeAdapter(VaultDtos.CalculateHashResponse::class.java, CalculateHashResponseGSONAdapter())
        .create()   
 

    val gsonReverse = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())
        .registerTypeAdapter(TransactionDto::class.java,TransactionDtoGSONAdapter())
        .create()


    val gsonRaw = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())
        .create()
   
    val gsonB64 = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())
      //  .registerTypeAdapter( GatewayResponseMeta::class.java,  MetaDataGSONAdapter() ) 
        .create()
    val gsonDecodeB64 = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())

        .create()
 


    val gsonPrint = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(BankRsaKeyResponse::class.java, BankRsaKeyResponseGSONAdapter())
        .registerTypeAdapter(DBKeyResponseDto::class.java, DBKeyResponseDtoGSONAdapter())
        .registerTypeAdapter(DukptDataResponse::class.java, DukptDataResponseGSONAdapter())
        .registerTypeAdapter(VaultDtos.DukptMetaResponseMin::class.java, DukptMetaResponseMinGSONAdapter())
        .registerTypeAdapter(MyHsmDto.DUKPTResponse::class.java, DUKPTResponseGSONAdapter())
        .registerTypeAdapter(GatewayResponseMeta::class.java, MetaDataGSONAdapter())
        .registerTypeAdapter(PandoraResponse::class.java, PandoraResponseGSONAdapter())
        .registerTypeAdapter(PandoraTransactionDto::class.java, PandoraTransactionDtoGSONAdapter())
        .registerTypeAdapter(TransactionDto::class.java, TransactionDtoGSONAdapter())
        .registerTypeAdapter(TransactionResponseDto::class.java, TransactionResponseDtoGSONResponse())
 
        .registerTypeAdapter(LogsResponseDtoGSONAdapter::class.java, LogsResponseDtoGSONAdapter())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())
        .registerTypeAdapter(TransactionDBData::class.java, TransactionDBDataGSONAdapter())

        .setPrettyPrinting()
        .create()


    val gsonRawResponse = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
        .registerTypeAdapter(BankRsaKeyResponse::class.java, BankRsaKeyResponseGSONAdapter())
        .registerTypeAdapter(DBKeyResponseDto::class.java, DBKeyResponseDtoGSONAdapter())
        .registerTypeAdapter(VaultDtos.DukptMetaResponseMin::class.java, DukptMetaResponseMinGSONAdapter())
        .registerTypeAdapter(MyHsmDto.DUKPTResponse::class.java, DUKPTResponseGSONAdapter())
        .registerTypeAdapter(GatewayResponseMeta::class.java, MetaDataGSONAdapter())
        .registerTypeAdapter(PandoraResponse::class.java, PandoraResponseGSONAdapter())
        .registerTypeAdapter(PandoraTransactionDto::class.java, PandoraTransactionDtoGSONAdapter())
        .registerTypeAdapter(TransactionDto::class.java, TransactionDtoGSONAdapter())
        .registerTypeAdapter(TransactionResponseDto::class.java, TransactionResponseDtoGSONResponse())
        .registerTypeAdapter(TransactionTerminalDto::class.java, TransactionTerminalDtoGSONAdapter())
        .registerTypeAdapter(LogsResponseDtoGSONAdapter::class.java, LogsResponseDtoGSONAdapter())

        .registerTypeAdapter(VaultDtos.CalculateHashResponse::class.java, CalculateHashResponseGSONAdapter())

        .create()
}