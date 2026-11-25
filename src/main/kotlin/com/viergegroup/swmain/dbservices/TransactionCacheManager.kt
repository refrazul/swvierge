package com.viergegroup.swmain.dbservices

import com.shared.commons.GsonConfig

import com.shared.commons.AppSettings
import com.shared.commons.DynamicISOFields
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dtos.transactions.TransactionFilterDto
import com.viergegroup.swmain.dbrepositories.TransactionDataRepository
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.managers.DBCipherManager
import com.viergegroup.swmain.webwatcher.Watcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.managers.MetricsManager
import kotlinx.coroutines.*
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause

import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object TransactionCacheManager {
    private val cacheDir = File("/etc/swvierge/cache/")
    private var recoveredOnce = false

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }

    fun saveToCache(data: TransactionDBData) {
        try {
            val uuid=StringUtils.encodeHexString(data.uuid)
            val cacheFile = File(cacheDir, "$uuid.txn")
            val json = GsonConfig.gsonRaw.toJson(data)
            cacheFile.writeText(json)
        } catch (e: Exception) {
            println("⚠️ Error saving to cache: ${e.message}")
        }
    }

    fun deleteCache(uuid: ByteArray) {
        val uuidS=StringUtils.encodeHexString(uuid)
        val cacheFile = File(cacheDir, "$uuidS.txn")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    @Synchronized
    fun recoverCachedTransactions(watcher: Watcher): List<TransactionDBData> {
        if (recoveredOnce) return emptyList()
        recoveredOnce = true
    
        val thresholdMillis = System.currentTimeMillis() - (7 * 60 * 1000) // 7 minutos
        val recoveredList = mutableListOf<TransactionDBData>()
    
        cacheDir.listFiles { file ->
            file.extension == "txn" && file.lastModified() < thresholdMillis
        }?.forEach { file ->
            try {
                val data = GsonConfig.gsonRaw.fromJson(file.readText(), TransactionDBData::class.java)
                recoveredList.add(data)
                file.delete()
            } catch (e: Exception) {
                watcher.error("⚠️ Error recovering cached transaction from ${file.name}: ${e.message}")
            }
        }
    
        return recoveredList
    }
    
}
