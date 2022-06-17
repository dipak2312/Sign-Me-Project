package com.app.signme.viewModel


import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.repository.SplashRepository
import io.reactivex.disposables.CompositeDisposable


class SplashViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val splashRepository: SplashRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val configParamsPhoneLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var deviceToken : String = ""


    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = AppineersApplication.sharedPreference.deviceToken ?: ""
    }


    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for getting config parameters
     */
    fun callGetConfigParameters() {
    /*    val map = HashMap<String, String>()
        map["app_version"] = BuildConfig.VERSION_NAME
        map["app_version_code"] = BuildConfig.VERSION_CODE.toString()
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = deviceToken
        map["ws_token"] = ""
        map["access_token"] = ""*/
        compositeDisposable.addAll(
            splashRepository.callConfigParameters()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        configParamsPhoneLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }


}