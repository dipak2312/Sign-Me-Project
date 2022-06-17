package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.LoginWithPhoneNumberRepository
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.HashMap

class LoginWithPhoneNumberViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithPhoneNumberRepository: LoginWithPhoneNumberRepository

) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""


    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var phoneNumberLoginMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    val configParamsLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()


    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for login using phone number
     */
    fun callLoginWithPhoneNumber(
        phoneNumber: String,
        password: String
    ) {
        val map = HashMap<String, RequestBody>()
        map["mobile_number"] = getStringRequestBody(phoneNumber)
        map["password"] = getStringRequestBody(password)
        map["device_type"] = getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = getStringRequestBody(deviceToken)
        compositeDisposable.addAll(
            loginWithPhoneNumberRepository.callLoginWithPhoneNumber(map = map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        phoneNumberLoginMutableLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))

                    }
                )
        )
    }

    /**
     * Api call for getting config parameters
     */
    fun callGetConfigParameters() {
      /*  val map = HashMap<String, String>()
        map["app_version"] = BuildConfig.VERSION_NAME
        map["app_version_code"] = BuildConfig.VERSION_CODE.toString()
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = deviceToken
        map["ws_token"] = ""
        map["access_token"] = ""*/
        compositeDisposable.addAll(
            loginWithPhoneNumberRepository.callConfigParameters()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        configParamsLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }
    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        sharedPreference.isSkip = false
        sharedPreference.userDetail = loginResponse
        sharedPreference.isLogin = true
        sharedPreference.isAddress = loginResponse?.address
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
        sharedPreference.isAdRemoved =  loginResponse!!.isAdsFree()
        sharedPreference.logStatusUpdated =
            loginResponse.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: IConstants.INACTIVE_LOG_STATUS
    }
}