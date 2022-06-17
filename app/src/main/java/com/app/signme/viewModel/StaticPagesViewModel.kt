package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.StaticPageResponse
import com.app.signme.repository.StaticPagesRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class StaticPagesViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val staticPagesRepository: StaticPagesRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val updateTNCResponseLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val staticPageResponseLiveData = MutableLiveData<TAListResponse<StaticPageResponse>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }


    fun callStaticPage(pageCode: String) {
        compositeDisposable.addAll(
            staticPagesRepository.getStaticPageData(pageCode)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        staticPageResponseLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    /**
     * Call update privacy policy api
     */
    fun callUpdateTNCPrivacyPolicy(pageType: String) {
        compositeDisposable.addAll(
            staticPagesRepository.updateTNCPrivacyPolicy(pageType)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        updateTNCResponseLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }
}