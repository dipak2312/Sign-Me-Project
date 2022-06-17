package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import javax.inject.Inject

class MyActivityRepository  @Inject constructor(
    val networkService: NetworkService
) {

    /* fun callSignUpWithPhone(map: HashMap<String, RequestBody>): Single<TAListResponse<LoginResponse>> =
         networkService.callSignUpWithPhone(fieldMap = map)*/

}