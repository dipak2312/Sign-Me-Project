package com.app.signme.dataclasses.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubscriptionResponse(
    @SerializedName("product_id")
    @Expose
    var vProductId: String? = "",
    @SerializedName("subscription_status")
    @Expose
    var subscription_status: String? = "",
    @SerializedName("purchase_token")
    @Expose
    var purchase_token: String? = ""

) : Parcelable