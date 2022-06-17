package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class LoginWithPhoneNumberRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callLoginWithPhoneNumber(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callLoginWithPhone(map = map)

    fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()
}
