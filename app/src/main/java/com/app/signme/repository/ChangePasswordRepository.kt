package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ChangePasswordRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun callChangePassword(oldPassword: String, newPassword: String)
    : Single<TAListResponse<JsonElement>> = networkService.callChangePassword(oldPassword,newPassword)

    fun callLogout(): Single<TAListResponse<JsonElement>> = networkService.callLogOut()

}