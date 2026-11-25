package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.VaultData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

 
@Repository
interface VaultDataDBRepository : CrudRepository<VaultData, Long> {

    @Transactional
    @Procedure("save_vault_data")
    fun saveVaultData(@Param("p_vault_group") vaultGroup: String,@Param("p_vault_key") vaultKey: String, @Param("p_vault_data") vaultData: String): String

    @Transactional(readOnly = true) 
    @Procedure("get_vault_data")
    fun getVaultData(@Param("p_vault_group") vaultGroup: String,@Param("p_vault_key") vaultKey: String): String
}
  