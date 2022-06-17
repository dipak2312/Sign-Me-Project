package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class ForgotPasswordEmailRepository @Inject constructor(
        val networkService: NetworkService
) {

    fun getForgotPasswordEmailResponse(email: String): Single<TAListResponse<JsonElement>> = networkService.callForgotPasswordWithEmail(email)
}