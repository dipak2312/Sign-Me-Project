package com.app.signme.dataclasses

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class Reason (
    @field:SerializedName("reason_id")
    @field:JsonProperty("reason_id")
    val reasonId: String? = "",
    @field:SerializedName("reason_name")
    @field:JsonProperty("reason_name")
    val reasonName: String? = "",
    @field:JsonProperty("reason_type")
    val reasonType: String? = ""
): Parcelable {
}