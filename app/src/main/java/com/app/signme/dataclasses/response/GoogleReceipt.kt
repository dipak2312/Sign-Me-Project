package com.app.signme.dataclasses.response

import com.google.gson.annotations.SerializedName

class GoogleReceipt(

    @field:SerializedName("purchaseState")
    val purchaseState: Int = 0,

    @field:SerializedName("autoRenewing")
    val autoRenewing: Boolean = false,

    @field:SerializedName("acknowledged")
    val acknowledged: Boolean = false,

    @field:SerializedName("purchaseToken")
    val purchaseToken: String = "",

    @field:SerializedName("linkedPurchaseToken")
    val linkedPurchaseToken: String = "",

    @field:SerializedName("orderId")
    val subscriptionId: String = "",

    @field:SerializedName("packageName")
    val packageName: String = "",

    @field:SerializedName("productId")
    val productId: String = "",

    @field:SerializedName("purchaseTime")
    val purchaseTime: Long = 0,

    @field:SerializedName("receipt_type")
    val receiptType: String = "android"
) {

}