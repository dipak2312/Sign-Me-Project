package com.app.signme.view.authentication.login.loginwithemailsocial

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.BuildConfig
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.common.isValidEmail
import com.app.signme.commonUtils.common.isValidText
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.dialog.AppUpdateDialog
import com.app.signme.commonUtils.utility.dialog.DialogUtil
import com.app.signme.commonUtils.utility.extension.*
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityLoginWithEmailSocialBinding
import com.app.signme.dataclasses.Social
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.core.AppConfig
import com.app.signme.core.BaseActivity
import com.app.signme.view.SplashActivity
import com.app.signme.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity
import com.app.signme.view.authentication.signup.SignUpWithEmailSocialActivity
import com.app.signme.view.authentication.social.AppleLoginManager
import com.app.signme.view.authentication.social.FacebookLoginManager
import com.app.signme.view.authentication.social.GoogleLoginManager
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.viewModel.LoginWithEmailSocialViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.textfield.TextInputEditText
import com.hb.logger.Logger
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginWithEmailSocialActivity : BaseActivity<LoginWithEmailSocialViewModel>() {

    private var binding: ActivityLoginWithEmailSocialBinding? = null
    private lateinit var social: Social
    var signUpRequestModel = SignUpRequestModel()
    private var captureUri: Uri? = null
    private var imagePath: String = ""
    var isShown = false
    var isTaken = false
    private var updateVersion: VersionConfigResponse? = null


    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_with_email_social)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        (application as AppineersApplication).setApplicationLoginType(IConstants.LOGIN_TYPE_EMAIL_SOCIAL)

        initListeners()
        printHashKey()
    }


    private fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(
                "com.appineers.whitelabel",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun initListeners() {
        binding?.let {
            with(it) {
                mbtnLogin.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Login Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "user login"
                    )
                    hideKeyboard()
                    binding?.tietPassword?.let { it1 ->
                        binding?.tietUsername?.let { it2 ->
                            when {
                                checkInternet() -> {
                                    validateAndSendRequest(
                                        it2,
                                        it1
                                    )
                                }
                            }
                        }
                    }
                }

                ibtnFacebook.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Facebook Login Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "facebook login"
                    )
                    when {
                        checkInternet() -> {
                            facebookLogin()
                        }
                    }

                }

                ibtnGoogle.setOnClickListener {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "google login"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Google Login Button Click")
                    when {
                        checkInternet() -> {
                            googleLogin()
                        }
                    }
                }

                ibtnApple.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Apple Login Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Apple login"
                    )
                    when {
                        checkInternet() -> {
                            appleLogin()
                        }
                    }

                }




                tvCreateNewAccount.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "Sign up Button Click"
                    )
                    hideKeyboard()
                    startActivity(
                        Intent(
                            this@LoginWithEmailSocialActivity,
                            SignUpWithEmailSocialActivity::class.java
                        )
                    )
                }




                tvForgotPassword.setOnClickListener {
                    hideKeyboard()
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "forgot password"
                    )
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Forgot Password Button Click")
                    startActivity(
                        Intent(
                            this@LoginWithEmailSocialActivity,
                            ForgotPasswordWithEmailActivity::class.java
                        )
                    )
                }

                tietPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                    hideKeyboard()
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Login Button Click")
                        MSCGenerator.addAction(
                            GenConstants.ENTITY_USER,
                            GenConstants.ENTITY_APP,
                            "user login"
                        )
                        binding?.tietPassword?.let { it1 ->
                            binding?.tietUsername?.let { it2 ->
                                when {
                                    checkInternet() -> {
                                        validateAndSendRequest(
                                            it2,
                                            it1
                                        )
                                    }
                                }
                            }
                        }
                        return@OnEditorActionListener true
                    }
                    false
                })
            }
        }
    }

    /**
     * Perform Google Login
     */
    private fun googleLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, GoogleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_GOOGLE_LOGIN)
    }

    /**
     * Perform Apple Login
     */
    private fun appleLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, AppleLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_APPLE_LOGIN)
    }

    /**
     * Perform Facebook Login
     */
    private fun facebookLogin() {
        hideKeyboard()
        val intent = Intent(this@LoginWithEmailSocialActivity, FacebookLoginManager::class.java)
        startActivityForResult(intent, IConstants.REQUEST_CODE_FACEBOOK_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IConstants.REQUEST_CODE_FACEBOOK_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("facebook_data")
                    if (mSocial != null) {
                        social = mSocial

                        showProgressDialog(
                            isCheckNetwork = true,
                            isSetTitle = false,
                            title = IConstants.EMPTY_LOADING_MSG
                        )
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_FB,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }

                IConstants.REQUEST_CODE_GOOGLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("google_data")
                    if (mSocial != null) {
                        social = mSocial
                        showProgressDialog(
                            isCheckNetwork = true,
                            isSetTitle = false,
                            title = IConstants.EMPTY_LOADING_MSG
                        )
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_GOOGLE,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }

                IConstants.REQUEST_CODE_APPLE_LOGIN -> {
                    val mSocial = data?.getParcelableExtra<Social>("apple_data")
                    if (mSocial != null) {
                        social = mSocial
                        showProgressDialog(
                            isCheckNetwork = true,
                            isSetTitle = false,
                            title = IConstants.EMPTY_LOADING_MSG
                        )
                        viewModel.callLoginWithEmailSocial(
                            socialType = IConstants.SOCIAL_TYPE_APPLE,
                            socialId = social.socialId ?: ""
                        )
                    } else {
                        showMessage(
                            getString(R.string.msg_no_user_data)
                        )
                    }
                }
            }
        }
    }

    /**
     * Show email not verified dialog
     */
    private fun showResendEmailDialog(message: String) {
        DialogUtil.alert(
            context = this@LoginWithEmailSocialActivity,
            msg = message + "\n" + getString(R.string.msg_resend_email),
            positiveBtnText = getString(R.string.resend),
            negativeBtnText = getString(R.string.cancel),
            il = object : DialogUtil.IL {
                override fun onSuccess() {
                    //showProgress()
                    binding?.tietUsername?.getTrimText()
                        ?.let {
                            showProgressDialog(
                                isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG
                            )
                            viewModel.callResendLink(email = it)
                        }
                }

                override fun onCancel(isNeutral: Boolean) {

                }
            },
            isCancelable = false
        )
    }


    //Method to validate data and call api request method
    private fun validateAndSendRequest(
        tietUsername: TextInputEditText,
        tietPassword: TextInputEditText
    ) {
        val email = tietUsername.text.toString()
        val password = tietPassword.text.toString()
        val validTextEmail = email.isValidText()
        val validateEmail = email.isValidEmail()
        val validateTextPassword = password.isValidText()
        val validatePassword = password.isValidPassword()
        when {
            !validTextEmail -> {
                showMessage(
                    getString(R.string.alert_enter_email)
                )
            }

            !validateEmail -> {
                showMessage(
                    getString(R.string.alert_enter_valid_email)
                )
            }

            !validateTextPassword -> {
                showMessage(getString(R.string.alert_enter_password))
            }
            /* !validatePassword -> {
                 showMessage(getString(R.string.alert_valid_password))
             }*/
            else -> {
                tietUsername.error = null
                tietPassword.error = null
                //viewModel.showDialog.postValue(true)

                callLoginWithEmail(email = email, password = password)
            }
        }
    }

    //Method to get forgot password response with emailId
    private fun callLoginWithEmail(email: String, password: String) {
        hideKeyboard()
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.callLoginWithEmail(email = email, password = password)
                }
            }

        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.loginEmailMutableLiveData.observe(this, Observer { response ->
            when {
                response.settings?.isSuccess == true -> {
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "logged in success"
                    )
                    viewModel.saveUserDetails(response.data!![0])
                    viewModel.callGetConfigParameters()
//                    if (AppineersApplication.sharedPreference.isAddress.equals(
//                            ""
//                        )
//                    ) {
//                        navigateToAddressScreen()
//                    } else {
//
//                    }
                    /*else {
                        navigateToHomeScreen()
                    }*/

                }
                /* response.settings?.success.equals("3") -> {
                     MSCGenerator.addAction(
                         GenConstants.ENTITY_APP,
                         GenConstants.ENTITY_USER,
                         "resend email"
                     )
                     showResendEmailDialog(response.settings?.message ?: "")
                 }
                 else -> {
                     if (response.equals("") || response == null) {
                         showMessage(
                             getString(R.string.str_login_message),
                             IConstants.SNAKBAR_TYPE_ERROR
                         )
                         finish()
                     } else {
                         MSCGenerator.addAction(
                             GenConstants.ENTITY_APP,
                             GenConstants.ENTITY_USER,
                             "logged in failed"
                         )
                         showMessage(response.settings!!.message)
                         Timber.d(response.settings?.message)
                     }

                 }*/
            }


        })

        viewModel.loginSocialMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "social login success"
                )
                showMessage(
                    response.settings!!.message,
                    IConstants.SNAKBAR_TYPE_SUCCESS
                )
                viewModel.saveUserDetails(response.data!![0])
                callEmailSocialSignUp(social)
                viewModel.callGetConfigParameters()
//                if (AppineersApplication.sharedPreference.isAddress.equals(
//                        ""
//                    )
//                ) {
//                    navigateToAddressScreen()
//                } else {
//
//                }


            }
            /* "2" -> {
                 MSCGenerator.addAction(
                     GenConstants.ENTITY_APP,
                     GenConstants.ENTITY_USER,
                     "social login success"
                 )

                 callEmailSocialSignUp(social)

             }
             else -> {
                 if (response.equals("") || response == null) {
                     showMessage(
                         getString(R.string.str_login_message),
                         IConstants.SNAKBAR_TYPE_ERROR
                     )
                     finish()
                 } else {
                     MSCGenerator.addAction(
                         GenConstants.ENTITY_APP,
                         GenConstants.ENTITY_USER,
                         "social login failed"
                     )

                     showMessage(response.settings!!.message)
                     Timber.d(response.settings?.message)
                 }

             }*/


        })

        viewModel.configParamsLiveData.observe(this, Observer { it ->
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                if (!it.data.isNullOrEmpty()) {
                    AppineersApplication.sharedPreference.configDetails =
                        it.data!!.get(0)
                    handleDefaultConfigDetails(it.data!!.get(0))
                }
            }
        })

        viewModel.signUpLiveDataSocial.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Sign Up Success"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                if (response?.data == null) {
                    finish()
                } else {
                    viewModel.saveUserDetails(response.data!![0])
                    startActivity(
                        HomeActivity.getStartIntent(
                            mContext = this@LoginWithEmailSocialActivity,
                            social = social
                        )
                    )
                    finish()
                }
            } /*else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "OTP validation failed"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_ERROR)
                Timber.d(response.settings?.message)
            }*/
        })


        viewModel.resendLinkMutableLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "resend email success"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
            } /*else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "resend email failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }*/
        })
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            if (serverError.code == 403 && serverError.success == "2") {
                if (social.socialId != null) {
                    callEmailSocialSignUp(social)
                } else {
                    serverError.message.showSnackBar(this@LoginWithEmailSocialActivity)

                }

            } else if (serverError.code == 403 && serverError.success == "1") {
                showResendEmailDialog(serverError.message)
            } else {
                handleApiStatusCodeError(serverError)

            }
        }

    }

    /**
     * Send Social SignUp request parameters
     */
    private fun callEmailSocialSignUp(social: Social) {
        if (social.profileImageUrl?.isNotEmpty() == true) {
            // binding?.sivUserImage?.setImageResource(R.drawable.user_profile)
            downloadSocialImage(social.profileImageUrl!!)
        }
        viewModel.signUpRequestModel.profileImage = getProfileImageUrl()
        viewModel.signUpRequestModel.firstName = social.firstName.toString()
        viewModel.signUpRequestModel.lastName = social.lastName.toString()
        viewModel.signUpRequestModel.userName = ""  //social.firstName.toString()
        viewModel.signUpRequestModel.email = social.emailId.toString()
        viewModel.signUpRequestModel.socialType = social.type ?: ""
        viewModel.signUpRequestModel.socialId = social.socialId ?: ""
        viewModel.signUpRequestModel.deviceType = IConstants.DEVICE_TYPE_ANDROID
        viewModel.signUpRequestModel.deviceModel = getDeviceName()
        viewModel.signUpRequestModel.deviceOs = getDeviceOSVersion()
        viewModel.signUpRequestModel.deviceToken =
            AppineersApplication.sharedPreference.deviceToken ?: ""
        hideKeyboard()
        showProgressDialog(
            isCheckNetwork = true,
            isSetTitle = false,
            title = IConstants.EMPTY_LOADING_MSG
        )
        if (checkInternet() && ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL_SOCIAL)
        ) {
            viewModel.callSignUpWithSocial()
            /* if (viewModel.signUpRequestModel.socialType.isEmpty()) {
                 viewModel.callSignUpWithSocial()
             }*/

        }


    }

    /**
     * Download user's social media profile picture to local for sign up
     * @param url String
     */
    private fun downloadSocialImage(url: String) {
        Glide.with(this@LoginWithEmailSocialActivity)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    imagePath = saveBitmapImage(
                        image = resource,
                        imageFileName = "JPEG_" + social?.name + ".jpg",
                        context = applicationContext
                    )
                    captureUri = Uri.parse(imagePath)
                    logger.dumpCustomEvent(
                        "Social Image saved at ",
                        social.profileImageUrl.toString()
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    private fun getProfileImageUrl(): String {
        return if (captureUri == null) {
            ""
        } else {
            imagePath ?: ""
        }
    }

    private fun handleDefaultConfigDetails(configDefaultDetails: VersionConfigResponse?) {
        updateUserSubscriptionData(configDefaultDetails)
        (application as AppineersApplication).isAdRemoved.value =
            configDefaultDetails?.isAdsFree()
        (application as AppineersApplication).isSubscriptionTaken.value =
            configDefaultDetails?.isSubscriptionTaken()


        AppineersApplication.sharedPreference.isAdRemoved = configDefaultDetails!!.isAdsFree()
        //CommonUtils.updateAdConfiguration(application as AppineersApplication,configDefaultDetails)
        updateAdConfiguration(configDefaultDetails)
        val config = configDefaultDetails
        checkUpdateAndTC(config)

        if (!BuildConfig.DEBUG) {
            (application as AppineersApplication).isLogStatusUpdated.value =
                (config.logStatusUpdated.toLowerCase(
                    Locale.getDefault()
                )) == IConstants.ACTIVE_LOG_STATUS
            AppineersApplication.sharedPreference.logStatusUpdated =
                config.logStatusUpdated.toLowerCase(Locale.getDefault())
            if (AppineersApplication.sharedPreference.logStatusUpdated.equals(
                    IConstants.INACTIVE_LOG_STATUS,
                    true
                )
            ) {
                Logger.clearAllLogs()
                Logger.disableLogger()
            } else if (AppineersApplication.sharedPreference.logStatusUpdated.equals(
                    IConstants.ACTIVE_LOG_STATUS,
                    true
                )
            ) {
                Logger.enableLogger()
            }
        } else {
            Logger.enableLogger()
        }


        val userDetails = AppineersApplication.sharedPreference.userDetail
        userDetails?.subscription = configDefaultDetails.subscription
        AppineersApplication.sharedPreference.userDetail = userDetails
        AppineersApplication.sharedPreference.isAddress = configDefaultDetails.address
        AppineersApplication.sharedPreference.isFirstLogin = configDefaultDetails.isFirstLogin
        AppineersApplication.sharedPreference.isAddressMandatory =
            configDefaultDetails.mandatoryArray!![0].isAddressMandatory

    }

    private fun checkUpdateAndTC(configDetails: VersionConfigResponse) {
        if (configDetails.termsConditionsVersion.equals("")
            && configDetails.privacyPolicyVersion.equals(
                ""
            )
        ) {
            startActivity(
                Intent(
                    this@LoginWithEmailSocialActivity,
                    HomeActivity::class.java
                )
            )
            finish()
        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            && !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
            && configDetails.shouldShowVersionDialog(
                this@LoginWithEmailSocialActivity
            )
        ) {
            isShown = true
            updateVersion = configDetails
            callAgreePrivacyPolicyTermsConditions(updateVersion)


        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            && !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
        ) {
            Log.i(
                SplashActivity.TAG,
                "checkUpdateAndTC: " + configDetails.termsConditionsVersion
            )
            callAgreePrivacyPolicyTermsConditions(configDetails)

        } else if (!configDetails.termsConditionsVersion.equals(configDetails.termsConditionsVersionApplication)
            || !configDetails.privacyPolicyVersion.equals(configDetails.privacyPolicyVersionApplication)
        ) {
            callAgreePrivacyPolicyTermsConditions(configDetails)
            Log.i(
                SplashActivity.TAG,
                "checkUpdateAndTC: " + configDetails.termsConditionsVersion
            )

        } else if (configDetails.shouldShowVersionDialog(this@LoginWithEmailSocialActivity)) {
            showNewVersionAvailableDialog(configDetails)
        } else {
            startActivity(
                Intent(
                    this@LoginWithEmailSocialActivity,
                    HomeActivity::class.java
                )
            )
            finish()
        }
    }

    /**
     *
     * @return if update comes for Privacy Policy & Terms & Conditions
     */
    private fun callAgreePrivacyPolicyTermsConditions(
        version: VersionConfigResponse?
    ) {
        var pageCodeList: ArrayList<StaticPage> = ArrayList()
        if (!version!!.termsConditionsVersion.equals(version.termsConditionsVersionApplication)) {
            isTaken = true
            pageCodeList.add(
                StaticPage(
                    pageCode = IConstants.STATIC_PAGE_TERMS_CONDITION,
                    forceUpdate = true
                )
            )
        }

        if (!version.privacyPolicyVersion.equals(version.privacyPolicyVersionApplication)) {
            isTaken = true
            pageCodeList.add(
                StaticPage(
                    pageCode = IConstants.STATIC_PAGE_PRIVACY_POLICY,
                    forceUpdate = true
                )
            )
        }

        if (!pageCodeList.isNullOrEmpty()) {
            isTaken = true
            startActivityForResult(
                StaticPagesMultipleActivity.getStartIntent(
                    mContext = this@LoginWithEmailSocialActivity,
                    pageCodeList = pageCodeList
                ), IConstants.REQUEST_STATIC_PAGE
            )

        }

    }

    /** Update AdConfiguration based on config api response*/
    private fun updateAdConfiguration(data: VersionConfigResponse?) {
        if (data != null) {
            AppineersApplication.sharedPreference.projectDebugLevel = data.projectDebugLevel
            AppineersApplication.sharedPreference.androidBannerId = data.androidBannerId
            AppineersApplication.sharedPreference.androidInterstitialId =
                data.androidInterstitialId
            AppineersApplication.sharedPreference.androidNativeId = data.androidNativeId
            AppineersApplication.sharedPreference.androidRewardedId = data.androidRewardedId
            AppineersApplication.sharedPreference.androidMoPubBannerId =
                data.androidMoPubBannerId
            AppineersApplication.sharedPreference.androidMopubInterstitialId =
                data.androidMopubInterstitialId

            if (AppConfig.AdProvider_ADMob) {
                (application as AppineersApplication).initGoogleAdMobSDK()

            } else if (AppConfig.AdProvider_MoPub) {
                (application as AppineersApplication).initMoPubSDK(data.isAppInDevelopment())
            }

            val adConfig = StringBuilder()
            adConfig.append("projectDebugLevel= " + data.projectDebugLevel)
            adConfig.append(", androidBannerId= " + data.androidBannerId)
            adConfig.append(", androidInterstitialId= " + data.androidInterstitialId)
            adConfig.append(", androidNativeId= " + data.androidNativeId)
            adConfig.append(", androidRewardedId= " + data.androidRewardedId)
            adConfig.append(", androidMoPubBannerId= " + data.androidMoPubBannerId)
            adConfig.append(", androidMopubInterstitialId= " + data.androidMopubInterstitialId)
            Log.i(HomeActivity.TAG, "adConfig Info: " + adConfig.toString())
            logger.dumpCustomEvent("adConfig Info", adConfig.toString())
        }
    }

    /**
     * Update user subscription data
     */
    private fun updateUserSubscriptionData(data: VersionConfigResponse?) {
        if (data != null) {
            val userDetails = AppineersApplication.sharedPreference.userDetail
            if (userDetails != null) {
                if (data.subscription != null) {
                    if (data.subscription?.size!! >= 0) {
                        userDetails.subscription = data.subscription
                        AppineersApplication.sharedPreference.userDetail = userDetails
                    }
                }

            }
        }
    }

    private fun showNewVersionAvailableDialog(version: VersionConfigResponse?) {
        AppUpdateDialog(this, version).show(supportFragmentManager, "update dialog")
    }
}


