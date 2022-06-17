package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.ForgotPasswordEmailRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class ForgotPasswordEmailViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        private val forgotPasswordEmailRepository: ForgotPasswordEmailRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val forgotPasswordWithEmailLiveData = MutableLiveData<TAListResponse<JsonElement>>()

    /**
     * Api call for getting password via email
     */
    fun getForgotPasswordWithEmail(email: String) {
        compositeDisposable.addAll(
                forgotPasswordEmailRepository.getForgotPasswordEmailResponse(email)
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                { response ->
                                    //showDialog.postValue(false)
                                    forgotPasswordWithEmailLiveData.postValue(response)
                                },
                                { error ->
                                    statusCodeLiveData.postValue(handleServerError(error))
                                }
                        )
        )
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
}