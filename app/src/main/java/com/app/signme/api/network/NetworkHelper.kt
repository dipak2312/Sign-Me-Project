package com.app.signme.api.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.app.signme.ServerError
import com.app.signme.commonUtils.logs.Logger
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class NetworkHelper constructor(private val context: Context) {

    companion object {
        private const val TAG = "NetworkHelper"
    }

    fun isNetworkConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            var networkInfo = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            if (!networkInfo)
                networkInfo = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            return networkInfo
        } else {
            var networkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) ?: return false
            if (!networkInfo.isConnected) {
                networkInfo =
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        ?: return false
            }
            return networkInfo.isConnected
        }
    }

    fun castToNetworkError(throwable: Throwable): NetworkError {
        val defaultNetworkError = NetworkError()
        try {
            if (throwable is ConnectException) return NetworkError(0, "0")
            if (throwable !is HttpException) return defaultNetworkError
            val error = GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .fromJson(throwable.response().errorBody()?.string(), NetworkError::class.java)
            return NetworkError(throwable.code(), error.statusCode, error.message)
        } catch (e: IOException) {
            Logger.e(TAG, e.toString())
        } catch (e: JsonSyntaxException) {
            Logger.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            Logger.e(TAG, e.toString())
        }
        return defaultNetworkError
    }

    fun castToServerError(throwable: Throwable): ServerError {
        val defaultServerError = ServerError()
        try {
            if (throwable is ConnectException) return ServerError(0, "1", "Something went wrong")
            if (throwable !is HttpException) return defaultServerError
            val body = throwable.response()?.errorBody()
            val errorJsonObj = JSONObject(body!!.charStream().readText())
            val settingsJsonObj = errorJsonObj.getJSONObject("settings")
            val code = throwable.code()
            val success = settingsJsonObj.getString("success")
            val message = settingsJsonObj.getString("message")
            return ServerError(code, success, message)
        } catch (e: IOException) {
            Logger.e(TAG, e.toString())
        } catch (e: JsonSyntaxException) {
            Logger.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            Logger.e(TAG, e.toString())
        }
        return defaultServerError
    }
}