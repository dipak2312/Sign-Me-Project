package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class LoginWithEmailSocialRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callSignUpWithSocial(map: HashMap<String, RequestBody>, file: MultipartBody.Part?): Single<TAListResponse<LoginResponse>> =
        networkService.callSignUpWithSocial(fieldMap = map,file=file)

    fun callLoginWithEmailSocial(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.loginWithSocial(map)

    fun callLoginWithEmail(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.loginWithEmail(map)

    fun callResendLink(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callSendVerificationLink(map = map)

    fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()


}
