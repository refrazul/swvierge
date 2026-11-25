package com.viergegroup.swmain.dtos.transactions

class TransactionLogResponse {
    class TransactionDetailsData{
        var errorCode:Int=0
        var errorMessage:String=""

        var transactionHexRaw:String=""
        var transactionASCIIRaw:String=""
        var transactionData: TransactionDto?=null
        var isoDescription:String=""

    }
    //var metaData=LogDetailedMetaData()
    var transactionLogContent:String=""
    var transactionRequestDetails= TransactionDetailsData()
    var transactionResponseDetails= TransactionDetailsData()
    var webApiResponseDetails= TransactionFullResponseDto()

}