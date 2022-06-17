package com.app.signme.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.PasswordStrength
import com.app.signme.commonUtils.utility.extension.isValidInputEmail
import com.app.signme.commonUtils.utility.extension.isValidInputText
import com.app.signme.commonUtils.utility.extension.toFieldRequestBodyMap
import com.app.signme.commonUtils.utility.validation.createValidationResult
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.SignUpWithEmailRepository
import com.app.signme.view.authentication.signup.signupconfig.SignUpConfigItem
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class SignUpWithEmailViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val application: Application,
    private val signUpWithEmailRepository: SignUpWithEmailRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var signUpLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    var signUpLiveDataSocial = MutableLiveData<TAListResponse<LoginResponse>>()
    val signUpRequestModel = SignUpRequestModel()

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callSignUpWithEmail() {
        compositeDisposable.addAll(
            signUpWithEmailRepository.callSignUpWithEmail(
                signUpRequestModel.toFieldRequestBodyMap(),
                file = if (signUpRequestModel.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                    "user_profile",
                    signUpRequestModel.profileImage
                )
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        signUpLiveData.postValue(response)

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
            loginResponse.logStatusUpdated?.toLowerCase(
                Locale.getDefault()
            ) ?: IConstants.INACTIVE_LOG_STATUS
    }

    /**
     * Validate Signup request
     * @param request SignUpRequestModel
     * @return Boolean Return true if all required fields are valid
     */
    fun isValid(request: SignUpRequestModel): Boolean {
        return when {
            //First Name
            !isValidInputText(
                text = request.firstName, emptyFailType = FIRST_NAME_EMPTY,
                invalidFailType = FIRST_NAME_INVALID, viewId = R.id.tietFirstName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }
            //Last Name
            !isValidInputText(
                text = request.lastName, emptyFailType = LAST_NAME_EMPTY,
                invalidFailType = LAST_NAME_INVALID, viewId = R.id.tietLastName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            //Email
            !isValidInputEmail(
                email = request.email,
                validationObserver = validationObserver
            ) -> {
                false
            }
            request.socialType.isEmpty() && request.password.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_EMPTY, R.id.tietPassword)
                false
            }
            request.socialType.isEmpty() && PasswordStrength.calculateStrength(request.password).value < PasswordStrength.STRONG.value -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_INVALID, R.id.tietPassword)
                false
            }
            request.socialType.isEmpty() && request.confirmPassword.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(CONFORM_PASSWORD_EMPTY, R.id.tietConfirmPassword)
                false
            }
            request.socialType.isEmpty() && request.password != request.confirmPassword -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_NOT_MATCH, R.id.tietConfirmPassword)
                false
            }

            !request.tnc -> {
                validationObserver.value =
                    createValidationResult(TNC_NOT_ACCEPTED, R.id.cbTermsAndPolicy)
                false
            }

            else -> true
        }
    }
}