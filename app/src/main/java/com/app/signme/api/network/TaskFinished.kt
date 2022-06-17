package com.app.signme.api.network

interface TaskFinished<T> {
    fun onTaskFinished(data: T)
}