package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class AddAddressRepository @Inject constructor(
    val networkService: NetworkService
) {

   /* fun callSignUpWithPhone(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callSignUpWithPhone(fieldMap = map)
*/
    fun updateUserProfile(map: HashMap<String, RequestBody>, file: MultipartBody.Part?
    ): Single<TAListResponse<LoginResponse>> =
        networkService.callUpdateUserProfile(map,file)


}