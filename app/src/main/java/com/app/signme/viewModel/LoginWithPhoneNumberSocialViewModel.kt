package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.LoginWithPhoneNumberSocialRepository
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.HashMap

class LoginWithPhoneNumberSocialViewModel (
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithPhoneNumberSocialRepository: LoginWithPhoneNumberSocialRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""

    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = AppineersApplication.sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var phoneNumberLoginMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var loginSocialMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()

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
            loginWithPhoneNumberSocialRepository.callLoginWithPhoneNumber(map = map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        phoneNumberLoginMutableLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))

                    }
                )
        )
    }

    /**
     * Api call for login using social account
     */
    fun callLoginWithSocial(socialType: String, socialId: String) {
        val map = HashMap<String, RequestBody>()
        map["social_login_type"] = getStringRequestBody(socialType)
        map["social_login_id"] = getStringRequestBody(socialId)
        map["device_type"] = getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = getStringRequestBody(deviceToken)
        compositeDisposable.addAll(
            loginWithPhoneNumberSocialRepository.callLoginWithPhoneNumberSocial(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        loginSocialMutableLiveData.postValue(response)
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
        //Logger.setUserInfo(loginResponse?.email ?: "")
        AppineersApplication.sharedPreference.isSkip = false
        AppineersApplication.sharedPreference.userDetail = loginResponse
        AppineersApplication.sharedPreference.isLogin = true
        AppineersApplication.sharedPreference.authToken = loginResponse?.accessToken ?: ""
        AppineersApplication.sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        AppineersApplication.sharedPreference.logStatusUpdated =
            loginResponse.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: IConstants.INACTIVE_LOG_STATUS
    }
}