package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.FeedbackResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class FeedbackRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun sendFeedback(
        dataMap: HashMap<String, RequestBody>,
        files: List<MultipartBody.Part>?
    ): Single<TAListResponse<FeedbackResponse>> = networkService.callSendFeedback(dataMap, files)
}