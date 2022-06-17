package com.app.signme.commonUtils.utility

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.helper.LOGApp
import com.app.signme.view.SplashActivity
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
@SuppressLint("NewApi")
class NotificationUtils {

    companion object {

        fun createAppNotification(
            mContext: Context,
            application: AppineersApplication,
            remoteMessage: RemoteMessage?
        ) {

            try {
                var notificationHelper: NotificationHelper? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    if (notificationHelper == null) {
                    notificationHelper = NotificationHelper(mContext)
                    notificationHelper.setDefaultChannelIfNotSet(mContext)
//                    }
                }

                if (remoteMessage?.data?.isNotEmpty() == true) {//{"notification_type":"shared_crekid","user_id":"144","kid_id":"27"}
                    sendNotification(mContext, application, notificationHelper, remoteMessage.data)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param data FCM body received.
         */
        private fun sendNotification(
            mContext: Context,
            application: AppineersApplication,
            notificationHelper: NotificationHelper?,
            data: Map<String, String>
        ) {
            try {
                val jsonObject = JSONObject(data["others"])
                LOGApp.e(jsonObject.toString())
                // Create an Intent for the activity you want to start
                val resultIntent = Intent(mContext, getLaunchClass()).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                val stackBuilder = TaskStackBuilder.create(mContext)
                stackBuilder.addNextIntentWithParentStack(resultIntent)
                val pendingIntent: PendingIntent?
                pendingIntent =
                    if ((application.getCurrentActivity() == null) || (application.getCurrentActivity()?.isDestroyed == true)) {
                        stackBuilder.getPendingIntent(
                            Random().nextInt(50),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    } else {
                        PendingIntent.getActivity(
                            mContext,
                            Random().nextInt(50)/* Request code */,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }

                createNotification(
                    mContext,
                    application,
                    notificationHelper,
                    pendingIntent,
                    data,
                    jsonObject.getString(IConstants.PARAM_NOTIFICATION_TYPE)
                )

            } catch (e: Exception) {
                e.printStackTrace()
//                Crashlytics.logException(e)
            }

        }

        private fun createNotification(
            mContext: Context,
            application: AppineersApplication,
            notificationHelper: NotificationHelper?,
            pendingIntent: PendingIntent?,
            data: Map<String, String>,
            notificationType: String
        ) {

            val GROUP_KEY_ROUTE = "com.app.whitelabel.ROUTE"
            val notificationId = Random().nextInt(50)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val title = if (data["title"]?.isNotEmpty() == true) {
                data["title"]
            } else {
                mContext.getString(R.string.app_name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nb = notificationHelper?.getNotificationDefault(title, data["message"])
                nb?.setSmallIcon(R.mipmap.ic_launcher)
                nb?.setSound(defaultSoundUri)
                nb?.setAutoCancel(true)
                nb?.setGroup(GROUP_KEY_ROUTE)
                nb?.setWhen(System.currentTimeMillis())
                nb?.color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark)

                nb?.setContentIntent(pendingIntent)

                notificationHelper?.notify(notificationId, nb)
                LOGApp.e("Notification Id : " + notificationId)
            } else {
                val notificationBuilder = NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(data["message"])
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(data["message"]))
                    .setSound(defaultSoundUri)
                    .setGroup(GROUP_KEY_ROUTE)


                notificationBuilder.setContentIntent(pendingIntent)

                notificationBuilder.color =
                    ContextCompat.getColor(mContext, R.color.colorPrimaryDark)
                val notificationManager =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                notificationManager!!.notify(
                    notificationId /* ID of notification */,
                    notificationBuilder.build()
                )
                LOGApp.e("Notification Id : " + notificationId)
            }
        }

        class ImageDownloader(
            val mContext: Context,
            val notificationHelper: NotificationHelper?,
            val pendingIntent: PendingIntent?,
            val data: Map<String, String>,
            val retailerName: String
        ) : AsyncTask<String, Void, Bitmap>() {
            override fun doInBackground(vararg p0: String?): Bitmap? {
                val input: InputStream
                try {

                    val url = URL(p0[0])
                    val connection = url.openConnection()
                    connection.doInput = true
                    connection.connect()
                    input = connection.getInputStream()
                    return BitmapFactory.decodeStream(input)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }
        }

        /**
         * Choose activity to open
         *
         * @return if any of first sync (contact or music) not completed than open login activity
         * else open main activity
         */
        private fun getLaunchClass(): Class<*> {
            return SplashActivity::class.java
        }

        private fun getHomePendingIntent(
            mContext: Context,
            application: AppineersApplication
        ): PendingIntent {
            // Create an Intent for the activity you want to start

            val resultIntent =
                Intent(mContext, SplashActivity::class.java)

            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            val stackBuilder = TaskStackBuilder.create(mContext)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            return if ((application.getCurrentActivity() == null) || (application.getCurrentActivity()?.isDestroyed == true)) {
                stackBuilder.getPendingIntent(
                    Random().nextInt(50),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    mContext,
                    Random().nextInt(50)/* Request code */,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        }

        fun areNotificationsEnabled(mContext: Context, application: AppineersApplication): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!manager.areNotificationsEnabled()) {
                    return false
                }
                val channels: List<NotificationChannel> = manager.notificationChannels
                for (channel in channels) {
                    if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                        return false
                    }
                }
                true
            } else {
                NotificationManagerCompat.from(mContext).areNotificationsEnabled()
            }
        }

        /**
         * Open System Notification Settings for Application
         */
        fun openAppNotificationSettings(context: Context) {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    else -> {
                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:" + context.packageName)
                    }
                }
            }
            context.startActivity(intent)
        }
    }
}