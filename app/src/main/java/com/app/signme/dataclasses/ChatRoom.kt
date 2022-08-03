package com.app.signme.dataclasses
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class ChatRoom(
    @field:SerializedName("receiverId")
    var receiverId: String? = "",

    @field:SerializedName("receiverName")
    var receiverName: String? = "",

    @field:SerializedName("receiverProfileImage")
    var receiverProfileImage: String? = "",

    @field:SerializedName("lastMessage")
    var lastMessage: String? = "",

    @field:SerializedName("senderId")
    var senderID: String? = "",

    @field:SerializedName("senderImage")
    var senderImage: String? = "",

    @field:SerializedName("senderName")
    var senderName: String? = "",

    @field:SerializedName("created")
    var created: Timestamp? = null,

    @field:SerializedName("id")
    var id: String? = "",

    @field:SerializedName("isTyping")
    var isTyping: String? = "",

    @field:SerializedName("docId")
    var docId: String? = "",
    @field:SerializedName("deletedBy")
    var deletedBy: String? = "",
    @field:SerializedName("senderFireBaseId")
    var senderFireBaseId: String? = "",
    @field:SerializedName("chatID")
    var chatID: String? = "",
    @field:SerializedName("chatCount")
    var chatCount: Long? = null,
    @field:SerializedName("friendStatus")
    var friendStatus: String? = "",
    @field:SerializedName("matchDate")
    var matchDate: String? = ""

) : Parcelable {
    fun getMessageCreatedTime(): String? {
        val formattedTime = created?.let {
            formatDateFromTimeStamp(
                it.toDate()
            )
        }
        return formattedTime ?: ""
    }

    fun formatDateAndTimeFromTimeStamp(timestampDate: Date): String? {
        var formattedDateTime: String? = ""
        val cal: Calendar = Calendar.getInstance()
        cal.timeInMillis = timestampDate.time
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        formattedDateTime = dateFormat.format(cal.time)

        return formattedDateTime
    }

    fun formatDateFromTimeStamp(timestampDate: Date): String? {
        var formattedDateTime: String? = ""
        val cal: Calendar = Calendar.getInstance()
        cal.timeInMillis = timestampDate.time
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        formattedDateTime = dateFormat.format(cal.time)

        return formattedDateTime
    }
}