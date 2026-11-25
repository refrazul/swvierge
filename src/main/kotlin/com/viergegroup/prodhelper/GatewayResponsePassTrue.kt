package com.viergegroup.prodhelper

import com.google.gson.annotations.SerializedName

// Define la clase GatewayResponse
data class GatewayResponsePassTrue(
    @SerializedName("id") val id: String,
    @SerializedName("source") val source: String,
    @SerializedName("status") val status: String,
    @SerializedName("actionCode") val actionCode: String,
    @SerializedName("emvResponsePayload") val emvResponsePayload: String,
    @SerializedName("meta") val meta: String
)
 
// Define la clase Wrapper para GatewayResponse
data class GatewayResponseWrapperPassTrue(
    @SerializedName("gatewayResponse") val gatewayResponse: GatewayResponsePassTrue
)