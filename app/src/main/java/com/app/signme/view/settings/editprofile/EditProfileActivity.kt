package com.app.signme.view.settings.editprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.app.signme.BuildConfig
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.common.CommonUtils
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.IConstants.Companion.MULTI_IMAGE_REQUEST_CODE
import com.app.signme.commonUtils.utility.dialog.ImageSourceDialog
import com.app.signme.commonUtils.utility.extension.*
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityEditProfileBinding
import com.app.signme.db.entity.MediaFileEntity
import com.app.signme.db.repo.MediaFileRepository
import com.app.signme.viewModel.UserProfileViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.*
import java.io.File
import java.util.*


class EditProfileActivity : BaseActivity<UserProfileViewModel>(), RecyclerViewActionListener {

    private var captureUri: Uri? = null
    private var imagePath: String = ""
    private var selectedPlace: Place? = null
    var binding: ActivityEditProfileBinding? = null
    var placeSearchBy = IConstants.CITY_SEARCH
    private var currentProfilePathToDelete: String? = ""
    private var selectedProfileImage: Boolean? = false
    var userId: String? = null
    var deletedImageId: String? = ""
    var mediaFile = java.util.ArrayList<String>()

    companion object {
        const val TAG = "HomeActivity"

        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, EditProfileActivity::class.java).apply {

            }
        }
    }


    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-editprofilescreen",
            "view-editprofilescreen",
            "view-editprofilescreen"
        )
        binding?.user = sharedPreference.userDetail
        (application as AppineersApplication).isRemoved = sharedPreference.userDetail?.profileImage.equals("")


        initListener()
        addObservers()

    }

    private fun initListener() {
        binding?.apply {
            ibtnBack.setOnClickListener {
                setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()

            }


            btnUpdate.setOnClickListener {
                setFireBaseAnalyticsData("id-saveprofile", "click_saveprofile", "click_saveprofile")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Validate and Submit Click")
                MSCGenerator.addAction(
                    GenConstants.ENTITY_USER,
                    GenConstants.ENTITY_APP,
                    "Validate and Submit Profile"
                )
                performEditProfile()
            }
            sivUserImage.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
                checkPermission()
            }
            btnAdd.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
                checkPermission()
            }

//            userName = binding?.textUserName!!.getTrimText(),
//            firstName = binding?.tietFirstName!!.getTrimText(),
//            lastName = binding?.tietLastName!!.getTrimText(),


            setProfileImage()


        }

    }

    private fun setProfileImage() {
        if (!sharedPreference.userDetail?.profileImage.equals("")) {
            loadImage(binding!!.sivUserImage, sharedPreference.userDetail?.profileImage)
        }
    }

    /**
     * Perform edit profile
     */
    private fun performEditProfile() {
        val signUpRequest = viewModel.getEditProfileRequest(
            //  userProfileImage = getProfileImageUrl(), //imagePath,
            userProfileImage = imagePath, //imagePath,
            firstName = binding?.tietFirstName!!.getTrimText(),
            lastName = binding?.tietLastName!!.getTrimText(),
            latitude = if (selectedPlace != null) (selectedPlace?.latLng?.latitude
                ?: 0.0).toString() else sharedPreference.userDetail?.latitude ?: "0.0",
            longitude = if (selectedPlace != null) (selectedPlace?.latLng?.longitude
                ?: 0.0).toString() else sharedPreference.userDetail?.longitude ?: "0.0",

            deleteImageProfile = currentProfilePathToDelete.toString(),   //currentProfilePathToDelete.toString()
            deleteImageIds = deletedImageId!!
        )
        if (viewModel.isValid(signUpRequest)) {
            when {
                checkInternet() -> {
                    showProgressDialog(
                        isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG
                    )
                    viewModel.updateUserProfile(signUpRequest)
                }
            }

        }
    }

    private fun addObservers() {
        hideProgressDialog()
        viewModel.updateUserLiveData.observe(this) {
            hideProgressDialog()
            if (it.settings?.success == "1") {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Profile updated"
                )
                showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                binding!!.btnUpdate.isClickable=false
                binding!!.btnUpdate.isFocusable=false
                Handler(mainLooper).postDelayed(
                    {
                        sharedPreference.userDetail = it.data!![0]
                        (application as AppineersApplication).isProfileUpdated.value = true
                        finish()
                    }, IConstants.SNAKE_BAR_SHOW_TIME
                )
//                if (it.data!!.size > 0) {
//                    userId = it.data!![0].userId
//                    if (userId != null) {
//                        if (!mediaFile.equals("") && deletedImageId.equals("")) {
//                            startUploadService()
//
//                        }
//
//
//                    }
//                    Handler(mainLooper).postDelayed(
//                        { finish() },
//                        3000L
//                    )
//                }

            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }

        viewModel.validationObserver.observe(this@EditProfileActivity) {
            binding?.root?.focusOnField(it.failedViewId)
            if (it.failType != null)
                focusInvalidInput(it.failType!!)
        }


    }
    private fun focusInvalidInput(failType: Int) {
        binding?.apply {
            when (failType) {

                FIRST_NAME_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_first_name)
                        )
                    }
                    tietFirstName.requestFocus()
                }
                FIRST_NAME_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_first_name_character)
                        )
                    }
                    tietFirstName.requestFocus()
                }

                FIRST_NAME_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_first_name_character)
                        )
                    }
                    tietFirstName.requestFocus()
                }

                LAST_NAME_EMPTY -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_enter_last_name)
                        )
                    }
                    tietLastName.requestFocus()
                }
                LAST_NAME_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_last_name_character)
                        )
                    }
                    tietLastName.requestFocus()
                }

                LAST_NAME_CHARACTER_INVALID -> {
                    runOnUiThread {
                        showMessage(
                            getString(R.string.alert_invalid_last_name_character)
                        )
                    }
                    tietLastName.requestFocus()
                }


            }
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }


    /* */
    /**
     * Open date picker and set date of birth
     *//*
    private fun setDOB() {
        val calendar = Calendar.getInstance()
        if (binding?.tietDOB?.getTrimText()?.isNotEmpty()!!) {
            calendar.time = binding!!.tietDOB.getTrimText().toMMDDYYYDate()
        }
        val datePicker = DatePickerDialog(
            this@EditProfileActivity, R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                binding!!.tietDOB.setText(viewModel.getDateFromPicker(year, month, dayOfMonth))
                binding!!.tietDOB.error = null
            }, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }
*/
    /**
     * Open auto complete place picker to get address
     */
    private fun openPlacePicker() {
        hideKeyboard()
        Places.initialize(applicationContext,(resources.getString(R.string.google_places_api_key)))
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields =
            listOf(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent: Intent
        if (placeSearchBy == IConstants.CITY_SEARCH) {
            intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            ).setTypeFilter(TypeFilter.CITIES)
                .build(this)
            /**
             * Use US if we have to show places only for USA
             */
            //.setCountry("US")

        } else {
            intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            )
                .build(this)
            /**
             * Use US if we have to show places only for USA
             */
            // .setCountry("US")

        }
        startActivityForResult(intent, IConstants.AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun checkPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.isAnyPermissionPermanentlyDenied) {
                        if (report.areAllPermissionsGranted()) {
                            currentProfilePathToDelete = ""
                            selectedProfileImage = true
                            if (report.areAllPermissionsGranted()) {
                                if ((application as AppineersApplication).isRemoved
                                ) {
                                    openImageFromGalleryCamera()

                                } else {
                                    ImageSourceDialog(this@EditProfileActivity, onPhotoRemove = {
                                        binding!!.sivUserImage.setImageResource(R.drawable.ic_profile_img)
                                        currentProfilePathToDelete =
                                            sharedPreference.userDetail?.profileImage
                                        // imagePath = sharedPreference.userDetail?.profileImage.toString()
                                        (application as AppineersApplication).isRemoved = true
                                    }).show(supportFragmentManager, "image source dialog")

                                }

                            }
                        }
                    } else {
                        showMessage(
                            getString(R.string.permission_denied_by_user)
                        )
                        CommonUtils.openApplicationSettings(this@EditProfileActivity)
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
        ImagePicker.with(this@EditProfileActivity)
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
            .start(MULTI_IMAGE_REQUEST_CODE)
    }

    private fun checkMediaUploadPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.isAnyPermissionPermanentlyDenied) {
                        if (report.areAllPermissionsGranted()) {
                            ImagePicker.with(this@EditProfileActivity)
                                .crop()
                                .compress(2048) //Final image size will be less than 1 MB(Optional)
                                .maxResultSize(
                                    1580,
                                    2580
                                )    //Final image resolution will be less than 1080 x 1080(Optional)
                                .galleryMimeTypes(  //Exclude gif images
                                    mimeTypes = arrayOf(
                                        "image/png",
                                        "image/jpg",
                                        "image/jpeg"
                                    )
                                )
                                .start(MULTI_IMAGE_REQUEST_CODE)
                            selectedProfileImage = false


                        }
                    } else {
                        showMessage(
                            getString(R.string.permission_denied_by_user)
                        )
                        CommonUtils.openApplicationSettings(this@EditProfileActivity)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IConstants.AUTOCOMPLETE_REQUEST_CODE -> {
                    if (data != null) {
                        //setAddress(Autocomplete.getPlaceFromIntent(data))
                    }
                }
                MULTI_IMAGE_REQUEST_CODE -> {
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
            imagePath = imageFile!!.absolutePath
            (application as AppineersApplication).isRemoved = false
            if (imageFile != null) {
                val newUri = FileProvider.getUriForFile(
                    this@EditProfileActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                captureUri = newUri
                if (selectedProfileImage == false) {

                } else if (selectedProfileImage == true) {
                    binding?.sivUserImage?.loadCircleImage(
                        imageFile.absolutePath,
                        R.drawable.user_profile
                    )

                }

            }

            hideProgressDialog()
        }
    }

    private fun deleteFiles() {
        val storage = this.cacheDir
        val filesPath = "$storage/files"
        val filesDir = File(filesPath)
        if (filesDir.exists()) {
            filesDir.deleteRecursively()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        deleteFiles()
    }

//    private fun setAddress(place: Place) {
//        selectedPlace = place
//        val locationAddress =
//            place.addressComponents?.let { super.getParseAddressComponents(addressComponents = it) }
//        binding?.apply {
//            tietAddress.setText(locationAddress?.address)
//            tietState.setText(locationAddress?.state)
//            tietCity.setText(locationAddress?.city)
//            tietZipCode.setText(locationAddress?.zipCode)
//            tietAddress.error = null
//            tietState.error = null
//            tietCity.error = null
//            tietZipCode.error = null
//        }
//        logger.debugEvent("Place Result", place.address ?: "")
//    }


    /**
     * Initialize recycle view
     */


    private fun startUploadService() {
        val mediaFileRepository: MediaFileRepository? =
            MediaFileRepository.getInstance(this)
        //mediaFileRepository!!.deleteAll()
        for (filePath in mediaFile) {
            val tempFileId = Date().time
            val uploadMedia = Intent(this, UploadPostMediaService::class.java)
            uploadMedia.putExtra(UploadPostMediaService.KEY_FILE_URI, filePath)
            uploadMedia.putExtra(UploadPostMediaService.KEY_USER_ID, userId)
            uploadMedia.putExtra(UploadPostMediaService.KEY_FILE_ID, tempFileId.toString())
            mediaFileRepository?.insertFile(
                MediaFileEntity(
                    fileId = tempFileId,
                    filePath = filePath,
                    userId = userId!!,
                    status = IConstants.PENDING
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(uploadMedia)
            } else {
                startService(uploadMedia)
            }
        }


    }

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        when (viewId) {


        }

    }



    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        TODO("Not yet implemented")
    }

}