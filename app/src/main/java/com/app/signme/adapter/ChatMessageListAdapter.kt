package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.dataclasses.ChatMessage
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*


class ChatMessageListAdapter(
    var chatMessagesList: List<ChatMessage>,
    val userId: String,
    val user1ImgUrl: String,
    val user2ImgUrl: String,
    val activity: Activity,
    val clickCallbackListener: ClickCallbackListener
) : RecyclerView.Adapter<ChatMessageListAdapter.ViewHolder>() {

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        if (!chatMessagesList[position].url.isNullOrEmpty()) {

            holder.message?.visibility = View.GONE
            holder.messageImage?.visibility = View.VISIBLE

        } else {
            holder.message?.text = chatMessagesList[position].content;
            holder.message?.visibility = View.VISIBLE
            holder.messageImage?.visibility = View.GONE

        }


        if (chatMessagesList[position].isShowTime) {
            holder.messageDate?.text = chatMessagesList[position].chatTimeStamp
            holder.messageDate?.visibility = View.VISIBLE
        } else {
            holder.messageDate?.visibility = View.GONE
        }

        val formattedTime = chatMessagesList[position].created?.let {
            formatDateAndTimeFromTimeStamp(
                it.toDate()
            )
        }

        if (!formattedTime.isNullOrEmpty()) {
            holder.messageTime?.text = formattedTime
            holder.messageTime?.visibility = View.VISIBLE
        } else {
            holder.messageTime?.visibility = View.GONE
        }

        holder.rlChat?.tag = chatMessagesList[position]

        holder.rlChat?.setOnLongClickListener {

            if (chatMessagesList[position].senderID.equals(userId) && chatMessagesList[position].isLog == false) {
                if (holder.rlChat?.tag != null) {
                    clickCallbackListener.onMessageClick(
                        holder.rlChat?.tag as ChatMessage,
                        chatMessagesList[position].content!!
                    )
                }

            }
//            clickCallbackListener.onMessageClick(
//                holder.rlChat?.tag as ChatMessage,
//                chatMessagesList[position].content!!
//            )
            true
        }

        holder.rlChat?.setOnClickListener {
            if (!chatMessagesList[position].content.isNullOrEmpty() && chatMessagesList[position].url.isNullOrEmpty()) {
                if (chatMessagesList[position].senderID.equals(userId) && chatMessagesList[position].isLog == false) {
                    if (holder.rlChat?.tag != null) {
                        clickCallbackListener.onMessageClick(
                            holder.rlChat?.tag as ChatMessage,
                            chatMessagesList[position].content!!
                        )
                    }
                }
            }
        }
    }


    private fun formatDateAndTimeFromTimeStamp(timestampDate: Date): String? {
        var formattedDateTime: String? = ""
        val cal: Calendar = Calendar.getInstance()
        cal.timeInMillis = timestampDate.time
        // val dateFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        formattedDateTime = dateFormat.format(cal.time)

        return formattedDateTime
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = if (viewType == MSG_TYPE_RIGHT) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessagesList[position].isLog == true) MSG_TYPE_LOG else if (chatMessagesList[position].senderID.equals(
                userId
            )
        ) MSG_TYPE_RIGHT else MSG_TYPE_LEFT
    }

    override fun getItemCount(): Int {
        return chatMessagesList.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var message: TextView? = null
        var messageImage: ShapeableImageView? = null
        var messageTime: TextView? = null
        var messageDate: TextView? = null
        var rlChat: LinearLayout? = null

        init {
            message = itemView.findViewById(R.id.tvTextMessage)
            messageDate = itemView.findViewById(R.id.tvMessageDate)
            messageTime = itemView.findViewById(R.id.tvMessageTime)
            messageImage = itemView.findViewById(R.id.ivMessageImage)
            rlChat = itemView.findViewById(R.id.rlChat)

        }
    }

    companion object {
        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1
        const val MSG_TYPE_LOG = 2
    }

    interface ClickCallbackListener {
        fun onMessageClick(data: ChatMessage, contentType: String)
    }
}