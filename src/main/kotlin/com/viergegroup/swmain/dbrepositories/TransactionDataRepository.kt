package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.TransactionDBData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import org.springframework.data.jpa.repository.Modifying
import java.math.BigDecimal 



@Repository
interface TransactionDataRepository : CrudRepository<TransactionDBData, Long> {

    @Modifying
    @Transactional
    @Procedure("update_transaction_status")
    fun updateTransactionStatus(
            @Param("p_transaction_id") pTransactionId: Long,
            @Param("p_transaction_status") transactionStatus: String
    )    :Long?
    
    @Transactional
    @Procedure("get_txns_tobe_reversed")
    fun getTxnsToBeReversed( 
        @Param("p_host_type") hostType: String
    ): List<Long> 

    @Transactional 
    @Procedure("update_iso_request_by_uuid")
    fun updateIsoRequestByUuid( 
         @Param("p_uuid") uuid: ByteArray, 
         @Param("p_meta_data") metaData: ByteArray,

        @Param("p_isoasciirequest") isoAsciiRequest: ByteArray,
        @Param("p_time_zone") timeZone: String
    ): Long?
  

     //LACPI2
     @Transactional(readOnly = true)
    @Procedure("get_transaction_by_type_and_rrn")
    fun getTransactionByTypeAndRrn( 
        @Param("p_rrn") rrn: String, 
        @Param("p_transaction_type") transactionType: String
    ): List<TransactionDBData>



    @Transactional
    @Procedure("update_iso_response_by_uuid")
    fun updateIsoResponseByUuid(
              @Param("p_uuid") uuid: ByteArray,
        @Param("p_isoasciiresponse") isoAsciiResponse: ByteArray,
        @Param("p_time_zone") timeZone: String
    ): Long?  
  /*  
   
    @Transactional
    @Procedure("update_received_ws_client_timeout")
    fun updateReceivedWsClientTimeout(
        @Param("p_host_type") hostType: String,
        @Param("p_tz") tz: String
    ): List<Long>
   
  */


       
    @Transactional(readOnly = true)
    @Procedure("get_transaction_data")
    fun getTransactionData( 
        @Param("p_transaction_id") transactionId: Long,
        @Param("p_host_type") hostType: String,
        @Param("p_transaction_message_type") transactionMessageType: String, 
        @Param("p_transaction_status") transactionStatus: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_rrn") rrn: String,
        @Param("p_authorization_id_response") authorizationIdResponse: String
    ): List<TransactionDBData>

    @Transactional(readOnly = true)
    @Procedure("get_transactions_list")
    fun getTransactionsList(
        @Param("p_server_name") serverName: String,
        @Param("p_operation_id") operationId: String,
        @Param("p_host_type") hostType: String,
        @Param("p_transaction_type") transactionType: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_rrn") rrn: String,
        @Param("p_creation_date_start") creationDateStart: LocalDateTime,
        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_offset") offset: Int,
        @Param("p_limit") limit: Int
    ): List<TransactionDBData>

    @Transactional(readOnly = true)
    @Procedure("get_total_transaction_pages")
    fun getTotalTransactionPages(
        @Param("p_server_name") serverName: String,
        @Param("p_operation_id") operationId: String,

        @Param("p_host_type") hostType: String,
        @Param("p_transaction_type") transactionType: String,

        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_rrn") rrn: String,
        @Param("p_creation_date_start") creationDateStart: LocalDateTime,

        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_page_size") pageSize: Int
    ): Int

     @Transactional
    @Procedure("insert_transaction_data")
    fun insertTransactionData(
        
        @Param("p_is_initial") isInitial: Boolean, 

        @Param("p_time_zone") timeZone: String,
 
        @Param("p_creation_date_time") creationDateTime: LocalDateTime,
        @Param("p_last_modification_date_time") lastModificationDateTime: LocalDateTime,


        @Param("p_error_code") errorCode: String,
        @Param("p_error_code_number") errorCodeNumber: Int,
        @Param("p_error_message") errorMessage: String,  

 
        @Param("p_card_bin") cardBin: String,  

        @Param("p_card_number") cardNumber: String,  
        @Param("p_affiliation")  affiliation: String,  
        @Param("p_bank_name")   bankName: String,  
        @Param("p_merchant_name")   merchantName: String,  
        @Param("p_merchant_type_code")  merchantTypeCode: String,   
        @Param("p_merchant_address")  merchantAddress: String,  

        

         @Param("p_operation_id") operationId: String,
         @Param("p_device_type") deviceType: String, 
         @Param("p_client_id") clientId: String,
         @Param("p_fiid") fiid: String,
        @Param("p_transaction_id_mpp") transactionIdMpp: String,

        @Param("p_authorization_id_response") authorizationIdResponse: String,
        @Param("p_bank") bank: String,
        @Param("p_card_type") cardType: String,
        @Param("p_client_email") clientEmail: String, 
        @Param("p_clientip") clientIP: String,
        @Param("p_client_location") clientLocation: String,
        @Param("p_client_user_name") clientUserName: String,
        @Param("p_db_kcv") dbKcv: String,
        @Param("p_device_model") deviceModel: String,
        @Param("p_emv_data") emvData: ByteArray,
        @Param("p_host_type") hostType: String,
        @Param("p_isoasciirequest") isoAsciiRequest: ByteArray,
        @Param("p_isoasciiresponse") isoAsciiResponse: ByteArray,
        @Param("p_key_serial_number") keySerialNumber: String,
        @Param("p_merchant_type") merchantType: String,
        @Param("p_meta_data") metaData: ByteArray,
        @Param("p_original_transaction_id") originalTransactionId: Long?,
        @Param("p_payment_plan_type") paymentPlanType: String,
        @Param("p_point_of_service_entry_mode") pointOfServiceEntryMode: String,
        @Param("p_response_code") responseCode: String,

 
        @Param("p_rrn") rrn: String,
		 @Param("p_system_trace_audit_number") systemTraceAuditNumber: String,
     
 
        @Param("p_server_name") serverName: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_transaction_amount") transactionAmount: String,
        @Param("p_transaction_direction") transactionDirection: String,
        @Param("p_transaction_message_type") transactionMessageType: String,
        @Param("p_transaction_status") transactionStatus: String,
        @Param("p_transaction_type") transactionType: String,

        
        @Param("p_transaction_currency_code_number") transactionCurrencuCodeNumber: Int,
  
        @Param("p_ram_total") ramTotal: BigDecimal,
        @Param("p_ram_used") ramUsed: BigDecimal,
        @Param("p_ram_usage_percentage") ramUsagePercentage: BigDecimal,
        @Param("p_storage_total") storageTotal: BigDecimal,
        @Param("p_storage_used") storageUsed: BigDecimal,
        @Param("p_storage_usage_percentage") storageUsagePercentage: BigDecimal,
        @Param("p_network_usage_percentage") networkUsagePercentage: BigDecimal,
        @Param("p_cpu_usage_percentage") cpuUsagePercentage: BigDecimal,

  

        @Param("p_uuid") uuid: ByteArray 
    ): Long?
}
