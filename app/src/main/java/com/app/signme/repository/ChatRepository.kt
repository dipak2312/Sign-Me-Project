package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.BlockUnblockResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ChatRepository @Inject constructor(
    val networkService: NetworkService
) {


    fun callBlockUser(
        map: java.util.HashMap<String, String>
    ): Single<TAListResponse<BlockUnblockResponse>> =
        networkService.callBlockUnBlock(map)

    fun callSendChatNotificationApi(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callSendChatNotificationApi(map)
}