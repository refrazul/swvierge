package com.viergegroup.swmain.dbmodels

import java.time.LocalDateTime
import javax.persistence.*
import com.amazonaws.services.dynamodbv2.xspec.N

@Entity
@Table(name = "vault_data")
 class VaultData{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vault_id")
    val vaultId: Long? = null

    @Column(name = "vault_key", nullable = false, unique = true)
    val vaultKey: String=""

    @Column(name = "vault_data", nullable = false)
    val vaultData: String=""
 
    @Column(name = "creation_timestamp", nullable = false, updatable = false)
    val creationTimestamp: LocalDateTime=LocalDateTime.now()

    @Column(name = "modification_timestamp")
    val modificationTimestamp: LocalDateTime=LocalDateTime.now()
}