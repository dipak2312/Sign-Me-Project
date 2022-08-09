package com.app.signme.repository

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.BlockedUser
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.BlockUnblockResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun callLogout(): Single<TAListResponse<JsonElement>> = networkService.callLogOut()

    fun callDeleteAccount(): Single<TAListResponse<JsonElement>> =
        networkService.callDeleteAccount()

    fun callGoAdFree(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callGoAdFree(map = map)

    fun callBuyRegulerSubscription(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuyRegularSubscription(map)
    fun callBuyGoldenSubscription(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuyGoldenSubscription(map)
    fun callGetBlockedUser(): Single<TAListResponse<BlockedUser>> =
        networkService.callGetBlockedUser()

    fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()



    fun callBlockUser(
        map: java.util.HashMap<String, String>
    ): Single<TAListResponse<BlockUnblockResponse>> =
        networkService.callBlockUnBlock(map)
}