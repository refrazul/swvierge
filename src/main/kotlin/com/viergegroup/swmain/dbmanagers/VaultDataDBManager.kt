package com.viergegroup.swmain.dbmanagers

import com.shared.commons.ErrorCode 
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.decodeHex
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbservices.ClientActivityDBService
import com.viergegroup.swmain.dbservices.TransactionsMetaDataDBService 
import com.viergegroup.swmain.dbservices.VaultDataDBService
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto 
 
import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import com.viergegroup.swmain.dbmodels.ReportType
import com.viergegroup.swmain.entities.catalogs.HostType 

internal object   VaultDataDBManager {
    private lateinit var vaultDataDBService: VaultDataDBService
    fun init( vaultDataDBService: VaultDataDBService){
        this.vaultDataDBService=vaultDataDBService
    } 
    fun saveVaultData( vaultGroup: String,vaultKey: String, vaultData: String): String {
       return  vaultDataDBService.saveVaultData(vaultGroup,vaultKey,vaultData)
  
    } 
  
    fun getVaultData( vaultGroup: String,vaultKey: String): String {
        return vaultDataDBService.getVaultData(vaultGroup,vaultKey)

    } 
 
 
} 