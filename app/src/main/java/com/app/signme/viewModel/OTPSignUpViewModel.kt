package com.app.signme.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants.Companion.COUNT_DOWN_TIMER
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.extension.timeToMinuteSecond
import com.app.signme.commonUtils.utility.extension.toFieldRequestBodyMap
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.app.signme.repository.OTPSignUpRepository
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*

class OTPSignUpViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val otpSignUpRepository: OTPSignUpRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var otpLiveData = MutableLiveData<TAListResponse<OTPResponse>>()
    var signUpLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    var signUpRequestModel = SignUpRequestModel()
    private val timeLiveData = MutableLiveData<String>()
    private var countDownTimer: CountDownTimer? = null  // Count down timer for 60 sec to enable retry button
    private val enableRetryLiveData = MutableLiveData<Boolean>()


    /**
     * Get timer live data to update timer value in view
     * @return MutableLiveData<String>
     */
    fun getTimerValue(): MutableLiveData<String> {
        return timeLiveData
    }

    fun getEnableRetrySetting(): MutableLiveData<Boolean> {
        return enableRetryLiveData
    }

    /**
     * Start count down timer from 60 second
     */
    fun startTimer() {
        countDownTimer = getCountDownTimer()
        countDownTimer?.start()
    }

    /**
     * Cancel count  down timer
     *//*
    fun cancelTimer() {
        countDownTimer?.cancel()
    }*/

    /**
     * Retrun CountDowmTimer object
     * @return CountDownTimer
     */
    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(COUNT_DOWN_TIMER, 1000) {
            override fun onFinish() {
                timeLiveData.value = ""
                enableRetryLiveData.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
                timeLiveData.value = String.format("Resend OTP in 60 seconds", millisUntilFinished.timeToMinuteSecond())
                enableRetryLiveData.value = false
            }
        }
    }


    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

    fun callSignUpWithSocial() {
        compositeDisposable.addAll(
            otpSignUpRepository.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap())
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

    fun callSignUpWithPhone() {
        compositeDisposable.addAll(
            otpSignUpRepository.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap())
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
     * Api call for send OTP to user phone number
     * @param mobileNumber String User's phone number on  which otp will received
     */

    fun callResendOtp(mobileNumber: String) {
        val map = HashMap<String, RequestBody>()
        map["type"] = getStringRequestBody("phone") // phone/email
        map["email"] = getStringRequestBody("")
        map["mobile_number"] = getStringRequestBody(mobileNumber)
        map["user_name"] = getStringRequestBody("")

        compositeDisposable.addAll(
            otpSignUpRepository.callCheckUniqueUser(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        otpLiveData.postValue(response)
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
        sharedPreference.authToken = loginResponse?.accessToken ?: ""
        sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        sharedPreference.logStatusUpdated = loginResponse.logStatusUpdated?.toLowerCase(Locale.getDefault()) ?: ""
    }
}