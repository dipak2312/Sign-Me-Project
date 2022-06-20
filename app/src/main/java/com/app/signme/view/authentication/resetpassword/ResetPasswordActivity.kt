package com.app.signme.view.authentication.resetpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.common.Resource
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.isValidPassword
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityResetPasswordBinding
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.ResetPasswordViewModel
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import timber.log.Timber

class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel>() {

    private var binding: ActivityResetPasswordBinding? = null
    private var newPassword: String = ""
    private var confirmPassword: String = ""
    private var mobileNumber: String = ""
    private var resetKey: String = ""
    private var validatePassword: Boolean = false

    companion object {
        fun getStartIntent(context: Context, mobileNumber: String, resetKey: String): Intent {
            return Intent(context, ResetPasswordActivity::class.java).apply {
                putExtra("phoneNumber", mobileNumber)
                putExtra("resetKey", resetKey)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this

        mobileNumber = intent?.getStringExtra("phoneNumber") ?: ""
        resetKey = intent?.getStringExtra("resetKey") ?: ""
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this@ResetPasswordActivity)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-resetPasswordScreen",
            "view_resetPasswordScreen",
            "view_resetPasswordScreen"
        )
        binding?.let {
            with(it) {

                tietPassword.doAfterTextChanged { enteredNewPassword ->
                    newPassword = enteredNewPassword?.toString()?.trim()!!
                }

                tietConfirmPassword.doAfterTextChanged { enteredConfirmPassword ->
                    confirmPassword = enteredConfirmPassword?.toString()?.trim()!!
                }

                btnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }

                mbtnNext.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Next Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Next Button Click"
                    )
                    when {
                        checkInternet() -> {
                            validateAndSendRequest(tietPassword)
                        }
                    }
                }

                tietConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            when {
                                checkInternet() -> {
                                    validateAndSendRequest(tietPassword)
                                    true
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
            }
        }
    }

    //Method to validate user input data and call api request
    private fun validateAndSendRequest(
        tietPassword: TextInputEditText
    ) {
        validatePassword = newPassword.isValidPassword()

        when {
            validatePassword -> {
                tietPassword.error = null
                if (newPassword == confirmPassword) {
                    hideKeyboard()
                    when {
                        checkInternet() -> {

                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG
                            )
                            viewModel.callResetPassword(newPassword, mobileNumber, resetKey)
                        }
                        else -> showMessage(getString(R.string.network_connection_error))
                    }
                    //  callResetPassword(newPassword, mobileNumber, resetKey)
                } else {
                    showMessage(getString(R.string.password_not_same))
                }
            }

            else -> {
                tietPassword.error = getString(R.string.valid_password)
                viewModel.messageString.postValue(Resource.error(getString(R.string.valid_password)))
            }
        }
    }

    //Method to get reset password response
    private fun callResetPassword(newPassword: String, mobileNumber: String, resetKey: String) {
        hideKeyboard()
        when {
            checkInternet() -> {
                CoroutineScope(IO).launch {
                    showProgressDialog(
                        isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG
                    )
                    viewModel.callResetPassword(newPassword, mobileNumber, resetKey)
                }
            }
            else -> showMessage(getString(R.string.network_connection_error))
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.resetPasswordLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            when (response.settings?.isSuccess) {
                true -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "Reset Password Success"
                    )
                    showMessage(response.settings!!.message)
                    Timber.d(response.settings!!.message)
                    startActivity(
                        Intent(
                            this@ResetPasswordActivity,
                            (application as AppineersApplication).getLoginActivity()
                        )
                    )
                }
               /* else -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "Reset Password Failed"
                    )
                    showMessage(response.settings!!.message)
                    Timber.d(response.settings?.message)
                }*/
            }
        })
        viewModel.statusCodeLiveData.observe(this, Observer { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        })

    }
}