package com.app.signme.dataclasses

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubscriptionPlan(
    @SerializedName("plan_name")
    @Expose
    var planName: String? = "",
    @SerializedName("plan_amount")
    @Expose
    var planAmount: String? = "",
    @SerializedName("plan_amount_for_display")
    @Expose
    var planAmountForDisplay: String? = "",
    @SerializedName("plan_validity_in_days")
    @Expose
    var planValidityInDays: String? = "",
    var isSelected: Boolean? = false,
    var type: String? = ""
) : Parcelable