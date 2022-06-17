package com.app.signme.view.authentication.signup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.app.signme.BuildConfig
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.common.*
import com.app.signme.commonUtils.common.CommonUtils.Companion.openApplicationSettings
import com.app.signme.commonUtils.common.isValidEmail
import com.app.signme.commonUtils.common.isValidMobileNumber
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.PasswordStrength
import com.app.signme.commonUtils.utility.dialog.ImageSourceDialog
import com.app.signme.commonUtils.utility.extension.*
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivitySignUpWithEmailBinding
import com.app.signme.dataclasses.Social
import com.app.signme.dataclasses.generics.Settings
import com.app.signme.dataclasses.response.StaticPage
import com.app.signme.utility.validation.*
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.staticpages.StaticPagesMultipleActivity
import com.app.signme.viewModel.SignUpWithEmailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.chip.Chip
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*


class SignUpWithEmailSocialActivity : BaseActivity<SignUpWithEmailViewModel>() {

    companion object {
        /**
         * Start intent to open signup activity with social information
         * @param mContext Context
         * @param social Social User's social information
         * @return Intent
         */
        fun getStartIntent(mContext: Context, social: Social): Intent {
            return Intent(mContext, SignUpWithEmailSocialActivity::class.java).apply {
                putExtra("social", social)
            }
        }

        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private var imageUrl: String? = null
    private var binding: ActivitySignUpWithEmailBinding? = null
    private var social: Social? = null
    private var captureUri: Uri? = null
    private var currentProfilePathToDelete: String? = ""

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up_with_email)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-signUpScreen", "view_signUpScreen", "view_signUpScreen")
        social = intent.getParcelableExtra("social")

        (application as AppineersApplication).isRemoved = true
        initListeners()
        //setSocialInformation()
        binding?.tvTermsAndPolicy?.let {
            setBoldAndColorSpannable(
                it,
                getString(R.string.terms_n_conditions),
                getString(R.string.privacy_policy)
            )
        }

        val genders = arrayOf("Man", "Woman", "Transgender","Non-binary/non-confirming","Prefer not to respond")
        AddGender(genders)


    }

    fun AddGender(genders: Array<String>)
    {


        for (gender in genders) {
            val chip = Chip(this@SignUpWithEmailSocialActivity)
            chip.text = gender
            binding?.genderChipGroup?.addView(chip)
        }
    }

    /**
     * Set Social information, if user signup with social
     */
    private fun setSocialInformation() {
        if (social != null) {
            //captureUri = Uri.parse(social?.profileImageUrl.toString())
            if (social?.firstName?.isNotEmpty() == true) {
                binding?.tietFirstName?.setText(social?.firstName)
            }

            if (social?.lastName?.isNotEmpty() == true) {
                binding?.tietLastName?.setText(social?.lastName)
            }
            if (social?.emailId?.isNotEmpty() == true) {
                binding?.tietEmail?.setText(social?.emailId)
                binding?.tietEmail?.isEnabled = false
            }
            /*  if (social?.name?.isNotEmpty() == true && social?.name?.contains(" ") == false) {
                  binding?.tietUserName?.setText(social?.name)
                  binding?.tietUserName?.isEnabled = false
              }
  */
            binding?.tietConfirmPassword?.visibility = View.GONE
            binding?.tietPassword?.visibility = View.GONE

            if (social?.profileImageUrl?.isNotEmpty() == true) {
                // binding?.sivUserImage?.setImageResource(R.drawable.user_profile)
                downloadSocialImage(social?.profileImageUrl!!)
            }
        }
    }

    /**
     * Download user's social media profile picture to local for sign up
     * @param url String
     */
    private fun downloadSocialImage(url: String) {
        Glide.with(this@SignUpWithEmailSocialActivity)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageUrl = saveBitmapImage(
                        image = resource,
                        imageFileName = "JPEG_" + social?.name + ".jpg",
                        context = applicationContext
                    )
                    captureUri = Uri.parse(imageUrl)
                    logger.dumpCustomEvent(
                        "Social Image saved at ",
                        social?.profileImageUrl.toString()
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    /**
     * Set spannable text for TNC and Privacy Policy
     * @param textView TextView
     * @param portions Array<out String>
     */
    private fun setBoldAndColorSpannable(textView: TextView, vararg portions: String) {
        val label = textView.text.toString()
        val spannableString1 = SpannableString(label)
        for (portion in portions) {
            val startIndex = label.indexOf(portion)
            val endIndex = startIndex + portion.length
            try {
                if (portion.equals(getString(R.string.terms_n_conditions), true))
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_TERMS_CONDITION,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SignUpWithEmailSocialActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                else if (portion.equals(getString(R.string.privacy_policy), true)) {
                    spannableString1.setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (checkInternet()) {
                                val pageCodeList: ArrayList<StaticPage> = ArrayList()
                                pageCodeList.add(
                                    StaticPage(
                                        pageCode = SettingsActivity.STATIC_PAGE_PRIVACY_POLICY,
                                        forceUpdate = false
                                    )
                                )
                                val intent =
                                    StaticPagesMultipleActivity.getStartIntent(
                                        this@SignUpWithEmailSocialActivity,
                                        pageCodeList
                                    )
                                startActivity(intent)
                            } else {
                                showMessage(
                                    getString(R.string.network_connection_error)
                                )

                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {// override updateDrawState
                            ds.isUnderlineText = false // set to false to remove underline
                        }
                    }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
                spannableString1.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@SignUpWithEmailSocialActivity,
                            R.color.app_color
                        )
                    ), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannableString1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                textView.movementMethod = LinkMovementMethod.getInstance()
                textView.highlightColor = Color.TRANSPARENT
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        textView.text = spannableString1
    }

    private fun initListeners() {
        binding?.let {
            with(it) {

                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }
                btnCreateAccount.setOnClickListener {
                    hideKeyboard()
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sign up Button Click")
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_USER,
                        GenConstants.ENTITY_APP,
                        "sign up"
                    )
                    signUp()
                }

//                sivUserImage.setOnClickListener {
//                    MSCGenerator.addAction(
//                        GenConstants.ENTITY_USER,
//                        GenConstants.ENTITY_APP,
//                        "add image"
//                    )
//                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Image Add Button Clicked")
//                    checkPermission()
//                }
//                btnAdd.setOnClickListener {
//                    MSCGenerator.addAction(
//                        GenConstants.ENTITY_USER,
//                        GenConstants.ENTITY_APP,
//                        "add image"
//                    )
//                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Image Add Button Clicked")
//                    checkPermission()
//                }


            }
        }
    }

    private fun checkPermission() {
        Dexter.withContext(this@SignUpWithEmailSocialActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.isAnyPermissionPermanentlyDenied) {
                        currentProfilePathToDelete = ""
                        if (report.areAllPermissionsGranted()) {
                            if ((application as AppineersApplication).isRemoved) {
                                openImageFromGalleryCamera()

                            } else {
                                ImageSourceDialog(
                                    this@SignUpWithEmailSocialActivity,
                                    onPhotoRemove = {
                                       // binding!!.sivUserImage.setImageResource(R.drawable.ic_user_profile)
                                        currentProfilePathToDelete =
                                            sharedPreference.userDetail?.profileImage
                                        (application as AppineersApplication).isRemoved =
                                            true
                                        // imagePath = sharedPreference.userDetail?.profileImage.toString()
                                    }).show(
                                    supportFragmentManager,
                                    "image source dialog"
                                )


                            }


                        }

                    } else {
                        showMessage(
                            getString(R.string.permission_denied_by_user)
                        )
                        openApplicationSettings(this@SignUpWithEmailSocialActivity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun openImageFromGalleryCamera() {
        ImagePicker.with(this@SignUpWithEmailSocialActivity)
            .cropSquare()//Crop image(Optional), Check Customization for more option
            .compress(1024) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .start(IConstants.MULTI_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        //   setAddress(Autocomplete.getPlaceFromIntent(data))
                        data.dataString?.let { Timber.d(it) }
                    }
                }

                else -> {
                    val fileUri = data?.data
                    captureUri = fileUri
                    handleImageRequest()
                }
            }


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            if (data != null) {
                val status = Autocomplete.getStatusFromIntent(data)
                logger.debugEvent("Place Picker", status.statusMessage ?: "")
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            logger.debugEvent("Place Picker", "Result Canceled")
        }

    }

    private fun handleImageRequest() {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            showProgressDialog(
                isCheckNetwork = true,
                isSetTitle = false,
                title = IConstants.EMPTY_LOADING_MSG
            )
            val currentUri = captureUri
            val imageFile = compressImageFile(currentUri!!)
            if (imageFile != null) {
                val newUri = FileProvider.getUriForFile(
                    this@SignUpWithEmailSocialActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                captureUri = newUri
//                binding?.sivUserImage?.loadCircleImage(
//                    imageFile.absolutePath,
//                    R.drawable.user_profile
//                )
                imageUrl = imageFile.absolutePath
                (application as AppineersApplication).isRemoved = false

            }

            hideProgressDialog()
        }
    }

    /**
     * Function to Send Email SignUp request parameters
     */
    private fun signUp() {
        viewModel.signUpRequestModel.profileImage = getProfileImageUrl()
        viewModel.signUpRequestModel.firstName =
            binding?.tietFirstName?.getTrimText().toString()
        viewModel.signUpRequestModel.lastName =
            binding?.tietLastName?.getTrimText().toString()
        viewModel.signUpRequestModel.userName = ""
        viewModel.signUpRequestModel.email = binding?.tietEmail?.getTrimText().toString()
        viewModel.signUpRequestModel.mobileNumber = ""
        viewModel.signUpRequestModel.password =
            binding?.tietPassword?.getTrimText().toString()
        viewModel.signUpRequestModel.confirmPassword =
            binding?.tietConfirmPassword?.getTrimText().toString()
        viewModel.signUpRequestModel.socialType = social?.type ?: ""
        viewModel.signUpRequestModel.socialId = social?.socialId ?: ""
        viewModel.signUpRequestModel.tnc = binding?.cbTermsAndPolicy?.isChecked!!
        viewModel.signUpRequestModel.deviceType = IConstants.DEVICE_TYPE_ANDROID
        viewModel.signUpRequestModel.deviceModel = getDeviceName()
        viewModel.signUpRequestModel.deviceOs = getDeviceOSVersion()
        viewModel.signUpRequestModel.deviceToken = sharedPreference.deviceToken ?: ""
        viewModel.signUpRequestModel.termsConditionsVersion =
            sharedPreference.applicationTermsCondition ?: ""
        viewModel.signUpRequestModel.privacyPolicyVersion =
            sharedPreference.applicationPrivacyPolicy ?: ""
        when {
            checkInternet() -> {
                if (viewModel.isValid(viewModel.signUpRequestModel)

                ) {
                    if (viewModel.signUpRequestModel.password.isValidPassword()
                        && viewModel.signUpRequestModel.confirmPassword.isValidPassword()
                    ) {
                        when {
                            checkInternet() -> {
                                hideKeyboard()
                                showProgressDialog(
                                    isCheckNetwork = true,
                                    isSetTitle = false,
                                    title = IConstants.EMPTY_LOADING_MSG
                                )
                                viewModel.callSignUpWithEmail()
                            }
                        }
                    } else {
                        showMessage(
                            getString(R.string.alert_valid_password)
                        )
                    }


                    /* if (checkInternet() && ((application as AppineersApplication).getApplicationLoginType() == IConstants.LOGIN_TYPE_EMAIL)
             ) {
                 if (viewModel.signUpRequestModel.socialType.isEmpty()) {
                     viewModel.callSignUpWithEmail()
                 }

             }*/

                }
            }
        }
    }

    private fun isValidSignUpRequest(): Boolean {
        when {
            !viewModel.signUpRequestModel.firstName.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_first_name)
                    )
                }
                return false
            }

            !isOnlyAlphabateAndSpace(viewModel.signUpRequestModel.firstName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_first_name_character)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.firstName.isValidTextLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_first_name_length)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.lastName.isValidText() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_last_name_character)
                    )
                }
                return false
            }

            !isOnlyAlphabateAndSpace(viewModel.signUpRequestModel.lastName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_first_name)
                    )
                }
                return false
            }


            !viewModel.signUpRequestModel.lastName.isValidTextLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_last_name_length)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.email.isValidEmail() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_valid_email)
                    )
                }
                return false
            }
            !viewModel.signUpRequestModel.mobileNumber.isValidMobileNumber() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_phone_number)
                    )
                }
                return false
            }
            !viewModel.signUpRequestModel.mobileNumber.isValidMobileNumberLength() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_phone_number_length)
                    )
                }
                return false
            }

            !isOnlyAlphanumric(viewModel.signUpRequestModel.userName) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_user_name_character)
                    )
                }
                return false

            }
            !viewModel.signUpRequestModel.city.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_enter_city)) }
                return false
            }
            !viewModel.signUpRequestModel.state.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_select_state)) }
                return false
            }
            !isOnlyAlphanumric(viewModel.signUpRequestModel.zipCode) -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_invalid_zip_code_character)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.dob.isValidText() -> {
                runOnUiThread { showMessage(getString(R.string.alert_enter_dob)) }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.password.isEmpty() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty()
                    && viewModel.signUpRequestModel.confirmPassword.isEmpty() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_enter_confirm_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.confirmPassword.isValidPassword() -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_valid_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty()
                    && PasswordStrength.calculateStrength(viewModel.signUpRequestModel.password).value < PasswordStrength.STRONG.value -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_valid_password)
                    )
                }
                return false
            }
            viewModel.signUpRequestModel.socialType.isEmpty() && viewModel.signUpRequestModel.confirmPassword != viewModel.signUpRequestModel.password -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_confirm_password_not_match)
                    )
                }
                return false
            }

            !viewModel.signUpRequestModel.tnc -> {
                runOnUiThread {
                    showMessage(
                        getString(R.string.alert_accept_tnc_and_privacy)
                    )
                }
                return false
            }

            else -> return true
        }
    }

    private fun getProfileImageUrl(): String {
        return if (captureUri == null) {
            ""
        } else {
            imageUrl ?: ""
        }
    }


    override fun setupObservers() {
        super.setupObservers()
        viewModel.signUpLiveData.observe(this) { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up success"
                )
                showMessage(response.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                Handler(mainLooper).postDelayed(
                    { finish() },
                    3000L
                )
            } /*else if (response.equals("") || response == null) {
                showMessage(
                    getString(R.string.str_sign_up_message),
                    IConstants.SNAKBAR_TYPE_ERROR
                )
                finish()
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up failed"
                )
                showMessage(response.settings!!.message)
                Timber.d(response.settings?.message)
            }*/
        }


        viewModel.signUpLiveDataSocial.observe(this, Observer {
            hideProgressDialog()
            if (it.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up success"
                )
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                if (it.data == null) {
                    finish()
                } else {
                    viewModel.saveUserDetails(it.data!![0])
                    navigateToHomeScreen()

                }
                // navigateToHomeScreen()
            } /*else if (it.equals("") || it == null) {
                showMessage(
                    getString(R.string.str_sign_up_message),
                    IConstants.SNAKBAR_TYPE_ERROR
                )
                finish()
            } else {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "sign up failed"
                )
                showMessage(it.settings!!.message)
                Timber.d(it.settings?.message)
            }*/
        })


        viewModel.validationObserver.observe(this) {
            binding?.root?.focusOnField(it.failedViewId)
            if (it.failType != null) {
                focusInvalidInput(it.failType!!)
            }
        }
        viewModel.statusCodeLiveData.observe(this, Observer { serverError ->
            hideProgressDialog()
            if (serverError.code == Settings.INTERNAL_SERVER_ERR0R
                && serverError.success == Settings.EMAIL_SIGN_UP_ERROR
            ) {
                showMessage(
                    getString(R.string.str_sign_up_error_message),
                    IConstants.SNAKBAR_TYPE_ERROR
                )
            } else {
                handleApiStatusCodeError(serverError)
            }
        })

    }


    override fun onPause() {
        super.onPause()
        //internetConnection = false
    }

    /**
     * Show focus on invalid input field
     * @param failType Int
     */
    private fun focusInvalidInput(failType: Int) {
        when (failType) {
            EMAIL_EMPTY -> {
                showMessage(getString(R.string.alert_enter_email))
            }
            EMAIL_INVALID -> {
                showMessage(getString(R.string.alert_enter_valid_email))
            }
            EMAIL_LENGTH -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_max_email_length),
                        resources.getInteger(R.integer.email_max_length)
                    )
                )
            }
            USER_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_user_name))
            }
            USER_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_user_name_length),
                        resources.getInteger(R.integer.user_name_min_length)
                    )
                )
            }

            USER_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_user_name_character))
            }
            FIRST_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_first_name))
            }
            FIRST_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_first_name_length),
                        resources.getInteger(R.integer.first_name_min_length)
                    )
                )
            }

            FIRST_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_first_name_character))
            }

            LAST_NAME_EMPTY -> {
                showMessage(getString(R.string.alert_enter_last_name))
            }
            LAST_NAME_INVALID -> {
                showMessage(
                    String.format(
                        getString(R.string.alert_min_last_name_length),
                        resources.getInteger(R.integer.first_name_min_length)
                    )
                )
            }

            LAST_NAME_CHARACTER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_last_name_character))
            }

            PHONE_NUMBER_EMPTY -> {
                showMessage(getString(R.string.alert_enter_mobile_number))
            }
            PHONE_NUMBER_INVALID -> {
                showMessage(getString(R.string.alert_invalid_phone_number_format))
            }

            PHONE_NUMBER_INVALID_LENGHT -> {
                showMessage(getString(R.string.alert_invalid_phone_number))
            }

            /* DOB_EMPTY -> {
                 showMessage(getString(R.string.alert_enter_dob))
             }
             ADDRESS_EMPTY -> {
                 showMessage(getString(R.string.alert_select_street_address))
             }
             STATE_EMPTY -> {
                 showMessage(getString(R.string.alert_select_state))
             }
             CITY_EMPTY -> {
                 showMessage(getString(R.string.alert_enter_city))
             }
             ZIP_CODE_EMPTY -> {
                 showMessage(getString(R.string.alert_enter_zip_code))
             }
             ZIP_CODE_INVALID -> {
                 showMessage(
                     String.format(
                         getString(R.string.alert_min_zip_code_length),
                         resources.getInteger(R.integer.zip_code_min_length)
                     )
                 )
             }

             ZIP_CODE_CHARACTER_INVALID -> {
                 showMessage(getString(R.string.alert_invalid_zip_code_character))
             }
 */
            PASSWORD_EMPTY -> {
                showMessage(getString(R.string.alert_enter_password))
            }
            PASSWORD_INVALID -> {
                showMessage(getString(R.string.alert_valid_password))
            }
            CONFORM_PASSWORD_EMPTY -> {
                showMessage(getString(R.string.alert_enter_confirm_password))
            }
            PASSWORD_NOT_MATCH -> {
                showMessage(getString(R.string.alert_msg_password_and_confirm_password_not_same))
            }
            TNC_NOT_ACCEPTED -> {
                showMessage(getString(R.string.alert_accept_tnc_and_privacy))
            }
        }
    }
}