package com.app.signme.repository

import com.app.quicklook.dataclasses.NotificationCount
import com.app.quicklook.dataclasses.NotificationDel
import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.UserNotification
import com.app.signme.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callGetNotification(): Single<TAListResponse<UserNotification>> =
        networkService.callGetNotification()

    fun callGetNotificationCount(): Single<TAListResponse<NotificationCount>> =
        networkService.callGetNotificationCount()

//    fun callDeleteNotification(): Single<TAListResponse<JsonElement>> =
//        networkService.callDeleteNotification()

    fun callDeleteNotificationRequest(body: RequestBody): Single<TAListResponse<JsonElement>> =
        networkService.callDeleteNotification(body)

    fun readNotificationList(map: HashMap<String, String>): Single<TAListResponse<NotificationDel>> =
        networkService.readNotificationCall(map)
}