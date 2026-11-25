package com.viergegroup.swmain.dbservices

import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import com.viergegroup.swmain.dbmodels.ReportType
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.dbrepositories.VaultDataDBRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import java.math.BigDecimal  
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
@Service
class VaultDataDBService {
   
    @Autowired  
    lateinit var vaultDataRepository: VaultDataDBRepository
    @Transactional
    fun saveVaultData( vaultGroup: String,vaultKey: String, vaultData: String): String {
        try { 
            WatcherGlobalLog.debug("saveVaultData - Entrada:vaultGroup=$vaultGroup, vaultKey=$vaultKey $vaultData")
            val results = vaultDataRepository.saveVaultData(vaultGroup,vaultKey,vaultData)
            return results   
        } catch (e: Exception) {  
            WatcherGlobalLog.error("saveVaultData - Error: ${e.message}")
            return ""
        } 
    }
    
    @Transactional
    fun getVaultData( vaultGroup: String,vaultKey: String): String {
        try { 
            WatcherGlobalLog.debug("getVaultData - Entrada: vaultGroup=$vaultGroup, vaultKey=$vaultKey ")
            val results = vaultDataRepository.getVaultData(vaultGroup,vaultKey)
            return results   
        } catch (e: Exception) {  
            WatcherGlobalLog.error("getVaultData - Error: ${e.message}")
            return ""
        } 
    } 


}  