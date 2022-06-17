package com.app.signme.view.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.dataclasses.ChatListData

class ChatListAdapter(val context: Context, var list: ArrayList<ChatListData>, private val chatClickListener: Context) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cv_chat_messages_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return list.size
    }

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(chatListData: ChatListData) {
            val textPersonName = itemView.findViewById(R.id.tvPersonName) as TextView
            val textMessage  = itemView.findViewById(R.id.tvChatMessage) as TextView
            val textMessageTime  = itemView.findViewById(R.id.tvMessageTime) as TextView
            val textChatCount  = itemView.findViewById(R.id.tvChatCount) as TextView
            textPersonName.text = chatListData.personName
            textMessage.text = chatListData.message
            textMessageTime.text = chatListData.messageTime
            textChatCount.text = chatListData.messageCount
        }
    }
}