package com.app.signme.view.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.dataclasses.Notification

class NotificationListAdapter(val context: Context, var list: ArrayList<Notification>, private val notificationListener: NotificationClickListener) :
    RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cv_notification_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: NotificationListAdapter.ViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return list.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(notification: Notification) {
            val textNotificationName = itemView.findViewById(R.id.tvNotificationName) as TextView
            val textNotificationMessage  = itemView.findViewById(R.id.tvNotificationBody) as TextView
            val textNotificationTime  = itemView.findViewById(R.id.tvNotificationTime) as TextView
            textNotificationName.text = notification.notificationName
            textNotificationMessage.text = notification.notificationMessage
            textNotificationTime.text = notification.notificationTime
        }
    }
}