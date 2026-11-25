package com.viergegroup.swmain.webwatcher

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.OperationId
import java.text.SimpleDateFormat
import java.util.*

object WatcherContext {
    private val watcherThreadLocal = ThreadLocal<Watcher>()

   /*  fun initOperation(requestEntity: RequestEntity):Watcher {
        watcherThreadLocal.set(Watcher(requestEntity))
        println("${getHeader("APP","INFO")} initOperation  not set in the context =>operationIdRequest [${ requestEntity.operationIdRequest}]   requestEntity.hostType ${ requestEntity.hostType}")
 
        return watcherThreadLocal.get() ?: throw IllegalStateException("initOperation  not set in the context")
    } */

    fun initOperation(requestEntity: RequestEntity):Watcher {
        watcherThreadLocal.get()?.close()   // cerrar el Watcher previo antes de reemplazarlo (evita abandonar sus scopes/FDs)
        watcherThreadLocal.set(Watcher(requestEntity))
        println("${getHeader("APP","INFO")} initOperation  not set in the context =>operationIdRequest [${ requestEntity.operationIdRequest}]   requestEntity.hostType ${ requestEntity.hostType}")
 
        return watcherThreadLocal.get() ?: throw IllegalStateException("initOperation  not set in the context")
    } 
    fun initHost( hostType:HostType):Watcher {
        if(watcherThreadLocal.get()!=null){
            return watcherThreadLocal.get()
        }
        val requestEntity= RequestEntity()
        requestEntity.operationIdRequest=OperationId.HostOperation
        requestEntity.hostType=hostType
        watcherThreadLocal.set(Watcher(requestEntity))
        println("${getHeader("APP","INFO")} initHosts not set in the context =>operationIdRequest [${ requestEntity.operationIdRequest}]   requestEntity.hostType ${ requestEntity.hostType}")

        return watcherThreadLocal.get() ?: throw IllegalStateException("initHosts not set in the context")
    }
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
    }


    private fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            if (element.className != this::class.java.name && !element.className.startsWith("java.lang.Thread")) {
                return "${element.className}:${element.lineNumber} (${element.methodName})"
            }
        }
        return "ERROR GETTING INFO CLASS "
    }

    fun getHeader(logType:String,logLevel:String):String{
        return "\t${getCurrentTimestamp()}\t|\t${logType}\t|\t${logLevel}\t|\t${getCallerInfo()}|\n"
    }
    fun getInstance(): Watcher {
        val watcher= watcherThreadLocal.get() ?:null
        if(watcher==null){ 
            val requestEntity=RequestEntity()
            requestEntity.operationIdRequest=OperationId.HostOperation
            requestEntity.hostType=HostType.Vierge
            println("${getHeader("APP","INFO")} getInstance IS NULL  TRY TO INIT IN => requestEntity.operationIdRequest[${ requestEntity.operationIdRequest}]   requestEntity.hostType ${ requestEntity.hostType}")
            return initOperation(requestEntity)
        }

        return watcher
    }
    fun getOptionalInstance(): Watcher? {
        return watcherThreadLocal.get() ?:null
    }

    fun finishOperation() {

        watcherThreadLocal.get()?.close()
        val watcher= watcherThreadLocal.get()
        if(watcher!=null){
            watcherThreadLocal.get()
        }
        watcherThreadLocal.remove()
    }
  /*  fun finishOperation(responseEntity: ResponseEntity) {
        watcherThreadLocal.get()?.close()
        val watcher= watcherThreadLocal.get()
        if(watcher!=null){
           watcher.upsertLogMetaData(responseEntity)
        }
        watcherThreadLocal.remove()
    }*/

}

