package com.app.signme.dataclasses

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatListData(
    @SerializedName("message_id")
    @Expose
    var messageId: String? = "",
    @SerializedName("person_name")
    @Expose
    var personName: String? = "",
    @SerializedName("message")
    @Expose
    var message: String? = "",
    @SerializedName("message_time")
    @Expose
    var messageTime: String? = "",
    @SerializedName("message_count")
    @Expose
    var messageCount: String? = "",
) : Parcelable