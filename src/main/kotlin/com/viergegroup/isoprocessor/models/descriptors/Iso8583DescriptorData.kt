package com.viergegroup.isoprocessor.models.descriptors

import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import java.util.*

class Iso8583DescriptorData {
   // var isoHeader = "" 
   var hasTokens:Boolean=true
    var mustParseEmvData:Boolean=false
    var headerDescriptors: ArrayList<HeaderDescriptor> = arrayListOf()
    var fieldDescriptors: ArrayList<Iso8583FieldDescriptor> = arrayListOf()
    var field63Descriptor: Field63Descriptor = Field63Descriptor()
        var field57Descriptor: Field57Descriptor = Field57Descriptor()

    var emvTagDescriptors=Iso8583EmvDescriptor()

    fun hasTokens():Boolean{
        return field63Descriptor.tokenDescriptors.size>0
    }
  
    fun getHeaderLen():Int{
        return getIsoHeader().length
    }
    
 fun getIsoHeader(): String {
    // primero intenta encontrar uno con transactionType = "All"
    val headerDescriptor = headerDescriptors.firstOrNull { it.transactionType == "All" }
        ?: headerDescriptors.firstOrNull()   // fallback seguro
     
    // si aun así es null, devolvemos valores por default
    return headerDescriptor?.headerRequest ?: "ISO026000050"
}

    fun getIsoHeader(transactionData: TransactionData): String {
 
            // buscar por tra nsactionType
            var headerDescriptor = headerDescriptors.firstOrNull {
                it.transactionType == transactionData.transactionType.transactionTypeName
            }

            // fallback a "All"
            if (headerDescriptor == null) {
                headerDescriptor = headerDescriptors.firstOrNull { it.transactionType == "All" }
            }

            // fallback final al primer descriptor disponible
            if (headerDescriptor == null) {
                headerDescriptor = headerDescriptors.firstOrNull()
            }

            // si absolutamente todo es null, devolvemos defaults
            val defaultRequest = "ISO026000050"
            val defaultResponse = "ISO026000045"

            return if (transactionData.transactionDirection == TransactionDirection.Response) {
                headerDescriptor?.headerResponse ?: defaultResponse
            } else {
                headerDescriptor?.headerRequest ?: defaultRequest
            }
        }
} 