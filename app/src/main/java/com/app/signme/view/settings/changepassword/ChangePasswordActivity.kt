package com.app.signme.view.settings.changepassword

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.extension.*
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityChangePasswordBinding
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.ChangePasswordViewModel
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : BaseActivity<ChangePasswordViewModel>() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-changePasswordScreen",
            "view_changePasswordScreen",
            "view_changePasswordScreen"
        )
        binding.apply {
            ibtnBack.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()
            }
            btnUpdate.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Update Button Click")
                MSCGenerator.addAction(
                    GenConstants.ENTITY_USER,
                    GenConstants.ENTITY_APP,
                    "Update Button Click"
                )
                changePassword()
            }
        }

        addObservers()
    }

    private fun addObservers() {
        viewModel.validationObserver.observe(this@ChangePasswordActivity) {
            binding.root.focusOnField(it.failedViewId)
            when (it.failType) {
                OLD_PASSWORD_EMPTY -> {
                    showMessage(getString(R.string.alert_enter_old_password))
                }
                PASSWORD_EMPTY -> {
                    showMessage(getString(R.string.alert_enter_new_password))
                }
                PASSWORD_INVALID -> {
                    showMessage(getString(R.string.alert_valid_password))
                }
                CONFORM_PASSWORD_EMPTY -> {
                    showMessage(getString(R.string.alert_enter_confirm_password))
                }
                PASSWORD_NOT_MATCH -> {
                    showMessage(getString(R.string.alert_confirm_password_not_match))
                }

                OLD_NEW_PASSWORD_MATCH -> {
                    showMessage(getString(R.string.alert_password_old_new_match))
                }
            }
        }

        viewModel.changePasswordLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Password Success"
                )
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed({ finish() }, 3000L)//callChangePasswordLogOut()
            } /*else if (!handleApiError(it.settings)) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Password Failed"
                )
                it?.settings?.message?.showSnackBar(this)
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Password Failed"
                )
                showMessage(it.settings!!.message)
            }*/
        }


        viewModel.logoutLiveData.observe(this) {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                viewModel.callPasswordLogout()
                navigateToLoginScreen()
            } /*else if (!handleApiError(it.settings)) {
                it?.settings?.message?.showSnackBar(this@ChangePasswordActivity)
            } else if (it.equals("") || it == null) {
                showMessage(
                    getString(R.string.str_change_password_message),
                    IConstants.SNAKBAR_TYPE_ERROR
                )
                finish()
            } else {
                showMessage(it.settings?.message!!)
            }*/

        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            handleApiStatusCodeError(serverError)
        }

    }

    /**
     * Navigate user to Login Screen if user not logged in or want to logout
     */
    private fun navigateToLoginScreen() {
        AppineersApplication.sharedPreference.userDetail = null
        AppineersApplication.sharedPreference.isLogin = false
        AppineersApplication.sharedPreference.authToken = ""
        AppineersApplication.sharedPreference.configDetails = null
        Logger.setUserInfo("")
        val intent = Intent(
            this@ChangePasswordActivity,
            (application as AppineersApplication).getLoginActivity()
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Perform change password.
     * Check Internet connection and validation of input fields.
     * If all is ok, then all api to change password
     */
    private fun changePassword() {
        binding.root.hideKeyBoard()
        when {
            checkInternet() -> {
                if (viewModel.isValid(
                        oldPassword = binding.tietOldPassword.getTrimText(),
                        newPassword = binding.tietNewPassword.getTrimText(),
                        confirmPassword = binding.tietConfirmPassword.getTrimText()
                    )
                ) {
                    if (tietOldPassword.getTrimText().isValidPassword()
                        && tietNewPassword.getTrimText().isValidPassword()
                        && tietConfirmPassword.getTrimText().isValidPassword()
                    ) {
                        hideKeyboard()
                        when {
                            checkInternet() -> {
                                showProgressDialog(
                                    isCheckNetwork = true,
                                    isSetTitle = false,
                                    title = IConstants.EMPTY_LOADING_MSG
                                )
                                viewModel.callChangePassword(
                                    oldPassword = binding.tietOldPassword.getTrimText(),
                                    newPassword = binding.tietNewPassword.getTrimText()
                                )
                            }
                        }

                    } else {
                        showMessage(
                            getString(R.string.alert_valid_password)
                        )
                    }
                }
            }
        }
    }


}