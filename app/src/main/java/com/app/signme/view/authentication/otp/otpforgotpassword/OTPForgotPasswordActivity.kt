package com.app.signme.view.authentication.otp.otpforgotpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.commonUtils.common.Toaster.show
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityOtpForgotPasswordBinding
import com.app.signme.core.BaseActivity
import com.app.signme.core.utility.OTP_EMPTY
import com.app.signme.core.utility.OTP_INVALID
import com.app.signme.view.authentication.resetpassword.ResetPasswordActivity
import com.app.signme.viewModel.OTPForgotPasswordViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import timber.log.Timber

class OTPForgotPasswordActivity : BaseActivity<OTPForgotPasswordViewModel>() {

    private var dataBinding: ActivityOtpForgotPasswordBinding? = null
    var otp: String = ""
    private var phoneNumber: String = ""
    private var resetKey: String = ""

    companion object {

        fun getStartIntent(
            context: Context,
            phoneNumber: String,
            otp: String,
            resetKey: String
        ): Intent {
            return Intent(context, OTPForgotPasswordActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
                putExtra("resetKey", resetKey)
            }
        }
    }

    override fun setDataBindingLayout() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_otp_forgot_password)
        dataBinding?.lifecycleOwner = this
        dataBinding?.viewModel = viewModel
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-otpForgotPasswordScreen",
            "view_otpForgotPasswordScreen",
            "view_otpForgotPasswordScreen"
        )
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
            resetKey = getStringExtra("resetKey") ?: ""
            show(this@OTPForgotPasswordActivity, otp)
        }

        dataBinding?.let {
            with(it, {
                phoneNumber = this@OTPForgotPasswordActivity.phoneNumber
                startTimmer()
                btnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }

                btnRetry.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Resend OTP Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Resend OTP Button Click"
                    )
                    resendOtp()
                }

                btnSubmitOtp.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Submit OTP Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Submit OTP Button Click"
                    )
                    setFireBaseAnalyticsData(
                        "id-otpVerify",
                        "click_otpVerify",
                        "click_otpVerify"
                    )

                    navigateToResetPassword()
                }
            })
        }
    }

    private fun startTimmer() {
        dataBinding?.let {
            with(it, {
                time = viewModel?.getTimerValue()
                enableRetry = viewModel?.getEnableRetrySettings()
                viewModel?.startTimer()
            })
        }
    }

    private fun navigateToResetPassword() {
        if (viewModel.isValid(otp = dataBinding?.otpView?.text.toString(), sendOtp = otp)) {
            viewModel.cancelTimer(markFinish = true)
            startActivity(
                ResetPasswordActivity.getStartIntent(
                    context = this@OTPForgotPasswordActivity,
                    mobileNumber = PhoneNumberUtils.normalizeNumber(phoneNumber),
                    resetKey = resetKey
                )
            )
        }
    }

    private fun resendOtp() {
        hideKeyboard()
        viewModel.startTimer()
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.getOTPForgotPasswordWithPhone(PhoneNumberUtils.normalizeNumber(this@OTPForgotPasswordActivity.phoneNumber))
            }
        }

    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.validationObserver.observe(this, Observer {
            when (it.failType) {
                OTP_EMPTY -> {
                    show(this, getString(R.string.alert_enter_otp))
                }
                OTP_INVALID -> {
                    show(this, getString(R.string.alert_invalid_otp))

                }
                else -> show(this, it.failType.toString() + "Fail Type not handled")
            }
        })
        viewModel.resendOtpLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP resend success"
                )
                if (!response.data.isNullOrEmpty()) {
                    otp = response.data!![0].otp ?: ""
                    resetKey = response.data!![0].resetKey ?: ""

                    show(this, otp)
                    viewModel.startTimer()
                    dataBinding!!.apply {
                        time = this@OTPForgotPasswordActivity.viewModel.getTimerValue()
                        enableRetry =
                            this@OTPForgotPasswordActivity.viewModel.getEnableRetrySettings()
                    }
                } else {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "OTP resend failed"
                    )
                    Timber.d(response.settings?.message)
                }
            } /*else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP resend failed"
                )
                Timber.d(response.settings?.message)
            }*/
        })
        viewModel.statusCodeLiveData.observe(this, Observer { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        })

    }
}