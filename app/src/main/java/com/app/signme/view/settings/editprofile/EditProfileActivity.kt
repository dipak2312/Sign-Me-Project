package com.app.signme.view.settings.editprofile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.app.signme.BuildConfig
import com.app.signme.R
import com.app.signme.adapter.AddUserProfileAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.common.CommonUtils
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.IConstants.Companion.MULTI_IMAGE_REQUEST_CODE
import com.app.signme.commonUtils.utility.dialog.ImageSourceDialog
import com.app.signme.commonUtils.utility.extension.compressImageFile
import com.app.signme.commonUtils.utility.extension.focusOnField
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityEditProfileBinding
import com.app.signme.dataclasses.ProfileImageModel
import com.app.signme.dataclasses.RelationshipType
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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : BaseActivity<UserProfileViewModel>(), RecyclerViewActionListener {

    var mAdapter: AddUserProfileAdapter? = null
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
    var status: String? = ""
    var city: String? = ""
    var state: String? = ""
    var selectMyGender: String? = ""
    var DOB: String? = ""
    var lookingForGender: String? = ""
    var relationship: String? = ""
    var lookingForRelation: ArrayList<String>? = null
    var userProfile = ArrayList<ProfileImageModel>()
    var lookingFor: ArrayList<String>? = null
    var selectedGender = ""
    var selectedLookingFor = ""
    var genders: Array<String>? = null
    var genders1: Array<String>? = null

    companion object {
        const val TAG = "EditProfileActivity"
        fun getStartIntent(mContext: Context, status: String): Intent {
            return Intent(mContext, EditProfileActivity::class.java).apply {
                putExtra(IConstants.STATUS, status)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
        status = intent?.getStringExtra(IConstants.STATUS)
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
        (application as AppineersApplication).isRemoved =
            sharedPreference.userDetail?.profileImage.equals("")
        genders = arrayOf("Man", "Woman", "Transgender", getString(R.string.label_non_binary), getString(R.string.label_not_respond)
        )
        genders1 = arrayOf("Man", "Woman", "Transgender", getString(R.string.label_non_binary), getString(R.string.label_not_respond)
        )
        if (status.equals(getString(R.string.label_edit))) {
            binding!!.tvEditProfile.text = getString(R.string.label_edit_profile_toolbar_text)
            binding!!.btnUpdate.text = getString(R.string.label_save_profile)
            binding!!.linFirstLastName.visibility=View.VISIBLE
            editProfile()
        } else {
            AddGender(genders!!)
            AddLokingFor(genders1!!)
            binding!!.tvEditProfile.text = getString(R.string.label_complete_profile_toolbar_text)
            binding!!.btnUpdate.text = getString(R.string.label_get_started_profile)
            binding!!.linFirstLastName.visibility=View.GONE
        }

        initListener()
        addObservers()
        getCityAndState()
        userProfile.add(ProfileImageModel(null, null))
        userProfile.add(ProfileImageModel(null, null))
        userProfile.add(ProfileImageModel(null, null))
        userProfile.add(ProfileImageModel(null, null))
        userProfile.add(ProfileImageModel(null, null))
        userProfile.add(ProfileImageModel(null, null))
        mAdapter = AddUserProfileAdapter(this, this)
        binding!!.mRecyclerView.adapter = mAdapter
        mAdapter!!.addAllItem(userProfile)

        getRelationshipStatus()

    }

    fun editProfile() {
        selectedGender = sharedPreference.userDetail?.gender.toString()
        if (selectedGender.isNotEmpty()) {
            if (selectedGender.equals(getString(R.string.non_binary))) {
                selectedGender = getString(R.string.label_non_binary)
            } else if (selectedGender.equals(getString(R.string.not_to_respond))) {
                selectedGender = getString(R.string.label_not_respond)
            }
        }

        DOB = sharedPreference.userDetail!!.dob
        binding!!.textDOB.text = DOB
        binding!!.textDOB.setTextColor(Color.parseColor("#ffffff"))
        binding!!.editAboutYou.setText(sharedPreference.userDetail!!.aboutMe)
        binding!!.distanceSlider.value = sharedPreference.userDetail!!.maxDistance!!.toFloat()
        binding!!.textDistanceSlider.text =
            sharedPreference.userDetail!!.maxDistance + getString(R.string.label_km)
        selectedLookingFor = sharedPreference.userDetail?.lookingForGender.toString()

        if (selectedLookingFor.isNotEmpty()) {
            if (selectedLookingFor.equals(getString(R.string.non_binary))) {
                selectedLookingFor = getString(R.string.label_non_binary)
            } else if (selectedLookingFor.equals(getString(R.string.not_to_respond))) {
                selectedLookingFor = getString(R.string.label_not_respond)
            }
        }
        val age = arrayOf(
            sharedPreference.userDetail!!.ageLowerLimit!!.toFloat(),
            sharedPreference.userDetail!!.ageUpperLimit!!.toFloat()
        )
        binding!!.textAgeStart.text = sharedPreference.userDetail!!.ageLowerLimit!!
        binding!!.textAgeEnd.text = sharedPreference.userDetail!!.ageUpperLimit!!
        binding!!.ageRangeSlider.values = age.toMutableList()

        AddGender(genders!!)
        AddLokingFor(genders1!!)
    }

    fun getRelationshipStatus() {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.callGetRelationshipStatus()
            }
        }
    }

    fun getCityAndState() {
        val geocoder = Geocoder(this@EditProfileActivity, Locale.getDefault())

        if (!sharedPreference.latitude.isNullOrEmpty() && !sharedPreference.longitude.isNullOrEmpty()) {
            val addresses: List<Address>
            addresses = geocoder.getFromLocation(
                sharedPreference.latitude!!.toDouble(),
                sharedPreference.longitude!!.toDouble(),
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            val address =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            com.app.signme.commonUtils.utility.extension.sharedPreference.appLoginType
            if (addresses != null && addresses.isNotEmpty()) {
                city = addresses[0].locality
                state = addresses[0].adminArea
                //val country = addresses[0].countryName
                val cityState = city + "," + " " + state
                binding!!.textCityState.setText(cityState)
            }
        }
    }

    fun addLookingFor(lookingFor: ArrayList<RelationshipType>) {
        lookingForRelation = ArrayList<String>()
        for (lookingfor in lookingFor) {
            val checkbox = MaterialCheckBox(this@EditProfileActivity)
            checkbox.text = lookingfor.relationshipStatus
            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    lookingForRelation!!.add(lookingfor.relationshipStatusId!!)
                } else {
                    lookingForRelation!!.remove(lookingfor.relationshipStatusId!!)
                }
            }

            var lookingrelation = sharedPreference.userDetail?.lookingForRelationType
            if (lookingrelation!!.isNotEmpty()) {
                for (relation in lookingrelation) {
                    if (relation.relationshipStatus.equals(lookingfor.relationshipStatus)) {
                        checkbox.isChecked = true
                    }
                }
            }

            binding?.lokingForRelation?.addView(checkbox)
        }
    }

    fun AddGender(genders: Array<String>) {
        binding!!.genderChipGroup.removeAllViews()
        for (mygender in genders) {
            val genderChip = Chip(this@EditProfileActivity)
            genderChip.text = mygender
            genderChip!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectMyGender = genderChip!!.text.toString()
                }
            }

            if (selectedGender.isNotEmpty() && mygender.equals(selectedGender)) {
                genderChip.isChecked = true
            }

            binding!!.genderChipGroup?.addView(genderChip)
        }

    }

    fun AddLokingFor(lookingFor: Array<String>) {
        binding!!.genderLookingForChipGroup.removeAllViews()
        for (gender in lookingFor) {
            val chip = Chip(this@EditProfileActivity)
            chip.text = gender
            chip!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    lookingForGender = chip!!.text.toString()
                }
            }
            if (selectedLookingFor.isNotEmpty() && gender.equals(selectedLookingFor)) {
                chip.isChecked = true
            }

            binding?.genderLookingForChipGroup?.addView(chip)
        }

    }


    private fun initListener() {
        binding?.apply {
            ibtnBack.setOnClickListener {
                setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                finish()

            }

            distanceSlider.addOnChangeListener { slider, value, fromUser ->
                var distance: Int = value.toInt()
                textDistanceSlider.text = distance.toString() + getString(R.string.label_km)
            }

            ageRangeSlider.addOnChangeListener { slider, value, fromUser ->
                var ageStart: Int = slider.values[0].toInt()
                var ageEnd: Int = slider.values[1].toInt()
                textAgeStart.text = ageStart.toString()
                textAgeEnd.text = ageEnd.toString()
            }


            btnUpdate.setOnClickListener {
                setFireBaseAnalyticsData("id-saveprofile", "click_saveprofile", "click_saveprofile")
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Validate and Submit Click")
                MSCGenerator.addAction(
                    GenConstants.ENTITY_USER,
                    GenConstants.ENTITY_APP,
                    "Validate and Submit Profile"
                )
                if (status.equals(getString(R.string.label_edit))) {
                    performEditProfile()
                } else {
                    performEditProfile()
                    // navigateToHomeScreen()
                }
            }

            btnAgeRange.setOnClickListener {

                val cMin = Calendar.getInstance()
                cMin.set(Calendar.YEAR, cMin.get(Calendar.YEAR)-90)
                val c = Calendar.getInstance()
                c.set(Calendar.YEAR, c.get(Calendar.YEAR)-18)
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)


                var dpd = DatePickerDialog(
                    this@EditProfileActivity,
                    DatePickerDialog.OnDateSetListener { view, year1, monthOfYear, dayOfMonth ->

                        // Display Selected date in textbox
                        var selectMonth = monthOfYear.toInt() + 1
                        textDOB.setTextColor(Color.parseColor("#ffffff"))
                        DOB =
                            year1.toString() + "-" + selectMonth.toString() + "-" + dayOfMonth.toString()
                        try {

                            var dateFormat = SimpleDateFormat("yyy-MM-dd");
                            var ageDate = dateFormat.parse(DOB)
                            DOB = dateFormat.format(ageDate)
                            textDOB.setText(DOB)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    year,
                    month,
                    day
                )

                dpd.datePicker.minDate=cMin.timeInMillis
                dpd.datePicker.maxDate = c.timeInMillis
                dpd.show()
            }

//            btnAdd.setOnClickListener {
//                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
//                checkPermission()
//            }

        }

    }

    private fun setProfileImage() {
        if (!sharedPreference.userDetail?.profileImage.equals("")) {
            // loadImage(binding!!.sivUserImage, sharedPreference.userDetail?.profileImage)
        }
    }

    /**
     * Perform edit profile
     */
    private fun performEditProfile() {
        var distance: String? = binding!!.distanceSlider.value.toInt().toString()
        var ageStart: String? = binding!!.ageRangeSlider.values[0].toInt().toString()
        var ageEnd: String? = binding!!.ageRangeSlider.values[1].toInt().toString()

        if (selectMyGender.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_select_gender), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (DOB.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_select_dob), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (binding!!.editAboutYou.text.toString().isNullOrEmpty()) {
            showMessage(getString(R.string.alert_add_about_you), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (lookingForGender.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_looking_for_gender), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (lookingForRelation.isNullOrEmpty()) {
            showMessage(
                getString(R.string.alert_select_relationship),
                IConstants.SNAKBAR_TYPE_ERROR
            )
        } else if (distance.equals("0")) {
            showMessage(getString(R.string.alert_select_distance), IConstants.SNAKBAR_TYPE_ERROR)
        } else {
            if (selectMyGender.equals(getString(R.string.label_non_binary))) {
                selectMyGender = getString(R.string.non_binary)
            }

            if (lookingForGender.equals(getString(R.string.label_not_respond))) {
                lookingForGender = getString(R.string.not_to_respond)
            }

            if (!lookingForRelation.isNullOrEmpty()) {
                relationship = lookingForRelation!!.joinToString(separator = ",")
            }
            val signUpRequest = viewModel.getEditProfileRequest(
                //  userProfileImage = getProfileImageUrl(), //imagePath,
                userProfileImage = imagePath, //imagePath,
                latitude = sharedPreference.latitude ?: "0.0",
                longitude = sharedPreference.longitude ?: "0.0",
                city = city ?: "",
                state = state ?: "",
                dob = DOB!!,
                deleteImageProfile = currentProfilePathToDelete.toString(),   //currentProfilePathToDelete.toString()
                deleteImageIds = deletedImageId!!,
                gender = selectMyGender!!,
                aboutMe = binding!!.editAboutYou.text.toString(),
                lookingForGender = lookingForGender!!,
                lookingForRelation = relationship!!,
                maxDistance = distance!!,
                ageLowerLimt = ageStart!!,
                ageUpperLimt = ageEnd!!
            )

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
                binding!!.btnUpdate.isClickable = false
                binding!!.btnUpdate.isFocusable = false
                sharedPreference.userDetail = it.data!![0]
                Handler(mainLooper).postDelayed(
                    {
                        (application as AppineersApplication).isProfileUpdated.value = true
                        if (status.equals(getString(R.string.label_edit))) {
                            finish()
                        } else {
                            navigateToHomeScreen()
                        }
                    }, IConstants.SNAKE_BAR_SHOW_TIME
                )

            }
        }

        viewModel.getRelationshipStatus.observe(this) { response ->
            lookingFor = ArrayList<String>()
            hideProgressDialog()
            if (response?.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "Change Password Success"
                )
                if (!response.data.isNullOrEmpty()) {
//                   for(response in response.data!!)
//                   {
//                       response.relationshipStatus?.let { lookingFor?.add(it) }
//                   }
                    addLookingFor(response.data!!)
                }
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

//                FIRST_NAME_EMPTY -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_enter_first_name)
//                        )
//                    }
//                    tietFirstName.requestFocus()
//                }
//                FIRST_NAME_INVALID -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_invalid_first_name_character)
//                        )
//                    }
//                    tietFirstName.requestFocus()
//                }
//
//                FIRST_NAME_CHARACTER_INVALID -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_invalid_first_name_character)
//                        )
//                    }
//                    tietFirstName.requestFocus()
//                }
//
//                LAST_NAME_EMPTY -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_enter_last_name)
//                        )
//                    }
//                    tietLastName.requestFocus()
//                }
//                LAST_NAME_INVALID -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_invalid_last_name_character)
//                        )
//                    }
//                    tietLastName.requestFocus()
//                }
//
//                LAST_NAME_CHARACTER_INVALID -> {
//                    runOnUiThread {
//                        showMessage(
//                            getString(R.string.alert_invalid_last_name_character)
//                        )
//                    }
//                    tietLastName.requestFocus()
//                }


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
        Places.initialize(applicationContext, (resources.getString(R.string.google_places_api_key)))
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
                                        //binding!!.sivUserImage.setImageResource(R.drawable.ic_profile_img)
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
//                    binding?.sivUserImage?.loadCircleImage(
//                        imageFile.absolutePath,
//                        R.drawable.user_profile
//                    )

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
        val availableIndex=mAdapter!!.getAllItems().filter { !it.imagePath.isNullOrEmpty() }.size
        mAdapter!!.addItem(availableIndex,ProfileImageModel(imagePath = "http://s3.amazonaws.com/quicklookbucket/quicklook/user_profile/8/IMG_20220617012407_62ac332f13396.png"))
        Log.i(TAG, "onItemClick: "+availableIndex)
        when (viewId) {


        }

    }


    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        TODO("Not yet implemented")
    }

}