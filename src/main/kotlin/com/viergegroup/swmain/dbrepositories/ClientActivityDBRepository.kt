package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.ClientActivityDBData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface ClientActivityDBRepository : CrudRepository<ClientActivityDBData, Long> {

    @Transactional
    @Procedure("upsert_client_activity_data")
    fun upsertClientActivityData(
        @Param("p_account_locked") accountLocked: Boolean,
        @Param("p_account_suspicious") accountSuspicious: Boolean,
        @Param("p_client_email") clientEmail: String,
        @Param("p_client_geolocation") clientGeolocation: String,
        @Param("p_clientip") clientIP: String,
        @Param("p_client_user_name") clientUserName: String,
        @Param("p_clonned_requests") clonnedRequests: Int,
        @Param("p_creation_date_time") creationDateTime: LocalDate?,
        @Param("p_device_model") deviceModel: String,
        @Param("p_failed_login_attempts") failedLoginAttempts: Int,
        @Param("p_frequent_location") frequentLocation: String,
        @Param("p_is_blocked") isBlocked: Boolean,
        @Param("p_last_login_date_time") lastLoginDateTime: LocalDate?,
        @Param("p_last_modification_date_time") lastModificationDateTime: LocalDate,
        @Param("p_last_transaction_date_time") lastTransactionDateTime: LocalDate?,
        @Param("p_number_of_devices") numberOfDevices: Int,
        @Param("p_number_of_transactions") numberOfTransactions: Int,
        @Param("p_reason_for_blocking") reasonForBlocking: String,
        @Param("p_registration_date_time") registrationDateTime: LocalDate?,
        @Param("p_suspicious_activity_detected") suspiciousActivityDetected: Boolean,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_total_amount_spent") totalAmountSpent: Double
    ): Long?

    @Transactional(readOnly = true)
    @Procedure("get_client_activity_list")
    fun getClientActivityDataList(
        @Param("p_client_email") clientEmail: String,
        @Param("p_client_user_name") clientUserName: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_creation_date_start") creationDateStart: LocalDateTime,
        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_offset") offset: Int,
        @Param("p_limit") limit: Int
    ): List<ClientActivityDBData>

    @Transactional(readOnly = true)
    @Procedure("get_client_activity_pages")
    fun getClientActivityPages(
        @Param("p_client_email") clientEmail: String,
        @Param("p_client_user_name") clientUserName: String,
        @Param("p_terminal_serial_number") terminalSerialNumber: String,
        @Param("p_creation_date_start") creationDateStart: LocalDateTime,
        @Param("p_creation_date_end") creationDateEnd: LocalDateTime,
        @Param("p_page_size") pageSize: Int
    ): Int
}
