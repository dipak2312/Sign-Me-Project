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
import com.app.signme.repository.LoginWithEmailRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.HashMap

class LoginWithEmailViewModel (
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithEmailRepository: LoginWithEmailRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""

    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = AppineersApplication.sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var loginEmailMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var resendLinkMutableLiveData = MutableLiveData<TAListResponse<JsonElement>>()



    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for login using phone number
     */
    fun callLoginWithEmail(email: String, password: String) {
        val map = HashMap<String, RequestBody>()
        map["email"] = getStringRequestBody(email)
        map["password"] = getStringRequestBody(password)
        map["device_type"] = getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = getStringRequestBody(deviceToken)
        compositeDisposable.addAll(
            loginWithEmailRepository.callLoginWithEmail(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        loginEmailMutableLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    /**
     * Api call for send verification link
     */

    fun callResendLink(email: String = "") {
        val map = HashMap<String, String>()
        map["email"] = email
        compositeDisposable.addAll(
            loginWithEmailRepository.callResendLink(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        resendLinkMutableLiveData.postValue(response)
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