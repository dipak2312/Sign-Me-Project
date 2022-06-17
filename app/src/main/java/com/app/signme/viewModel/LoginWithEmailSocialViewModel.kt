package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.extension.toFieldRequestBodyMap
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.LoginWithEmailSocialRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.HashMap

class LoginWithEmailSocialViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val loginWithEmailSocialRepository: LoginWithEmailSocialRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    var deviceToken : String = ""

    override fun onCreate() {
        checkForInternetConnection()
        deviceToken = AppineersApplication.sharedPreference.deviceToken ?: ""
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var loginSocialMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var loginEmailMutableLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var resendLinkMutableLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var signUpRequestModel = SignUpRequestModel()
    var signUpLiveDataSocial = MutableLiveData<TAListResponse<LoginResponse>>()
    val configParamsLiveData = MutableLiveData<TAListResponse<VersionConfigResponse>>()

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
    /**
     * Api call for Social SignUp using Social
     */
    fun callSignUpWithSocial() {
        compositeDisposable.addAll(
            loginWithEmailSocialRepository.callSignUpWithSocial(
                signUpRequestModel.toFieldRequestBodyMap(),
                file = if (signUpRequestModel.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                    "user_profile",
                    signUpRequestModel.profileImage
                )
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        //showDialog.postValue(false)
                        signUpLiveDataSocial.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    /**
     * Api call for login using phone number
     */
    fun callLoginWithEmailSocial(socialType: String, socialId: String) {
        val map = HashMap<String, RequestBody>()
        map["social_login_type"] = getStringRequestBody(socialType)
        map["social_login_id"] = getStringRequestBody(socialId)
        map["device_type"] = getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())
        map["device_token"] =getStringRequestBody(deviceToken)
        compositeDisposable.addAll(
            loginWithEmailSocialRepository.callLoginWithEmailSocial(map)
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
            loginWithEmailSocialRepository.callLoginWithEmail(map)
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
            loginWithEmailSocialRepository.callConfigParameters()
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
     * Api call for send verification link
     */

    fun callResendLink(email: String = "") {
        val map = HashMap<String, String>()
        map["email"] = email
        compositeDisposable.addAll(
            loginWithEmailSocialRepository.callResendLink(map)
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
        AppineersApplication.sharedPreference.isSkip = false
        AppineersApplication.sharedPreference.userDetail = loginResponse
        AppineersApplication.sharedPreference.isLogin = true
        AppineersApplication.sharedPreference.isAddress = loginResponse?.address
        AppineersApplication.sharedPreference.authToken = loginResponse?.accessToken ?: ""
        AppineersApplication.sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        AppineersApplication.sharedPreference.logStatusUpdated =
            loginResponse.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: IConstants.INACTIVE_LOG_STATUS
    }
}