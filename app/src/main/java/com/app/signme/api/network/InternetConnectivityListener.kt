package com.app.signme.api.network

interface InternetConnectivityListener {

    fun onInternetConnectivityChanged(isConnected: Boolean)
}