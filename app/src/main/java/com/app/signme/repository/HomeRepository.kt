package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import io.reactivex.Single
import javax.inject.Inject

class HomeRepository @Inject constructor(
    val networkService: NetworkService
) {
   /* fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()*/

    fun callBuySubscription(map: HashMap<String, okhttp3.RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuySubscription(map)
}