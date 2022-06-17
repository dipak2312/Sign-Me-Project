package com.app.signme.api.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.lang.ref.WeakReference

internal class NetworkChangeReceiver : BroadcastReceiver() {

    private var mNetworkChangeListenerWeakReference: WeakReference<NetworkChangeListener>? = null

    override fun onReceive(context: Context, intent: Intent) {
        val networkChangeListener = mNetworkChangeListenerWeakReference?.get()
        networkChangeListener?.onNetworkChange(isNetworkConnected(context))
    }

    fun setNetworkChangeListener(networkChangeListener: NetworkChangeListener) {
        mNetworkChangeListenerWeakReference =
            WeakReference(networkChangeListener)
    }

    fun removeNetworkChangeListener() {
        if (mNetworkChangeListenerWeakReference != null) {
            mNetworkChangeListenerWeakReference?.clear()
        }
    }

    /**
     * Function to get weather network is connected or not
     */
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val networkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) ?: return false
            return networkInfo.isConnected
        }

    }

    internal interface NetworkChangeListener {
        fun onNetworkChange(isNetworkAvailable: Boolean)
    }

}