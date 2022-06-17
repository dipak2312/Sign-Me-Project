@file:Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.app.signme.commonUtils.utility.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Check is network available or not
 * @param context Context?
 * @return Boolean return network conectivity status. true/false
 */
fun checkInternetConnected(context: Context?): Boolean {
    return if (context != null) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        activeNetwork?.isConnectedOrConnecting == true
    } else {
        true
    }
}

fun getStringMultipartBodyPart(key: String, filePath: String?): MultipartBody.Part? {
    val file = File(filePath)
    val fileBody: RequestBody = RequestBody.create("image/*".toMediaType(), file)
    return MultipartBody.Part.createFormData(key, file.name, fileBody)
}

fun getStringRequestBody(value: String?): RequestBody {
    val mediaTypeText: MediaType = "text/plain".toMediaType()
    return RequestBody.create(mediaTypeText, value ?: "")
}
fun getStringRequestPassword(value: String?): RequestBody {
    val mediaTypeText: MediaType = "application/json".toMediaType()
    return RequestBody.create(mediaTypeText, value ?: "")
}
fun getStringMultipartBodyPartForLogOrZipFile(
    key: String?,
    filePath: String?
): MultipartBody.Part? {
    val file = File(filePath)
    val filebody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
    return key?.let { createFormData(it, file.name, filebody) }
}