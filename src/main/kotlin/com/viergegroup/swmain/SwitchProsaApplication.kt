package com.viergegroup.swmain

import com.shared.commons.AppSettings
import com.viergegroup.swmain.dbservices.*
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.managers.HostMasterManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import com.viergegroup.swmain.managers.MetricsManager
import com.viergegroup.swmain.managers.TxnsCheckerManager


import com.viergegroup.swmain.managers.AlarmTelegramService

import com.viergegroup.swmain.managers.DiskManager
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import com.viergegroup.swmain.dbmanagers.VaultDataDBManager

//sudo -i -u switchapp psql
//sudo -i -u postgres psql

/*
*
*
* DROP DATABASE switchprosa;
CREATE DATABASE switchprosa;
\connect switchprosa; 
sudo systemctl restart postgresql-13.service
*/
@SpringBootApplication
class SwitchProsaApplication 
lateinit var logsMetaDataDBService: LogsMetaDataDBService
lateinit var clientActivityDBService: ClientActivityDBService
lateinit var transactionDataService: TransactionDataService
lateinit var systemStatsDBService: SystemStatsDBService
 lateinit var transactionsMetaDataDBService: TransactionsMetaDataDBService
 lateinit var vaultDataDBService: VaultDataDBService

fun main(args: Array<String>) { 

	val appContext = runApplication<SwitchProsaApplication>(*args)
	val dataPathLinux = appContext.environment.getProperty("custom.linux.datapath").orEmpty().trim()
	val dataPathWin = appContext.environment.getProperty("custom.microsoft.datapath").orEmpty().trim()

	try { 
 
		println("INIT_AppSettings_7")
		AppSettings.init(dataPathLinux!!,dataPathWin!!)
		println("  main_dataPathLinux [${AppSettings.getInstance().dataPathLinux}]")
		println("  main_dataPathMicrosoft [${AppSettings.getInstance().dataPathMicrosoft}]")
	  
	} catch (e: Exception) {
		println("AppSettings EXCEPTION ${e.message}")

	}
	
	val columns = 80
	val rows = 20
	val random = java.util.Random()
	

	
	val asciiArt = """
		_/_/_/  _/          _/      _/      _/    _/_/_/   
	   _/        _/          _/      _/      _/  _/          
		_/_/    _/    _/    _/      _/      _/  _/  _/_/     
		   _/    _/  _/  _/          _/  _/    _/    _/      
	_/_/_/        _/  _/              _/        _/_/_/     
			===========================                
			:: SW-VG ::               (${AppSettings.getInstance().swVGVersion})
	""".trimIndent()
	/*
	for (char in asciiArt) {
		print(char)
		Thread.sleep(10)
	}

		for (char in asciiArt) {
			print(char)
			Thread.sleep(10)
		}*/

	try {
		println("databaseEnabled ${AppSettings.getInstance().databaseEnabled}")
		//val snifferResult=AlarmTelegramService.executeSniffer() 
		AlarmEmailService.sendAlarmEmail(AlarmCause.SYSTEM_INITIALIZED,asciiArt)
              
		if(AppSettings.getInstance().databaseEnabled) {
			println("INIT BEANS")
			logsMetaDataDBService = appContext.getBean(LogsMetaDataDBService::class.java)
			clientActivityDBService = appContext.getBean(ClientActivityDBService::class.java)
			transactionDataService = appContext.getBean(TransactionDataService::class.java)
			systemStatsDBService = appContext.getBean(SystemStatsDBService::class.java)
			transactionsMetaDataDBService = appContext.getBean(TransactionsMetaDataDBService::class.java)
			vaultDataDBService = appContext.getBean(VaultDataDBService::class.java)
   
			
			println("BEANS INITIALIZED")


			println("INIT LOGGER")
			Watcher.init(transactionDataService, logsMetaDataDBService,systemStatsDBService, clientActivityDBService,transactionsMetaDataDBService)
			println("LOGGER INITIALIZED")
			println("INIT HOSTS")
			HostMasterManager.init() 
			println("HOSTS INITIALIZED")
		} 

		val vaultVersion="_"+AppSettings.getInstance().vVersion
		val	baseUrl=AppSettings.getInstance().vUrl 
		val simulateHSM=AppSettings.getInstance().simulateHSM
		val isVaultConsole=AppSettings.getInstance().isVaultConsole
		val pandoraLocal=AppSettings.getInstance().pandoraLocal
		val isDocumentVault=AppSettings.getInstance().isDocumentVault
		val maskPCI=  AppSettings.getInstance().maskPCI
		val restartServicesHour= AppSettings.getInstance().restartServicesHour
		val restartServicesMinutes= AppSettings.getInstance().restartServicesMinutes
		DiskManager.startDiskSpaceChecker() 
		MetricsManager.startMonitoring()  
		TxnsCheckerManager.startMonitoring()
		VaultDataDBManager.init(vaultDataDBService)
        println("RESTART_SERVICE_AT ${restartServicesHour}:${restartServicesMinutes}")

		println(" maskPCI [$maskPCI] isDocumentVault [$isDocumentVault]  pandoraLocal [$pandoraLocal] simulateHSM [$simulateHSM] \n vaultVersion [$vaultVersion] isVaultConsole [$isVaultConsole]  \n vUrl [$baseUrl]  \n\n")

		 
		Runtime.getRuntime().addShutdownHook(Thread {
			AlarmEmailService.sendAlarmEmail(AlarmCause.SYSTEM_STOPPED,asciiArt)
 
		}) 
	} catch (e: Exception) {
		println("EXCEPTION INITIALAZING => [${e.message}]")

	}

}



