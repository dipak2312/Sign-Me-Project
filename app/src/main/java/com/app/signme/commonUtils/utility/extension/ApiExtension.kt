package com.app.signme.commonUtils.utility.extension

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.app.signme.utility.helper.mediahelper.FileUtils.getMimeType

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File


fun Any.toFieldStringMap(): HashMap<String, String> {
    /*val classAny = this::class
    val paramMap = HashMap<String, Any>()
    for (field in classAny.members) {
        paramMap[field.name] = field.
    }
    return paramMap*/
    val gson = Gson()
    val json = gson.toJson(this)
    val jsonObject = JSONObject(json)
    val param = HashMap<String, String>()
    try {
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = jsonObject.get(key)

            if (value != null && value is String) {
                param[key] = value
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return param
}

fun Any.toFieldRequestBodyMap(): HashMap<String, RequestBody> {
    val gson = Gson()
    val json = gson.toJson(this)
    val jsonObject = JSONObject(json)
    val param = HashMap<String, RequestBody>()
    try {
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            var value = jsonObject.get(key) // {"path":"/emulated/0/"}
            if (value is JSONObject && value.has("path")) {
                val path = value.getString("path")
                value = File(path)
            }

            val body = getRequestBody(value)
            val reqKey = getRequestKey(key, value)
            if (body != null) {
                param[reqKey] = body
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return param
}

fun <T> getRequestBody(t: T): RequestBody? {
    return when (t) {
        is String -> getStringRequestBodyForMap(t as String)
        is File -> getFileRequestBody(t as File)
        else -> null
    }
}

fun <T> getRequestKey(key: String, value: T): String {
    return when (value) {
        is File -> getFileUploadKey(key, value)
        else -> key
    }
}

fun getFileRequestBody(file: File): RequestBody? {
    try {
        val mimeType = getMimeType(file)
        if (mimeType != null) {
            return file.asRequestBody(mimeType.toMediaTypeOrNull())
        }
    } catch (e: Exception) {
        Log.d("", "File to request body failed")
    }
    return null
}

fun getFileRequestBody(path: String): RequestBody? {
    val file = File(path)
    return getFileRequestBody(file)
}

fun getStringRequestBodyForMap(value: String?): RequestBody? {
    try {
        return value?.toRequestBody("text/plain".toMediaTypeOrNull())
    } catch (e: Exception) {
        Log.d("", "string to request body failed")
    }
    return null
}

fun getFileUploadKey(key: String, file: File): String {
    return "" + key + "\"; filename=\"" + file.name
}

/*private fun getMimeType(url: String): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}*/

/**
 * close opened soft keyboard.
 *
 * @param mActivity context
 */
fun hideSoftKeyboard(mActivity: Activity) {
    try {
        val view = mActivity.currentFocus
        if (view != null) {
            val inputManager =
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}
