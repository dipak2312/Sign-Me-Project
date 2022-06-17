package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import io.reactivex.Single
import javax.inject.Inject

class OTPForgotPasswordRepository @Inject constructor(
    val networkService: NetworkService
){
    fun getOTPForgotPasswordPhoneResponse(mobileNumber: String): Single<TAListResponse<ResetWithPhone>> = networkService.callForgotPasswordWithPhone(mobileNumber)
}