package com.app.signme.api.network


import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException
import java.util.regex.Pattern

class RequestInterceptor(private val onRequestInterceptor: OnRequestInterceptor) : Interceptor {

    interface OnRequestInterceptor {
        fun provideBodyMap(): HashMap<String, String>
        fun provideHeaderMap(): HashMap<String, String>
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()


        val method = request.method
        val bodyMap = onRequestInterceptor.provideBodyMap()

        if (method == "GET") {
            // If Request method is GET
            val fullUrl = request.url
            val parameterNames = fullUrl.queryParameterNames

            val parameterIterator = parameterNames.iterator()
            val urlRequestBuilder = request.url.newBuilder()

            while (parameterIterator.hasNext()) {
                val key = parameterIterator.next()
                val values = fullUrl.queryParameterValues(key)
                val value = fullUrl.queryParameterValues(key)[values.lastIndex]//If query has multiple value then dev needs to send in other form
                urlRequestBuilder.removeAllQueryParameters(key) // remove already query parameter
                urlRequestBuilder.addQueryParameter(key, value)
            }

            bodyMap.asIterable().forEach {
                urlRequestBuilder.addQueryParameter(it.key, it.value)
            }
            request = request.newBuilder().get().url(urlRequestBuilder.build()).build()

        } else {
            // If Request method is POST / DELETE /  PUT / PATCH ..
            val requestBody = request.body
            if (requestBody != null) {

                lateinit var newBody: RequestBody
                when {
                    request.body is FormBody -> { // if request body is FORM
                        // BODY
                        val formBody = request.body as FormBody
                        val size = formBody.size
                        val newBodyBuilder = FormBody.Builder()

                        for (i in 0 until size) {
                            val key = formBody.name(i)
                            val value = formBody.value(i)
                            newBodyBuilder.add(key, value)
                        }

                        bodyMap.asIterable().forEach {
                            newBodyBuilder.add(it.key, it.value)
                        }
                        newBody = newBodyBuilder.build()
                    }
                    request.body is MultipartBody -> { //if request body is MULTIPART BODY
                        val formBody = request.body as MultipartBody
                        val partList = formBody.parts

                        val newBodyBuilder = MultipartBody.Builder()
                        newBodyBuilder.setType(MultipartBody.FORM)

                        for (i in partList) {
                            if (i.body.contentType()?.type?.equals("text")!!) { //"text", "image", "audio", "video"
                                val key = getKeyFromContentDisposition(i.headers?.get("Content-Disposition")!!)
                                val value = bodyToString(i.body)
                                newBodyBuilder.addPart(MultipartBody.Part.createFormData(key,null,value.toRequestBody(("text/plain").toMediaType())))
                            } else {
                                newBodyBuilder.addPart(i.headers,i.body)
                            }
                        }

                        bodyMap.asIterable().forEach {
                            newBodyBuilder.addPart(MultipartBody.Part.createFormData(it.key,null,it.value.toRequestBody(("text/plain").toMediaType())))
                        }
                        newBody = newBodyBuilder.build()
                    }
                    else -> newBody = requestBody
                }

                //Check what method type user requested and pass parameters to respective method
                lateinit var requestBuilder: Request.Builder
                requestBuilder = when (request.method) {
                    "POST" -> request.newBuilder().post(newBody)
                    "PUT" -> request.newBuilder().put(newBody)
                    "PATCH" -> request.newBuilder().patch(newBody)
                    "DELETE" -> request.newBuilder().delete(newBody)
                    else -> request.newBuilder().post(newBody)
                }
                request = requestBuilder.build()
            }

        }

        val headerMap = onRequestInterceptor.provideHeaderMap()
        val requestBuilder = request.newBuilder()
        headerMap.asIterable().forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        request = requestBuilder.build()

        return chain.proceed(request)
    }


    private fun getKeyFromContentDisposition(value: String): String {
        val pat = Pattern.compile("(?<=name=\")\\w+")
        val mat = pat.matcher(value)
        while (mat.find()) {
            return (mat.group())
        }
        return ""
    }

    private fun bodyToString(request: RequestBody?): String {
        try {
            val buffer = Buffer()
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }
}

