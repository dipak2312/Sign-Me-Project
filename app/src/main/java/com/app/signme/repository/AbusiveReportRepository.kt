package com.app.signme.repository


import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.Reason
import com.app.signme.dataclasses.generics.TAListResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class AbusiveReportRepository @Inject constructor(
    val networkService: NetworkService
)
{

    fun callGetReportReason(reportType:String): Single<TAListResponse<Reason>> =
        networkService.callGetReportReason(reportType)

    fun callReportAbusiveUser(map: HashMap<String, String>): Single<TAListResponse<JsonElement>> =
        networkService.callReportAbusiveUser(map)


}