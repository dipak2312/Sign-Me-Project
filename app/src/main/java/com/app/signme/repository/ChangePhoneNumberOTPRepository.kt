package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class ChangePhoneNumberOTPRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun checkUniqueUser(map: HashMap<String, RequestBody>): Single<TAListResponse<OTPResponse>> =
        networkService.callCheckUniqueUser(map)

    fun callChangePhoneNumber(number: String): Single<TAListResponse<JsonElement>> =
        networkService.callChangePhoneNumber(number)
}