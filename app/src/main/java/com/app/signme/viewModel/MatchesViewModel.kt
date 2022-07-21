package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.LikeSuperlikeMatchesResponse
import com.app.signme.dataclasses.LikesMatchesResponse
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.dataclasses.generics.TAGenericResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.repository.MatchesRepository
import io.reactivex.disposables.CompositeDisposable

class MatchesViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val matchesRepository: MatchesRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val LikeSuperlikeMatchesLiveData= MutableLiveData<TAGenericResponse<LikeSuperlikeMatchesResponse>>()
    val viewLikeSuperlikeMatchesLiveData= MutableLiveData<TAListResponse<LikesMatchesResponse>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun getLikeSuperlikeMatchesList() {
        compositeDisposable.addAll(
            matchesRepository.getLikeSuperlikeMatchesList()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        LikeSuperlikeMatchesLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun getViewAllList(pageIndex:String,connectionType:String?) {
        compositeDisposable.addAll(
            matchesRepository.getViewAllList(pageIndex,connectionType)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        viewLikeSuperlikeMatchesLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

}