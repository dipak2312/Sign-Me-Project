package com.app.signme.scheduler.aws.cacheUtils


import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class UploadSuccessCallback(
    @JsonProperty("local_path")
    @SerializedName("local_path")
    var localPath: String?="",

    @JsonProperty("message")
    @SerializedName("message")
    var message: String?="",

    @JsonProperty("callback_key")
    @SerializedName("callback_key")
    var callbackKey: String="",

    @JsonProperty("status")
    @SerializedName("status")
    var status: Int=-1,

    @JsonProperty("position")
    @SerializedName("position")
    var position: Int=-20
) : Parcelable