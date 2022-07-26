package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.LikeUnLikeResponse
import com.app.signme.dataclasses.OtherUserDetailsResponse
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.DeleteMediaResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.google.gson.JsonElement
import io.reactivex.Single
import javax.inject.Inject

class HomeRepository @Inject constructor(
    val networkService: NetworkService
) {
   /* fun callConfigParameters(): Single<TAListResponse<VersionConfigResponse>> =
        networkService.callConfigParameters()*/

    fun callBuySubscription(map: HashMap<String, okhttp3.RequestBody>): Single<TAListResponse<LoginResponse>> =
        networkService.callBuySubscription(map)

    fun getSwiperList(pageIndex:String?,signId:String?): Single<TAListResponse<SwiperViewResponse>> =
        networkService.callGetSwiperList(pageIndex,signId)

    fun callGetOtherUserDetailsList(otherUserId:String?): Single<TAListResponse<OtherUserDetailsResponse>> =
        networkService.callGetOtherUserDetails(otherUserId)

    fun callLikeSuperlikeCancel(map:HashMap<String,String>): Single<TAListResponse<LikeUnLikeResponse>> =
        networkService.callLikeSuperlikeCancel(map)

    fun unMatchUser(userId:String): Single<TAListResponse<JsonElement>> =
        networkService.unMatch(userId)
}