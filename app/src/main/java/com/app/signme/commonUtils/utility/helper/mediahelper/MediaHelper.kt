@file:Suppress("DEPRECATION")

package com.app.signme.utility.helper.mediahelper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import com.app.signme.commonUtils.utility.extension.getCameraUri
import java.io.*

class MediaHelper {

    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var provider: String? = null
    private val DIRECTORY_NAME = "Pictotum"

    private var photoFile: File? = null
    private var photoUri: Uri? = null

    private var callback: Callback? = null

    private val context: Context?
        get() = if (activity != null) activity else fragment!!.context

    //=============
    //Image - start
    //=============
    private val REQUEST_FILE = 1000
    private val REQUEST_IMAGE_CAMERA = 1001

    private var requestedMimeType: String? = null

    object DefaultExtensions {
        const val IMAGE = ".jpeg"
        const val AUDIO = ".mp3"
        const val VIDEO = ".mp4"
        const val PDF = ".mp3"
        const val OTHER = ".other"
    }

    constructor(activity: Activity) {
        this.activity = activity
    }

    constructor(activity: Activity, provider: String) {
        this.activity = activity
        this.provider = provider
        FileUtils.AUTHORITY = provider
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
    }

    constructor(fragment: Fragment, provider: String) {
        this.fragment = fragment
        this.provider = provider
    }

    abstract class Callback {
        abstract fun onResult(file: File?, mimeType: String?)

        open fun onError(e: Exception) {}
    }

    fun image(callback: Callback?) {
        this.callback = callback
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = null
        try {
            requestedMimeType = MIME_TYPE_IMAGE
            photoFile = createFile(MIME_TYPE_IMAGE, DefaultExtensions.IMAGE)
            //Old
            //Uri photoURI = Uri.fromFile(photoFile);
            //New
            photoUri = getCameraUri(activity!!)//FileProvider.getUriForFile(context!!, provider!!, photoFile!!)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            if (activity != null) activity!!.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA)
            else if (fragment != null) fragment!!.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA)

        } catch (ex: IOException) {
            callback?.onError(ex)
            ex.printStackTrace()
            return
        }

    }

    fun file(mimeType: String, callback: Callback) {
        this.requestedMimeType = mimeType
        this.callback = callback

        val intent: Intent?
        intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = mimeType
        if (activity != null) activity!!.startActivityForResult(Intent.createChooser(intent, "Select App"), REQUEST_FILE)
        else if (fragment != null) {
            fragment!!.startActivityForResult(Intent.createChooser(intent, "Select App"), REQUEST_FILE)
        }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            fragment?.onActivityResult(requestCode,resultCode,data)
        println("HEEEEEE"+data)
         if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == RESULT_OK && photoFile != null) {
             if (callback != null) callback!!.onResult(photoFile, requestedMimeType)
         } else if (requestCode == REQUEST_FILE && resultCode == RESULT_OK) {
             if (callback != null) {
                 try {
                     var file = FileUtils.getFile(context!!, data.data)
                     if (file != null && file.exists()) {
                         callback!!.onResult(file, requestedMimeType)
                     } else {
                         file = guessFile(context, requestedMimeType, data.data)
                         saveFile(data.data, file, object : Callback() {
                             override fun onResult(file: File?, mimeType: String?) {
                                 callback!!.onResult(file, mimeType)
                             }


                             override fun onError(e: Exception) {
                                 super.onError(e)
                                 callback!!.onError(e)
                                 e.printStackTrace()
                             }
                         })
                     }
                 } catch (e: IOException) {
                     e.printStackTrace()
                 }

             }
         }
     }

    //=============
    @Throws(IOException::class) private fun guessFile(context: Context?, defaultMimeType: String?, uri: Uri?): File {
        val mimeType = FileUtils.getMimeType(context!!, uri!!)
        var extension: String?
        if (!TextUtils.isEmpty(mimeType) && MIME_TYPE_STREAM.equals(mimeType, ignoreCase = true)) {
            requestedMimeType = mimeType
            extension = FileUtils.getExtension(uri.toString() + "")
            if (TextUtils.isEmpty(extension) && !TextUtils.isEmpty(defaultMimeType)) {
                extension = getExtensionFromMimeType(defaultMimeType)
            }
        } else {
            requestedMimeType = defaultMimeType
            extension = getExtensionFromMimeType(defaultMimeType)
        }

        return createFile(mimeType, extension!!)
    }

    @SuppressLint("DefaultLocale")
    private fun getExtensionFromMimeType(mimeType: String?): String {
        if (mimeType == null) return DefaultExtensions.OTHER

        return when {
            requestedMimeType!!.toLowerCase().contains("image") -> {
                DefaultExtensions.IMAGE
            }
            requestedMimeType!!.toLowerCase().contains("audio") -> {
                DefaultExtensions.AUDIO
            }
            requestedMimeType!!.toLowerCase().contains("video") -> {
                DefaultExtensions.VIDEO
            }
            requestedMimeType!!.toLowerCase().contains("pdf") -> {
                DefaultExtensions.PDF
            }
            else -> {
                DefaultExtensions.OTHER
            }
        }
    }


    @SuppressLint("DefaultLocale")
    @Throws(IOException::class) private fun createFile(mimeType: String?, extension: String): File {
        val parentStorageDir = File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME)
        if (!parentStorageDir.exists()) {
            parentStorageDir.mkdirs()
        }
        val folderName: String

        if (mimeType != null && mimeType.toLowerCase().contains("image")) {
            folderName = "Images"
        } else if (mimeType != null && mimeType.toLowerCase().contains("audio")) {
            folderName = "Audio"
        } else if (mimeType != null && mimeType.toLowerCase().contains("video")) {
            folderName = "Video"
        } else if (mimeType != null && mimeType.toLowerCase().contains("pdf")) {
            folderName = "Pdf"
        } else {
            folderName = "Others"
        }

        val mediaStorageDir = File(parentStorageDir.absolutePath, folderName)
        if (mediaStorageDir.exists() == false) {
            mediaStorageDir.mkdirs()
        }

        return File.createTempFile("File_" + System.currentTimeMillis(), extension, mediaStorageDir)
    }

    @SuppressLint("StaticFieldLeak")
    private fun saveFile(source: Uri?, destination: File?, callback: Callback?) {

        object : AsyncTask<Void, Void, Boolean>() {
            var exception: Exception? = null
            override fun doInBackground(vararg voids: Void): Boolean? {

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = context!!.contentResolver.openInputStream(source!!)
                    outputStream = FileOutputStream(destination!!)
                    val buf = ByteArray(1024)
                    val len: Int
                    len = inputStream!!.read(buf)
                    while (len > 0) {
                        outputStream.write(buf, 0, len)
                    }
                    outputStream.close()
                    outputStream.close()
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                    exception = e
                } finally {
                    if (inputStream != null) try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        exception = e
                    }

                    if (outputStream != null) try {
                        outputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        exception = e
                    }

                }
                return false
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                if (callback != null) {
                    if (result!!) callback.onResult(destination, requestedMimeType)
                    else callback.onError(this.exception!!)
                }
            }
        }.execute()
    }

    companion object {

        const val MIME_TYPE_AUDIO = "audio/*"
        const val MIME_TYPE_TEXT = "text/*"
        const val MIME_TYPE_IMAGE = "image/*"
        const val MIME_TYPE_VIDEO = "video/*"
        const val MIME_TYPE_APP = "application/*"
        const val MIME_TYPE_STREAM = "application/octet-stream"
    }
}