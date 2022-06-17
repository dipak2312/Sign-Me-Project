package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun updateUserProfile(map: HashMap<String, RequestBody>, file: MultipartBody.Part?
    ): Single<TAListResponse<LoginResponse>> =
        networkService.callUpdateUserProfile(map,file)

    fun callUploadMultimediaMedia(
        dataMap: HashMap<String, RequestBody>,
        file: MultipartBody.Part?
    ): Single<TAListResponse<JsonElement>> = networkService.callUploadMultimediaMedia(dataMap, file)

    fun callDeleteMultimediaMedia(
        map: HashMap<String, String>
    ): Single<TAListResponse<JsonElement>> = networkService.callDeleteMultimediaMedia(map = map)

    fun callGetProfile(): Single<TAListResponse<LoginResponse>> =
        networkService.callGetProfile()

}