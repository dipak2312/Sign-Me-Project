package com.app.signme.view.authentication.forgotpassword.email


import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.commonUtils.common.Resource
import com.app.signme.commonUtils.common.isValidEmail
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityForgotPasswordWithEmailBinding
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.ForgotPasswordEmailViewModel
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants

class ForgotPasswordWithEmailActivity : BaseActivity<ForgotPasswordEmailViewModel>() {

    private var binding: ActivityForgotPasswordWithEmailBinding? = null
    private var email: String = ""

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password_with_email)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-forgotpasswordscreen",
            "view-forgotpasswordscreen",
            "view-forgotpasswordscreen"
        )
        binding?.let {
            with(it) {

                tietEMail.doAfterTextChanged { text ->
                    email = text?.toString()!!.trim()
                }

                mbtnSendResetLink.setOnClickListener {
                    setFireBaseAnalyticsData("id_forgotclick", "click_forgotclick", "click_forgotclick")
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Send Reset Link Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Send Reset Link Button Click"
                    )
                    when {
                        checkInternet() -> {
                            validateAndSendRequest(tietEMail)
                        }
                    }
                }

                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }

                tietEMail.setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            when {
                                checkInternet() -> {
                                    validateAndSendRequest(tietEMail)
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

    /**Method to validate data and call api request method
     *
     */
    private fun validateAndSendRequest(tietEMail: TextInputEditText) {
        val validate = email.isValidEmail()
        when {
            validate -> {
                tietEMail.error = null
                getForgotPasswordResetLinkWithEmail(email)

            }
            else -> {
                //tietEMail.error = getString(R.string.valid_email)
                if(tietEMail.text.toString().isNullOrEmpty())
                {
                    viewModel.messageString.postValue(Resource.error(getString(R.string.valid_email)))
                }
                else
                {
                    viewModel.messageString.postValue(Resource.error(getString(R.string.enter_valid_email)))
                }

            }
        }
    }

    /**Method to get forgot password response with emailId
     *
     */
    private fun getForgotPasswordResetLinkWithEmail(email: String) {
        hideKeyboard()
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.getForgotPasswordWithEmail(email)
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.forgotPasswordWithEmailLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            when (response.settings?.isSuccess) {
                true -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "Send Reset Link Success"
                    )
                    showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                    Handler().postDelayed({
                        finish()
                    }, IConstants.SNAKE_BAR_SHOW_TIME)
                }
              /*  else -> {
                    if (response.equals("") || response == null) {
                        showMessage(
                            getString(R.string.str_forgot_password_message),
                            IConstants.SNAKBAR_TYPE_ERROR
                        )
                        finish()
                    } else {
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_APP,
                            GenConstants.ENTITY_USER,
                            "Send Reset Link Failed"
                        )
                        showMessage(response.settings!!.message)
                        Timber.d(response.settings?.message)
                    }

                }*/
            }
        })
        viewModel.statusCodeLiveData.observe(this, { serverError ->
            hideProgressDialog()
           handleApiStatusCodeError(serverError)
        })

    }
}