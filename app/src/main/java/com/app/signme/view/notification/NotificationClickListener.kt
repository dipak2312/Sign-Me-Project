package com.app.signme.view.notification

import com.app.signme.dataclasses.Notification

interface NotificationClickListener {
    fun onNotificationClick(data: Notification)
}