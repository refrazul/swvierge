package com.viergegroup.swmain.dbservices

import com.viergegroup.swmain.dbmodels.ClientActivityDBData
import com.viergegroup.swmain.dbrepositories.ClientActivityDBRepository
import com.viergegroup.swmain.dtos.clientactivity.ClientActivityFilterDto
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
//
@Service 
class ClientActivityDBService { 
    @Autowired
    lateinit var clientActivityDataRepository: ClientActivityDBRepository
    @Transactional(readOnly = true)
    fun getClientActivityDataList(dto: ClientActivityFilterDto): List<ClientActivityDBData> {

        WatcherGlobalLog.debug("getClientActivityDataList  dto.startDate ${dto.startDate} dto.endDate ${dto.endDate}")
        return clientActivityDataRepository.getClientActivityDataList(
            dto.clientEmail,
            dto.clientUserName, 
            dto.terminalSerialNumberFilter,
            dto.startDate,
            dto.endDate,
            dto.startRow,
            dto.totalRows
        )
    }

    @Transactional(readOnly = true)
    fun getTotalClientActivityPages(dto: ClientActivityFilterDto): Int {
        WatcherGlobalLog.debug("getTotalClientActivityPages  dto.startDate ${dto.startDate} dto.endDate ${dto.endDate}")

        return clientActivityDataRepository.getClientActivityPages(
            dto.clientEmail,
            dto.clientUserName,
            dto.terminalSerialNumberFilter,
            dto.startDate,
            dto.endDate,
            dto.totalRows
        )
    }

    @Transactional
    fun upsertClientActivityData(data: ClientActivityDBData): Long? {
        return clientActivityDataRepository.upsertClientActivityData(
            data.accountLocked,
            data.accountSuspicious,
            data.clientEmail,
            data.clientGeolocation,
            data.clientIP,
            data.clientUserName,
            data.clonnedRequests,
            data.creationDateTime?.toLocalDate(),
            data.deviceModel,
            data.failedLoginAttempts,
            data.frequentLocation,
            data.isBlocked,
            data.lastLoginDateTime?.toLocalDate(),
            data.lastModificationDateTime.toLocalDate(),
            data.lastTransactionDateTime?.toLocalDate(),
            data.numberOfDevices,
            data.numberOfTransactions,
            data.reasonForBlocking,
            data.registrationDateTime?.toLocalDate(),
            data.suspiciousActivityDetected,
            data.terminalSerialNumber,
            data.totalAmountSpent
        )
    }
}
