package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.Reason
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.repository.AbusiveReportRepository

import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class AbusiveReportViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val abusiveReportRepository: AbusiveReportRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {
    var reportLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var reasonLiveData = MutableLiveData<TAListResponse<Reason>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()

    override fun onCreate() {

    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
    val inputTextSizeLiveData = MutableLiveData<String>()
    fun getInputTextSize(): MutableLiveData<String> {
        return inputTextSizeLiveData
    }

    /**
     * Api call to get report reason
     */
    fun callGetReportReason(reportType:String) {
        compositeDisposable.addAll(
            abusiveReportRepository.callGetReportReason(reportType)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        reasonLiveData.postValue(response)
                    },
                    { error ->

                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }



    /**
     * Api call for Abusive User
     */
    fun callReportAbusiveUser(map: HashMap<String, String>) {
        compositeDisposable.addAll(
            abusiveReportRepository.callReportAbusiveUser(
                map
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        reportLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }


}