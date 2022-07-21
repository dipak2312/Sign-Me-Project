package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import com.app.signme.dataclasses.LikeSuperlikeMatchesResponse
import com.app.signme.dataclasses.LikesMatchesResponse
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.dataclasses.generics.TAGenericResponse
import com.app.signme.dataclasses.generics.TAListResponse
import io.reactivex.Single
import javax.inject.Inject

class MatchesRepository @Inject constructor(
    val networkService: NetworkService
) {

    fun getLikeSuperlikeMatchesList(): Single<TAGenericResponse<LikeSuperlikeMatchesResponse>> =
        networkService.callGetLikeSuperlikeMatchesList()

    fun getViewAllList(pageIndex:String,connectionType:String?): Single<TAListResponse<LikesMatchesResponse>> =
        networkService.callViewAllList(connectionType,pageIndex)


}