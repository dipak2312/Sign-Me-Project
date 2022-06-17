package com.app.signme.dataclasses

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Notification(
    @SerializedName("message_id")
    @Expose
    var messageId: String? = "",
    @SerializedName("notification_name")
    @Expose
    var notificationName: String? = "",
    @SerializedName("notification_message")
    @Expose
    var notificationMessage: String? = "",
    @SerializedName("notification_time")
    @Expose
    var notificationTime: String? = "",
) : Parcelable