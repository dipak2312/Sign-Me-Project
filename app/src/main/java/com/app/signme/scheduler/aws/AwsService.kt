package com.app.signme.scheduler.aws

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ProgressEvent
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.scheduler.aws.cacheUtils.UploadSuccessCallback
import com.google.gson.Gson
import com.hb.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * @author Mahesh Lipane
 * Date/Time of the change  : 28/01/2022
 * Developer Name: Mahesh Lipane
 * */
class AwsService : LifecycleService() {

    private var s3Client: AmazonS3Client? = null
    private var awsUploader: AwsUploader? = null
    private var mNotificationManager: NotificationManager? = null
    private var uploadSuccessCallback: UploadSuccessCallback? = null

    companion object {
        private val TAG: String = AwsService::class.java.simpleName
        val logger by lazy {
            Logger(TAG)
        }

        private const val TITLE = "Uploading image to bucket"
        const val CHANNEL_ID = "com.app.signme.LOCAL_CHANNEL"
        val NOTIFICATION_ID: Int = Random().nextInt(50)
        const val UPLOADING_SUCCESS: Int = -2
        const val UPLOADING_FAILED: Int = -1

        const val UPLOAD_CANCELLED_ACTION = "UPLOAD_CANCELLED_ACTION"

        const val EXTRA_POSITION = "file_position"
        const val EXTRA_FILE_PATH = "file_path"

        const val ACCESS_KEY = "AKIAZXFVGK4EDCXSSWMQ"
        const val SECRETE_KEY = "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"
        const val BUCKET_NAME = "appineers"
        var userId:String=sharedPreference.userDetail!!.userId!!
        val FOLDER_PATH:String = "sign_me/image/$userId/"

        /**
         * Get start intent
         * @param mContext
         * @param path
         * @return
         */
        fun getStartIntent(mContext: Context, path: String, position: Int?): Intent {
            return Intent(mContext, AwsService::class.java).apply {
                putExtra(EXTRA_FILE_PATH, path)
                putExtra(EXTRA_POSITION, position)
            }
        }

        private fun updateNotification(
            context: Context,
            message: String,
            progress: Int
        ): Notification {
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setContentTitle(TITLE)
                setContentText(message)
                setProgress(100, progress, false)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
            }

            return notificationBuilder.build()
        }

        /**
         * This function return true if deleted successfully
         */
        suspend fun deleteFile(key: String): Boolean {
            var isDeleted = false
            val waitFor = CoroutineScope(IO).async {
                return@async try {
                    logger.dumpCustomEvent(TAG, "Try to deleting: " + key)
                    val s3Client = AmazonS3Client(
                        BasicAWSCredentials(ACCESS_KEY, SECRETE_KEY)
                    )

                    val request =
                        s3Client.deleteObjects(DeleteObjectsRequest(BUCKET_NAME).withKeys(key))
                    if (!request.deletedObjects.isNullOrEmpty()) {
                        isDeleted = request.deletedObjects.get(0).isDeleteMarker
                    } else {
                        isDeleted = false
                    }
                    logger.dumpCustomEvent(
                        TAG,
                        "File is delete: " + isDeleted
                    )
                } catch (e: AmazonServiceException) {
                    // The call was transmitted successfully, but Amazon S3 couldn't process
                    // it, so it returned an error response.
                    e.printStackTrace()
                    throw e
                } catch (e: Exception) {
                    // Amazon S3 couldn't be contacted for a response, or the client
                    // couldn't parse the response from Amazon S3.
                    e.printStackTrace()
                    throw e
                }
            }

            try {
                waitFor.await()
                return isDeleted
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        s3Client = AmazonS3Client(
            BasicAWSCredentials(ACCESS_KEY, SECRETE_KEY)
        )
        val f = IntentFilter()
        f.addAction(UPLOAD_CANCELLED_ACTION)
        registerReceiver(uploadCancelReceiver, f)
    }


    fun Int.deepCopy(): Int {
        val jSON = Gson().toJson(this)
        return Gson().fromJson(jSON, Int::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        uploadSuccessCallback = UploadSuccessCallback()
        createNotificationChannel(this)

        val position = intent?.getIntExtra(EXTRA_POSITION, -2)?.deepCopy() ?: -2
        val filePath: String = intent?.getStringExtra(EXTRA_FILE_PATH) ?: ""
        uploadSuccessCallback?.localPath = filePath
        uploadSuccessCallback?.position = position

        Log.i(TAG, "onStartCommand: init " + position)

        val fileToUpload = File(filePath)
        //  val readyToUploadFile = FOLDER_PATH + md5(filePath) + "." + fileToUpload.extension
        val readyToUploadFile = FOLDER_PATH + Date().time + "." + fileToUpload.extension

        val msg = "Uploading $readyToUploadFile..."

        // create a new uploader for this file
        awsUploader = AwsUploader(this, s3Client!!, BUCKET_NAME, readyToUploadFile, fileToUpload)

        // listen for progress updates and broadcast/notify them appropriately
        awsUploader!!.setProgressListener(object : AwsUploader.UploadProgressListener {
            override fun progressChanged(
                progressEvent: ProgressEvent?,
                bytesUploaded: Long,
                percentUploaded: Int
            ) {
                mNotificationManager?.notify(
                    NOTIFICATION_ID,
                    updateNotification(this@AwsService, msg, percentUploaded)
                )
                postStatus(position,msg,percentUploaded)
            }
        })

        // broadcast/notify that our upload is starting
        mNotificationManager?.notify(NOTIFICATION_ID, updateNotification(this, msg, 0))
        postStatus(position,msg,0)

        CoroutineScope(IO).launch {
            val schedulerProvider =
                (this@AwsService.application as AppineersApplication).applicationComponent.getSchedulerProvider()
            (this@AwsService.application as AppineersApplication).applicationComponent.getCompositeDisposable()
                .addAll(
                    awsUploader!!.start(position)
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                            { response ->
                                mNotificationManager?.notify(
                                    NOTIFICATION_ID,
                                    updateNotification(
                                        this@AwsService,
                                        "File successfully uploaded.",
                                        100
                                    )
                                )
                                postStatus(position,"File successfully uploaded.",UPLOADING_SUCCESS,response?.callbackKey!!)
                                Log.i(TAG, "onStartCommand: url " + response?.callbackKey)
                                Log.i(TAG, "onStartCommand: position " + position)
                                stopSelf()
                            },
                            { error ->
                                Log.i("TAG", "onStartCommand: " + error.message)
                                try {
                                    //  Toast.makeText(this@UploadService,"Uploading Failed",Toast.LENGTH_LONG).show()
                                    throw error
                                } catch (excep: AwsUploader.UploadInterruptedException) {
                                    mNotificationManager?.notify(
                                        NOTIFICATION_ID,
                                        updateNotification(
                                            this@AwsService,
                                            "User interrupted",
                                            UPLOADING_FAILED
                                        )
                                    )
                                    postStatus(position,"User interrupted",UPLOADING_FAILED)
                                    stopSelf()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    mNotificationManager?.notify(
                                        NOTIFICATION_ID,
                                        updateNotification(
                                            this@AwsService,
                                            "Error: " + e.message,
                                            UPLOADING_FAILED
                                        )
                                    )
                                    postStatus(position,"User interrupted",UPLOADING_FAILED)
                                    stopSelf()
                                }
                            }
                        ))
        }
        return START_STICKY
    }

    private fun postStatus(position: Int, message: String, status: Int, callbackResponse: String="") {
        uploadSuccessCallback?.position =position
        uploadSuccessCallback?.message = message
        uploadSuccessCallback?.status = status
        uploadSuccessCallback?.callbackKey = callbackResponse
        (this@AwsService.application as AppineersApplication).awsFileUploader.postValue(
            uploadSuccessCallback
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "white_label",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.setShowBadge(false)
            mNotificationManager = context.getSystemService(NotificationManager::class.java)
            mNotificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        if (mNotificationManager != null) {
            mNotificationManager!!.cancel(NOTIFICATION_ID)
        }
        unregisterReceiver(uploadCancelReceiver)
        super.onDestroy()
    }

    private val uploadCancelReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (awsUploader != null) {
                awsUploader!!.interrupt()
            }
        }
    }

    private fun md5(s: String?): String? {
        return try {
            // create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s!!.toByteArray())
            val messageDigest = digest.digest()

            // create Hex String
            val hexString = StringBuilder()
            for (b in messageDigest) hexString.append(Integer.toHexString(0xFF and b.toInt()))
            hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }


}