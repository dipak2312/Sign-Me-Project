package com.app.signme.view.chat

import com.app.signme.dataclasses.ChatListData

interface ChatClickListener {
    fun onChatClick(data: ChatListData)
}