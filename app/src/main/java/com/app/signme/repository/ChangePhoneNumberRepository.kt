package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import okhttp3.RequestBody
import javax.inject.Inject

class ChangePhoneNumberRepository @Inject constructor(
    val networkService: NetworkService
) {
    fun checkUniqueUser(map: HashMap<String, RequestBody>) = networkService.callCheckUniqueUser(map)
}