package com.viergegroup.isoprocessor.models.iso

import com.viergegroup.swmain.entities.ResponseEntity
import java.util.*

class Iso8583TransactionMetaData:ResponseEntity() {
    var guid: UUID = UUID.randomUUID();
    var iso8583MetaDataRequest=Iso8583MetaData(guid)
    var iso8583MetaDataResponse=Iso8583MetaData(guid)

}