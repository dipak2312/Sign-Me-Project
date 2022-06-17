package com.app.signme.api.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class WebServiceUtils {
    companion object {
        fun getStringMultipartBodyPart(fieldName: String, filePath: String): MultipartBody.Part {
            val file = File(filePath)
            return MultipartBody.Part.createFormData(
                fieldName, file.name, file
                    .asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        fun getStringRequestBody(text: String): RequestBody {
            return text.toRequestBody("text/plain".toMediaTypeOrNull())
        }
    }
}
