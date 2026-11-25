package com.viergegroup.swmain.dtos.logs

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LogsRequestDto : RequestEntity(){


    var initialDateTime: String=""
    var finalDateTime: String=""

    var page=0
    var size=10


    fun initialDateTime2Date():LocalDateTime{
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.parse(initialDateTime, formatter)
            WatcherGlobalLog.info("initialDateTime2Date $date")
            date
        }catch (e:Exception){
            WatcherGlobalLog.error("initialDateTime2Date ${e.message}")

            LocalDateTime.now()
        }
    }
    fun finalDateTime2Date():LocalDateTime{
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.parse(finalDateTime, formatter)
            WatcherGlobalLog.info("finalDateTime2Date $date")
            date
        }catch (e:Exception){
            WatcherGlobalLog.info("finalDateTime2Date ${e.message}")

            LocalDateTime.now()
        }
    }


}