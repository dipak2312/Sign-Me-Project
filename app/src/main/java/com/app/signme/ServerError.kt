package com.app.signme

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ServerError(
    @Expose
    @SerializedName("code")
    val code: Int = 0,
    @Expose
    @SerializedName("success")
    val success: String = "1",
    @Expose
    @SerializedName("message")
    val message: String = "Something went wrong",
)