package com.app.signme.viewModel

import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.PasswordStrength
import com.app.signme.commonUtils.utility.validation.createValidationResult
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.repository.ChangePasswordRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable

class ChangePasswordViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val changePasswordRepository: ChangePasswordRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val changePasswordLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val logoutLiveData = MutableLiveData<TAListResponse<JsonElement>>()

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    /**
     * Api call for change password
     */
    fun callChangePassword(oldPassword: String, newPassword: String) {
        compositeDisposable.addAll(
            changePasswordRepository.callChangePassword(oldPassword, newPassword)
                .subscribeOn(schedulerProvider.io())
                .subscribe({ response ->
                    changePasswordLiveData.postValue(response)
                },
                { error ->
                    statusCodeLiveData.postValue(handleServerError(error))
                })
        )
    }
    fun callLogout() {
        compositeDisposable.addAll(
            changePasswordRepository.callLogout()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        logoutLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }
    /**
     * Clear all user saved data
     */
    fun callPasswordLogout() {
        AppineersApplication.sharedPreference.userDetail = null
        AppineersApplication.sharedPreference.isLogin = false
        AppineersApplication.sharedPreference.authToken = ""
    }
    /**
     * Validate passwords
     * @param oldPassword String
     * @param newPassword String
     * @param confirmPassword String
     * @return Boolean
     */
    fun isValid(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return when {

            oldPassword.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(OLD_PASSWORD_EMPTY, R.id.tietOldPassword)
                false
            }
            newPassword.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_EMPTY, R.id.tietNewPassword)
                false
            }
            PasswordStrength.calculateStrength(newPassword).value < PasswordStrength.STRONG.value -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_INVALID, R.id.tietNewPassword)
                false
            }
            confirmPassword.isEmpty() -> {
                validationObserver.value =
                    createValidationResult(CONFORM_PASSWORD_EMPTY, R.id.tietConfirmPassword)
                false
            }

            oldPassword == newPassword -> {
                validationObserver.value =
                    createValidationResult(OLD_NEW_PASSWORD_MATCH, R.id.tietNewPassword)
                false
            }

            newPassword != confirmPassword -> {
                validationObserver.value =
                    createValidationResult(PASSWORD_NOT_MATCH, R.id.tietConfirmPassword)
                false
            }

            else -> true
        }
    }
}