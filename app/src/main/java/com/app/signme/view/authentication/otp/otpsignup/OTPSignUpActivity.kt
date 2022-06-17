package com.app.signme.view.authentication.otp.otpsignup


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityOtpSignUpBinding
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.OTPSignUpViewModel
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class OTPSignUpActivity : BaseActivity<OTPSignUpViewModel>() {

    companion object {

        /**
         * Start intent to open OTPSignUpActivity with information
         * @param context [ERROR : Context]
         * @param phoneNumber String Phone Number on which OTP sent
         * @param otp String   OTP which is sent to user
         * @param request [ERROR : SignUpRequestModel]  SignUp request for registration
         * @return Intent
         */
        fun getStartIntent(
            context: Context,
            phoneNumber: String,
            otp: String,
            request: SignUpRequestModel
        ): Intent {
            return Intent(context, OTPSignUpActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
                putExtra("otp", otp)
                putExtra("request", request)
            }
        }
    }

    private var binding: ActivityOtpSignUpBinding? = null
    private var phoneNumber = ""
    var otp = "" // User received OTP
    private var request: SignUpRequestModel? = null
    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_sign_up)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-otpSignUpScreen",
            "view_otpSignUpScreen",
            "view_otpSignUpScreen"
        )
        intent?.apply {
            phoneNumber = getStringExtra("phoneNumber") ?: ""
            otp = getStringExtra("otp") ?: ""
            request = getParcelableExtra("request")
        }
        binding?.let {
            with(it, {
                phoneNumber = "+1 " + this@OTPSignUpActivity.phoneNumber
                time = viewModel?.getTimerValue()
                enableRetry = viewModel?.getEnableRetrySetting()
            })
        }

        initListeners()
    }


    private fun initListeners() {
        binding?.let {
            with(it, {
                Toast.makeText(this@OTPSignUpActivity, otp, Toast.LENGTH_LONG).show()
                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }

                tvResendOtp.setOnClickListener {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Resend OTP Button Click"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Resend OTP Button Click")
                    showProgressDialog(
                        isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG
                    )
                    viewModel?.startTimer()
                    viewModel?.callResendOtp(PhoneNumberUtils.normalizeNumber(this@OTPSignUpActivity.phoneNumber))
                }

                btnValidate.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Validate Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Submit OTP Button Click"
                    )
                    when {
                        checkInternet() -> {
                            if (isValid(otp = binding?.otpView?.text.toString(), sendOtp = otp)) {
                                if (((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_PHONE)
                                ) {
                                    if (request != null) {
                                        viewModel?.signUpRequestModel = request!!
                                        when {
                                            checkInternet() -> {
                                                showProgressDialog(
                                                    isCheckNetwork = true,
                                                    isSetTitle = false,
                                                    title = IConstants.EMPTY_LOADING_MSG
                                                )
                                                if (request?.socialType.isNullOrEmpty()) {
                                                    viewModel?.callSignUpWithPhone()
                                                }
                                            }
                                        }
                                    }


                                }

                            }
                        }
                    }
                }
            })
        }

    }


    /**
     * Validate otp
     * @param otp String OTP, which is intered by user
     * @param sendOtp String OTP, which is sent to user
     * @return Boolean
     */
    private fun isValid(otp: String, sendOtp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                showMessage(getString(R.string.alert_enter_otp))
                false
            }
            otp != sendOtp -> {
                showMessage(getString(R.string.alert_invalid_otp))
                return false
            }
            else -> true
        }
    }


    override fun setupObservers() {
        super.setupObservers()
        hideProgressDialog()
        viewModel.signUpLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP validation success"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                if (response?.data == null) {
                    finish()
                } else {
                    viewModel.saveUserDetails(response.data!![0])
                    navigateToHomeScreen()

                }

            } /*else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP validation failed"
                )
                Timber.d(response.settings?.message)
            }*/
        })
        viewModel.otpLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP generated"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                otp = response.data?.get(0)?.otp ?: ""
            } /*else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP generation failed"
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