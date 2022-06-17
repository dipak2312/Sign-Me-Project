package com.app.signme.viewModel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.extension.timeToMinuteSecond
import com.app.signme.dataclasses.createValidationResult
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.ChangePhoneNumberOTPRepository
import com.google.gson.JsonElement
import io.reactivex.disposables.CompositeDisposable
import java.util.HashMap

class ChangePhoneNumberOTPViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    val application: Application,
    private val changePhoneNumberOTPRepository: ChangePhoneNumberOTPRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    companion object {
        const val COUNT_DOWN_TIMER = 30000L // 30 second
    }

    val changePhoneNumberLiveData = MutableLiveData<TAListResponse<JsonElement>>()
    val otpPhoneNumberLiveData = MutableLiveData<TAListResponse<OTPResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    private val timeLiveData = MutableLiveData<String>()
    private var countDownTimer: CountDownTimer? =
        null  // Count down timer for 60 sec to enable retry button
    private val enableRetryLiveData = MutableLiveData<Boolean>()

    /**
     * Api call for send OTP to user phone number
     * @param phone String User;s phone number on which otp will received
     */

    fun callResendOTP(type: String, email: String = "", phone: String = "", userName: String = "") {
        val map = HashMap<String, okhttp3.RequestBody>()
        map["type"] = getStringRequestBody(type) // phone/email
        map["email"] = getStringRequestBody(email)
        map["mobile_number"] = getStringRequestBody(phone)
        map["user_name"] = getStringRequestBody(userName)

        compositeDisposable.addAll(
            changePhoneNumberOTPRepository.checkUniqueUser(map)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        otpPhoneNumberLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                        //messageString.postValue(Resource.error(error.message))
                    }
                )
        )
    }

    /**
     * Api call for change user's phone number
     * @param phone String New Phone Number
     */
    fun callChangePhoneNumber(phone: String) {
        compositeDisposable.addAll(
            changePhoneNumberOTPRepository.callChangePhoneNumber(phone)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        changePhoneNumberLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

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
     */
    fun cancelTimer() {
        countDownTimer?.cancel()
    }

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
                timeLiveData.value = String.format(
                    application.getString(
                        R.string.lbl_resend_otp_in_minute,
                        millisUntilFinished.timeToMinuteSecond()
                    )
                )
                enableRetryLiveData.value = false
            }
        }
    }

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
     * Validate inputs
     */
    fun isValid(otp: String, sendOtp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                validationObserver.value = createValidationResult(failType = OTP_EMPTY)
                false
            }
            otp != sendOtp -> {
                validationObserver.value = createValidationResult(failType = OTP_INVALID)
                return false
            }
            else -> true
        }
    }

    /**
     * Clear all user saved data
     */
    fun updatePhoneNumber(phoneNumber: String) {
        val userDetail = sharedPreference.userDetail
        userDetail?.mobileNo = phoneNumber
        sharedPreference.userDetail = userDetail
    }
}