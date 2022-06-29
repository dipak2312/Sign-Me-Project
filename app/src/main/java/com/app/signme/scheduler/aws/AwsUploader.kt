package com.app.signme.scheduler.aws

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.webkit.MimeTypeMap
import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import com.app.signme.commonUtils.utility.extension.getMimeType
import com.app.signme.scheduler.aws.cacheUtils.CacheUtils.getStringArrayPref
import com.app.signme.scheduler.aws.cacheUtils.CacheUtils.setStringArrayPref
import com.app.signme.scheduler.aws.cacheUtils.UploadSuccessCallback
import com.app.signme.scheduler.aws.cacheUtils.CacheCompat.apply
import com.hb.logger.Logger
import io.reactivex.Single
import kotlinx.coroutines.*
import java.io.File
import java.util.*

/**
 * @author Mahesh Lipane
 * Date/Time of the change  : 28/01/2022
 * Developer Name: Mahesh Lipane
 * */
open class AwsUploader(
    context: Context,
    private val s3Client: AmazonS3Client,
    private val s3BucketName: String,
    private val s3key: String,
    private val file: File
) {
    private var prefs: SharedPreferences
    private var partSize = MIN_DEFAULT_PART_SIZE
    private var progressListener: UploadProgressListener? = null
    private var bytesUploaded: Long = 0
    private var userInterrupted = false
    private var userAborted = false

    companion object {
        private const val MIN_DEFAULT_PART_SIZE = (5 * 1024 * 1024).toLong()
        private val TAG: String = AwsUploader::class.java.simpleName
        private const val PREFS_NAME = "preferences"
        private const val PREFS_UPLOAD_ID = "_uploadId"
        private const val PREFS_ETAGS = "_etags"
        private const val PREFS_ETAG_SEP = "~~"
        const val EVENT_UPLOADING = "Uploading Event"
    }

    class UploadInterruptedException : RuntimeException {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)

        companion object {
            private const val serialVersionUID = 1L
        }
    }

    init {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    /**
     * Initiate a multipart file upload to Amazon S3
     *
     * @return the URL of a successfully uploaded file
     */
    suspend fun start(positon:Int): Single<UploadSuccessCallback> {
        var response = ""
        val waitFor = CoroutineScope(Dispatchers.IO).async {
            // initialize
            val partETags: MutableList<PartETag> = ArrayList()
            val contentLength = file.length()
            var filePosition: Long = 0
            var startPartNumber = 1
            userInterrupted = false
            userAborted = false
            bytesUploaded = 0

            // check if we can resume an incomplete download
            var uploadId = cachedUploadId
            if (uploadId != null) {
                // we can resume the download
                Log.i(TAG, "resuming upload for $uploadId")
                logger.dumpCustomEvent(EVENT_UPLOADING, "resuming upload for $uploadId")

                // get the cached etags
                val cachedEtags = cachedPartEtags
                partETags.addAll(cachedEtags)

                // calculate the start position for resume
                startPartNumber = cachedEtags.size + 1
                filePosition = (startPartNumber - 1) * partSize
                bytesUploaded = filePosition
                Log.i(TAG, "resuming at part $startPartNumber position $filePosition")
                logger.dumpCustomEvent(
                    EVENT_UPLOADING,
                    "resuming at part $startPartNumber position $filePosition"
                )
            } else {
                // initiate a new multi part upload
                Log.i(TAG, "initiating new upload")
                logger.dumpCustomEvent(EVENT_UPLOADING, "initiating new upload")

                try {
                    val initRequest = InitiateMultipartUploadRequest(
                        s3BucketName, s3key
                    )
                    configureInitiateRequest(initRequest)
                    val initResponse = s3Client.initiateMultipartUpload(initRequest)
                    uploadId = initResponse.uploadId
                } catch (e: Exception) {
                    cancel(e.message!!, e)
                }
            }
            try {
                val abortRequest = AbortMultipartUploadRequest(s3BucketName, s3key, uploadId)
                var k = startPartNumber
                while (filePosition < contentLength) {
                    val thisPartSize = Math.min(partSize, contentLength - filePosition)
                    Log.i(TAG, "starting file part $k with size $thisPartSize")
                    logger.dumpCustomEvent(
                        EVENT_UPLOADING,
                        "starting file part $k with size $thisPartSize"
                    )
                    val uploadRequest = UploadPartRequest().withBucketName(
                        s3BucketName
                    )
                        .withKey(s3key).withUploadId(uploadId)
                        .withPartNumber(k).withFileOffset(filePosition).withFile(file)
                        .withPartSize(thisPartSize)
                    val s3progressListener = ProgressListener { progressEvent: ProgressEvent ->

                        // bail out if user cancelled
                        if (userInterrupted) {
                            s3Client.shutdown()
                            cancel(
                                "User interrupted",
                                UploadInterruptedException("User interrupted")
                            )
                        } else if (userAborted) {
                            // aborted requests cannot be resumed, so clear any cached etags
                            clearProgressCache()
                            s3Client.abortMultipartUpload(abortRequest)
                            s3Client.shutdown()
                        }
                        bytesUploaded += progressEvent.bytesTransfered.toLong()

                        //Log.d(TAG, "bytesUploaded=" + bytesUploaded);

                        // broadcast progress
                        val fpercent = (bytesUploaded * 100 / contentLength).toFloat()
                        val percent = Math.round(fpercent)
                        if (progressListener != null) {
                            progressListener!!.progressChanged(
                                progressEvent,
                                bytesUploaded,
                                percent
                            )
                        }
                    }

                    try {
                        uploadRequest.progressListener = s3progressListener
                        val result = s3Client.uploadPart(uploadRequest)
                        partETags.add(result.partETag)

                        // cache the part progress for this upload
                        if (k == 1) {
                            initProgressCache(uploadId)
                        }
                        // store part etag
                        cachePartEtag(result)
                        filePosition += thisPartSize
                        k++
                    } catch (e: AmazonClientException) {
                        cancel(e.message!!, e)
                    }

                }
                val compRequest = CompleteMultipartUploadRequest(
                    s3BucketName, s3key, uploadId,
                    partETags
                )
                val result = s3Client.completeMultipartUpload(compRequest)
                bytesUploaded = 0
                clearProgressCache()
                response = result.location
                // uploadLocation = Single.just(Gson().toJson(result))
                return@async
            } catch (e: Exception) {
                cancel(e.message!!, UploadInterruptedException(positon.toString(),e))
            }
        }
        try {
            waitFor.await()
            return  Single.just(UploadSuccessCallback(position = positon, callbackKey = response))
        } catch (e: UploadInterruptedException) {
            Log.i(TAG, "start: Exception " + e.message)
            return  Single.error(e)
        }catch (e: Exception) {
            Log.i(TAG, "start: Exception " + e.message)
            return  Single.error(e)
        }
    }

    private val cachedUploadId: String?
        get() = prefs.getString(s3key + PREFS_UPLOAD_ID, null)

    // get the cached etags
    private val cachedPartEtags: List<PartETag>
        get() {
            val result: MutableList<PartETag> = ArrayList()
            // get the cached etags
            val etags = getStringArrayPref(prefs, s3key + PREFS_ETAGS)
            for (etagString in etags) {
                val partNum = etagString.substring(0, etagString.indexOf(PREFS_ETAG_SEP))
                val partTag =
                    etagString.substring(etagString.indexOf(PREFS_ETAG_SEP) + 2, etagString.length)
                val etag = partNum.toInt().let { PartETag(it, partTag) }
                result.add(etag)
            }
            return result
        }

    private fun cachePartEtag(result: UploadPartResult) {
        val serialEtag =
            result.partETag.partNumber.toString() + PREFS_ETAG_SEP + result.partETag.eTag
        val etags: ArrayList<String> = getStringArrayPref(prefs, s3key + PREFS_ETAGS)
        etags.add(serialEtag)
        setStringArrayPref(prefs, s3key + PREFS_ETAGS, etags)
    }

    private fun initProgressCache(uploadId: String?) {
        // store uploadID
        val edit = prefs.edit().putString(s3key + PREFS_UPLOAD_ID, uploadId)
        apply(edit)
        // create empty etag array
        val etags = ArrayList<String>()
        setStringArrayPref(prefs, s3key + PREFS_ETAGS, etags)
    }

    private fun clearProgressCache() {
        // clear the cached uploadId and etags
        val edit = prefs.edit()
        edit.remove(s3key + PREFS_UPLOAD_ID)
        edit.remove(s3key + PREFS_ETAGS)
        apply(edit)
    }

    fun interrupt() {
        userInterrupted = true
    }

    fun abort() {
        userAborted = true
    }

    /**
     * Override to configure the multipart upload request.
     *
     * By default uploaded files are publicly readable.
     *
     * @param initRequest S3 request object for the file to be uploaded
     */
    private fun configureInitiateRequest(initRequest: InitiateMultipartUploadRequest) {
        val metadata = ObjectMetadata()
        metadata.contentType = file.getMimeType()
        initRequest.cannedACL = CannedAccessControlList.PublicRead
        initRequest.objectMetadata = metadata
    }

    // url = file path or whatever suitable URL you want.
    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun setPrefs(prefs: SharedPreferences) {
        this.prefs = prefs
    }

    fun getPartSize(): Long {
        return partSize
    }

    fun setPartSize(partSize: Long) {
        check(partSize >= MIN_DEFAULT_PART_SIZE) { "Part size is less than S3 minimum of $MIN_DEFAULT_PART_SIZE" }
        this.partSize = partSize
    }

    fun setProgressListener(progressListener: UploadProgressListener?) {
        this.progressListener = progressListener
    }

    interface UploadProgressListener {
        fun progressChanged(
            progressEvent: ProgressEvent?,
            bytesUploaded: Long,
            percentUploaded: Int
        )
    }
}