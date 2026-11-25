package com.viergegroup.swmain.dtos.iso

import com.viergegroup.swmain.entities.ResponseEntity

class Iso8583TransactionMetaDto: ResponseEntity()  {

    var iso8583MetaDataRequest= Iso8583MetaDto()
    var iso8583MetaDataResponse= Iso8583MetaDto()



}