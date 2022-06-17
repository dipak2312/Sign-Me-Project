package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class SignUpRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callSignUpWithEmail(map: HashMap<String, RequestBody>, file: MultipartBody.Part?): Single<TAListResponse<JsonElement>> =
        networkService.callSignUpWithEmail(fieldMap = map,file=file)

    fun callSignUpWithSocial(map: HashMap<String, RequestBody>, file: MultipartBody.Part?): Single<TAListResponse<LoginResponse>> =
        networkService.callSignUpWithSocial(fieldMap = map,file=file)

    fun callCheckUniqueUser(map: HashMap<String, RequestBody>): Single<TAListResponse<OTPResponse>> =
        networkService.callCheckUniqueUser(map)

}
