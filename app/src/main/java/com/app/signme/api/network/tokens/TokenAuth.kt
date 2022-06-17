package com.app.signme.api.network.tokens

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class TokenAuth(context: Context) : Interceptor {
    var mContext: Context = context
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        //Build new request
        val builder = request.newBuilder()
        builder.header("Accept", "application/json")

        request = builder.build()

        return chain.proceed(request)
    }

  /*  private fun setAuthHeader(builder: Request.Builder, token: String?) {
        if (token != null)
            builder.header("Authorization", token)
    }*/
}

