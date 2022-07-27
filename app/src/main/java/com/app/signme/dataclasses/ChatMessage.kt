package com.app.signme.dataclasses

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMessage(
    @field:SerializedName("content")
    var content: String? = "",
    @field:SerializedName("id")
    var id: String? = "",
    @field:SerializedName("created")
    var created: Timestamp? = null,
    @field:SerializedName("senderID")
    var senderID: String? = "",
    @Expose(
        deserialize = false,
        serialize = false
    )
    @field:SerializedName("url")
    var url: String? = "",
    @field:SerializedName("senderName")
    var senderName: String? = "",
    @field:SerializedName("isLog")
    var isLog: Boolean? = false,

    var isShowTime: Boolean = false,
    var chatTimeStamp: String = "",
) : Parcelable{

    fun getMessageTime():String{
        return ""
    }
}
