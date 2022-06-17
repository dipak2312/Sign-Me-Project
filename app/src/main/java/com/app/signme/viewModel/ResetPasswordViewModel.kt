package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.ResetPasswordRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class ResetPasswordViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val resetPasswordRepository: ResetPasswordRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val resetPasswordLiveData = MutableLiveData<TAListResponse<JsonElement>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callResetPassword(newPassword: String, mobileNumber: String, resetKey: String) {
        compositeDisposable.addAll(
            resetPasswordRepository.callResetPassword(newPassword, mobileNumber, resetKey)
                .subscribeOn(schedulerProvider.io())
                    .subscribe({ response ->
                        resetPasswordLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    })
        )
    }
}