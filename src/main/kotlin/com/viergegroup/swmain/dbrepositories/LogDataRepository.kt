package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.LogsMetaData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.math.BigDecimal 



@Repository
interface LogDataRepository : CrudRepository<LogsMetaData, Long> {


    @Transactional(readOnly = true)
    @Procedure("log_exists")
    fun logExists(@Param("p_uuid") uuid: ByteArray): Long

    @Transactional(readOnly = true)
    @Procedure("get_log_meta_list")
    fun getLogs(  
        @Param("p_error_code") errorCode: String,
        @Param("p_server_name") serverName: String,
        @Param("p_host_type") hostType: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_rrn") rrn: String,
        @Param("p_module_id") moduleId: String,
        @Param("p_operation_id") operationId: String,
        @Param("p_transaction_type") transactionType: String,

        @Param("p_creation_date_start") creationDateStart: LocalDateTime,
        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_offset") offset: Int,
        @Param("p_limit") limit: Int
    ): List<LogsMetaData>

    @Transactional(readOnly = true)
    @Procedure("get_log_meta_pages")
    fun getLogsPages(
        @Param("p_error_code") errorCode: String,
        @Param("p_server_name") serverName: String,
        @Param("p_host_type") hostType: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_rrn") rrn: String,
        @Param("p_module_id") moduleId: String,
        @Param("p_operation_id") operationId: String,
        @Param("p_transaction_type") transactionType: String,

        @Param("p_creation_date_start") creationDateStart: LocalDateTime,
        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_page_size") pageSize: Int
    ): Int
    @Transactional
 
    @Procedure("update_log_error_data") 
    fun updateLogErrorData( 
        @Param("p_uuid") uuid: ByteArray,
        @Param("p_error_code") errorCode: String,
        @Param("p_error_code_number") errorCodeNumber: Int,
        @Param("p_error_message") errorMessage: String,
        @Param("p_operation_id") operationId: String,
        @Param("p_operation_duration") operationDuration: Float,
        @Param("p_rrn") rrn: String,
        @Param("p_response_code") responseCode: String,

        @Param("p_card_bin") cardBin: String,
        @Param("p_card_number") cardNumber: String,
        @Param("p_card_type") cardTypé: String,
        @Param("p_affiliation") affiliation: String,
        @Param("p_bank_name") bankName: String,
        @Param("p_merchant_name") merchantName: String,
        @Param("p_merchant_type_code") merchantTypeCode: String,
        @Param("p_merchant_address") merchantAddress: String

    ): Long?




    @Procedure("upsert_log_meta_data")
    fun upsertLogMetaData(
        @Param("p_time_zone") timeZone: String,
        @Param("p_bank") bank: String,
        @Param("p_client_email") clientEmail: String,
        @Param("p_client_geolocation") clientGeolocation: String,
        @Param("p_clientip") clientIP: String,
        @Param("p_client_user_name") clientUserName: String,
        @Param("p_creation_date_time") creationDateTime: LocalDateTime,
        @Param("p_device_model") deviceModel: String,
        @Param("p_error_code") errorCode: String,
        @Param("p_error_code_number") errorCodeNumber: Int,
        @Param("p_error_message") errorMessage: String,
        @Param("p_host_type") hostType: String,
        @Param("p_last_modification_date_time") lastModificationDateTime: LocalDateTime,
        @Param("p_merchant_type") merchantType: String,
        @Param("p_module_id") moduleId: String,
        @Param("p_operation_id") operationId: String,
        @Param("p_point_of_service_entry_mode") pointOfServiceEntryMode: String,
        @Param("p_response_code") responseCode: String,
        @Param("p_rrn") rrn: String,
        @Param("p_server_name") serverName: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_transaction_amount") transactionAmount: String,
        @Param("p_transaction_type") transactionType: String,

        
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
