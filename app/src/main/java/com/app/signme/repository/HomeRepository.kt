package com.app.signme.repository

import com.app.quicklook.dataclasses.NotificationCount
import com.app.signme.api.network.NetworkService
import com.app.signme.application.AppineersApplication
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

    fun getSwiperList(availableIds:String?,signId:String?): Single<TAListResponse<SwiperViewResponse>> =
        networkService.callGetSwiperList(availableIds,signId)

    fun callGetOtherUserDetailsList(otherUserId:String?): Single<TAListResponse<OtherUserDetailsResponse>> =
        networkService.callGetOtherUserDetails(otherUserId)

    fun callLikeSuperlikeCancel(map:HashMap<String,String>): Single<TAListResponse<LikeUnLikeResponse>> =
        networkService.callLikeSuperlikeCancel(map)

    fun callUnlikeUnsuperlike(map:HashMap<String,String>): Single<TAListResponse<JsonElement>> =
        networkService.callUnlikeUnsuperlike(map)

    fun unMatchUser(userId:String): Single<TAListResponse<JsonElement>> =
        networkService.unMatch(userId)

    fun callGetNotificationCount(): Single<TAListResponse<NotificationCount>> =
        networkService.callGetNotificationCount()
}