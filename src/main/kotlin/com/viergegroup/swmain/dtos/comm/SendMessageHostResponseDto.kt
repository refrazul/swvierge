package com.viergegroup.swmain.dtos.comm

import com.viergegroup.swmain.entities.ResponseEntity

class SendMessageHostResponseDto: ResponseEntity()  {
    var hex=""
    var ascii=""
   private var byteArray=ByteArray(0)

    fun getIsoByteArray():ByteArray{
        return byteArray
    }
    fun setIsoByteArray(byteArray: ByteArray){
this.byteArray=byteArray
    }

}