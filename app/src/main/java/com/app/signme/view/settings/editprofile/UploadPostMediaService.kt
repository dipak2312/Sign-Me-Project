package com.app.signme.view.settings.editprofile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.app.signme.R
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getStringMultipartBodyPart
import com.app.signme.dataclasses.MediaUpload
import com.app.signme.db.repo.MediaFileRepository
import com.app.signme.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Developer Name: Anita Chipkar
 * Use: To upload user media files.
 * */
class UploadPostMediaService : LifecycleService() {

    private val NOTIFICATION_ID: Int = 909

    var mediaFileRepository: MediaFileRepository? = null

    companion object {
        private const val TITLE = "Uploading post media"
        const val CHANNEL_ID = "com.appineers.whitelabel.LOCAL_CHANNEL"
        const val KEY_FILE_URI = "file_uri_string"
        const val KEY_USER_ID = "user_id"
        const val KEY_FILE_ID = "file_id"

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


        fun getMultipartFile(mediaPath: String): MultipartBody.Part? {
            var file: MultipartBody.Part? = null
            val key = "image"

            getStringMultipartBodyPart(
                key,
                mediaPath
            )?.let {
                file = it
            }
            return file
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel(this)

        mediaFileRepository = MediaFileRepository.getInstance(this)

        val notification = getNotificationBuilder(this, true)

        val file = intent?.getStringExtra(KEY_FILE_URI)
        val userId = intent?.getStringExtra(KEY_USER_ID)
        val fileId = intent?.getStringExtra(KEY_FILE_ID)

        val map = HashMap<String, RequestBody>()
        map["img_category"] = WebServiceUtils.getStringRequestBody(IConstants.IMAGE_CATEGORY)
        map["local_media_id"] = WebServiceUtils.getStringRequestBody(fileId!!)
        changeFileStatus(fileId, IConstants.IN_PROGRESS)
        val request =
            UserProfileRepository(
                (this.application as AppineersApplication)
                    .applicationComponent.getNetworkService()
            ).callUploadMultimediaMedia(
                map,
                getMultipartFile(file!!)
            )

        val schedulerProvider =
            (this.application as AppineersApplication).applicationComponent.getSchedulerProvider()

        (this.application as AppineersApplication).applicationComponent.getCompositeDisposable()
            .addAll(request.subscribeOn(schedulerProvider.io()).subscribe(
                { response ->
                    stopSelf()
                    if (response.settings?.success == "1") {
                        val notif = updateNotification(this, getString(R.string.upload_success))
                        val managerCompat =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        managerCompat.notify(NOTIFICATION_ID, notif)
                        if (mediaFileRepository != null) {
                            mediaFileRepository!!.deleteFile(fileId.toLong())
                        }
                        /* (application as AppineersApplication).isMediaUploaded.postValue(
                             MediaUpload(
                                 userId = userId,
                                 imageId = response.data!![0].ima,
                                 url = response.data!![0].url
                             )
                         )*/
                    } else {
                        showFailedNotification(fileId, file, userId!!)
                    }
                },
                {
                    stopSelf()
                    showFailedNotification(fileId, file, userId!!)
                }
            ))


        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun changeFileStatus(fileId: String?, status: String) {
        if (fileId != null && mediaFileRepository != null) {
            CoroutineScope(Dispatchers.IO).launch {
                mediaFileRepository!!.changeStatus(fileId.toLong(), status)
            }
        }
    }

    private fun showFailedNotification(fileId: String?, filePath: String, userId: String) {
        val notif = updateNotification(this, getString(R.string.upload_failed))
        val managerCompat =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        managerCompat.notify(NOTIFICATION_ID, notif)
        changeFileStatus(fileId, IConstants.PENDING)

        (application as AppineersApplication).isMediaUploaded.postValue(
            MediaUpload(
                userId = userId,
                imageId = fileId,
                url = filePath
            )
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.setShowBadge(false)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}