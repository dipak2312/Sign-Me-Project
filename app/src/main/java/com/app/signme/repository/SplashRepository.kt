package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import io.reactivex.Single
import javax.inject.Inject

class SplashRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()
}