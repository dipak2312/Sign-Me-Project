package com.app.signme.api.network

import android.os.AsyncTask
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

@Suppress("DEPRECATION")
internal class CheckInternetTask(callback: TaskFinished<Boolean>) :
    AsyncTask<Void, Void, Boolean>() {

    private val mCallbackWeakReference: WeakReference<TaskFinished<Boolean>> =
        WeakReference<TaskFinished<Boolean>>(callback)

    override fun doInBackground(vararg params: Void): Boolean? {
        try {
            val url: URL
            try {
                url = URL("https://clients3.google.com/generate_204")
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return false
            }
            val urlConnection: HttpURLConnection
            try {
                urlConnection = url.openConnection() as HttpURLConnection
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 1500
            urlConnection.readTimeout = 1500
            urlConnection.connect()
            return urlConnection.responseCode == 204 && urlConnection.contentLength == 0
        } catch (e: IOException) {
            return false
        }

    }

    override fun onPostExecute(isInternetAvailable: Boolean?) {
        val callback = mCallbackWeakReference.get()
        if (callback != null) {
            isInternetAvailable?.let { callback.onTaskFinished(it) }
        }
    }
}