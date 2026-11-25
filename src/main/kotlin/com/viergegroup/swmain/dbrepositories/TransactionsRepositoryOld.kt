package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.TransactionDBData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


 
@Repository
interface TransactionsRepositoryOld  : CrudRepository<TransactionDBData, Long> {

    @Transactional
    @Procedure("get_transaction_data_by_auth")
    fun getTransactionDataByAuth(
            @Param("p_authorization_id_response") authorizationIdResponse:String
    ): TransactionDBData?



 
    @Transactional
    @Procedure("get_total_transaction_pages")
    fun getTotalPages(
        @Param("p_server_name") pServerName: String,
        @Param("p_host_type") pHostType: String,
        @Param("p_transaction_type") transactionType: String,

        @Param("p_terminal_serial_number") pTerminalSerialNumber: String,
        @Param("p_rrn") pRrn: String,
        @Param("p_creation_date_start") pCreationDateStart: LocalDateTime,
        @Param("p_creation_date_end") pCreationDateEnd: LocalDateTime,
        @Param("p_page_size") pPageSize: Int
    ): Int

    @Transactional
    @Procedure("get_transactions_history")
    fun getTransactionsHistory(
        @Param("p_server_name") pServerName: String,
        @Param("p_host_type") pHostType: String,
        @Param("p_transaction_type") transactionType: String,

        @Param("p_terminal_serial_number") pTerminalSerialNumber: String,
        @Param("p_rrn") pRrn: String,
        @Param("p_creation_date_start") pCreationDateStart: LocalDateTime,
        @Param("p_creation_date_end") pCreationDateEnd: LocalDateTime,
        @Param("p_offset") pOffset: Int,
        @Param("p_limit") pLimit: Int
    ): List<TransactionDBData>





    @Transactional(readOnly = false)
     @Procedure("insert_transaction_data")
    fun insertTransactionData(
            @Param("p_authorization_id_response") authorizationIdResponse: String,
            @Param("p_card_type") cardType: String,
            @Param("p_db_kcv") dbKcv: String,

            @Param("p_emv_data") emvData: ByteArray,
            @Param("p_host_type") hostType: String,
            @Param("p_isoascii") isoascii: ByteArray,

            @Param("p_key_serial_number") keySerialNumber: String,
            @Param("p_merchant_type") merchantType: String,
            @Param("p_meta_data") metaData: ByteArray,

            @Param("p_original_transaction_id") originalTransactionId: Long,
            @Param("p_payment_plan_type") paymentPlanType: String,
            @Param("p_point_of_service_entry_mode") pointOfServiceEntryMode: String,

            @Param("p_response_code") responseCode: String,
            @Param("p_rrn") rrn: String,
            @Param("p_terminal_serial_number") terminalSerialNumber: String,

            @Param("p_transaction_amount") transactionAmount: String,
            @Param("p_transaction_direction") transactionDirection: String,
            @Param("p_transaction_message_type") transactionMessageType: String,

            @Param("p_transaction_status") transactionStatus: String,
            @Param("p_transaction_type") transactionType: String



    ):Long?

    @Transactional
    @Procedure("get_transaction_data_by_htr")
    fun getTransactionDataByHtr(
            @Param("p_host_type") pHostType: String,
            @Param("p_terminal_serial_number") pTerminalSerialNumber: String,
            @Param("p_rrn") pRrn: String
    ): TransactionDBData?

    @Transactional
    @Procedure("get_transaction_data_by_ar")
    fun getTransactionDataByAr(
            @Param("p_authorization_id_response") authorizationIdResponse:String,
            @Param("p_terminal_serial_number") pTerminalSerialNumber: String

         ): TransactionDBData?



    @Transactional
    @Procedure("get_transaction_data_by_id")
    fun getTransactionDataById(
            @Param("p_transaction_id") pTransactionId: Long
    ): TransactionDBData?


    @Transactional
    @Procedure("get_transaction_data_by_rrn")
    fun getTransactionDataByRrn(
            @Param("p_rrn") pRrn: String
    ): TransactionDBData?




    /*fun findTransactionDBDataByTransactionId(transactionId:Long): TransactionDBData?
    fun findTransactionDBDataByRrn(rrn:String): TransactionDBData?
    fun findTransactionDBDataByAuthorizationIdResponse(authorizationIdResponse:String): List<TransactionDBData>
    fun findTransactionDBDataByRrnAndTerminalSerialNumber(rrn:String,terminalSerialNumber:String):  List<TransactionDBData>
    fun findTransactionDBDataByRrnAndTerminalSerialNumberAndHostType(rrn:String,terminalSerialNumber:String,hostType: HostType):  List<TransactionDBData>
    fun findTransactionDBDataByAuthorizationIdResponseAndTerminalSerialNumber(authorizationIdResponse:String,terminalSerialNumber:String): List<TransactionDBData>

    @Query("SELECT t FROM TransactionDBData t WHERE t.hostType = :hostType AND t.authorizationIdResponse = :authorizationIdResponse")
    fun findTransactionDBDataByHostTypeAndAuthorizationIdResponse(
        @Param("hostType") hostType: HostType,
        @Param("authorizationIdResponse") authorizationIdResponse: String
    ): List<TransactionDBData>*/


}