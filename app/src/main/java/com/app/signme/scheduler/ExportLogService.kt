package com.app.signme.scheduler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.app.signme.BuildConfig
import com.app.signme.R
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.repository.FeedbackRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Date/Time of the change  : 18/09/2020
 * Developer Name: Rohit Salvi
 * Change Name: Export log to admin
 * */
class ExportLogService : LifecycleService() {

    private val NOTIFICATION_ID: Int = 909

    companion object {
        private const val TITLE = "Sending Logs to Admin"
        const val CHANNEL_ID = "com.app.whitelabel.LOCAL_CHANNEL"
        const val KEY_FILE_URI = "file_uri_string"

        private fun getNotificationBuilder(context: Context, isAutoCancel: Boolean): Notification {
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setContentTitle(TITLE)
                setProgress(0, 0, true)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(isAutoCancel)
            }

            return notificationBuilder.build()
        }

        private fun updateNotification(context: Context, message: String): Notification {
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setContentTitle(TITLE)
                setContentText(message)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
            }

            return notificationBuilder.build()
        }

        fun getStringMultipartBodyPart(key: String, file: File): List<MultipartBody.Part> {
            val filebody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            return listOf(MultipartBody.Part.createFormData(key, file.name, filebody))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel(this)
        val notification = getNotificationBuilder(this, false)

        val file = intent?.getSerializableExtra(KEY_FILE_URI) as File
        val map = HashMap<String, RequestBody>()
        map["feedback"] = WebServiceUtils.getStringRequestBody("Log file for Android")
        map["images_count"] = WebServiceUtils.getStringRequestBody("1")
        map["device_type"] =
            WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["app_version"] =
            WebServiceUtils.getStringRequestBody("Version: " + BuildConfig.VERSION_NAME + " Version Code: " + BuildConfig.VERSION_CODE)

        val request =
            FeedbackRepository((this.application as AppineersApplication).applicationComponent.getNetworkService()).sendFeedback(
                files = getStringMultipartBodyPart("image_1", file),
                dataMap = map
            )

        val schedulerProvider =
            (this.application as AppineersApplication).applicationComponent.getSchedulerProvider()

        (this.application as AppineersApplication).applicationComponent.getCompositeDisposable()
            .addAll(request.subscribeOn(schedulerProvider.io()).subscribe(
                { response ->
                    stopSelf()
                    if (response.settings?.success == "1") {
                        val notif = updateNotification(this, "Log sent successfully.")
                        val managerCompat =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        managerCompat.notify(NOTIFICATION_ID, notif)
                    } else {
                        val notif = updateNotification(this, "Error while sending.")
                        val managerCompat =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        managerCompat.notify(NOTIFICATION_ID, notif)
                    }
                },
                { _ ->
                    stopSelf()
                    val notif = updateNotification(this, "Error while sending.")
                    val managerCompat =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    managerCompat.notify(NOTIFICATION_ID, notif)
                }
            ))


        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "whitelabel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.setShowBadge(false)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}