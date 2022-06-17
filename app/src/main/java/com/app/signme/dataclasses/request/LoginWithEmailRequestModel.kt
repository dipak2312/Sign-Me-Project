package com.app.signme.dataclasses.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize



@Parcelize
data class LoginWithEmailRequestModel(
    @field:SerializedName("email")
    var email: String = "",
    @field:SerializedName("password")
    var password: String = "",
    @field:SerializedName("device_type")
    var deviceType: String = "",
    @field:SerializedName("device_model")
    var deviceModel: String = "",
    @field:SerializedName("device_os")
    var deviceOs: String = "",
    @field:SerializedName("device_token")
    var deviceToken: String = "",
) : Parcelable