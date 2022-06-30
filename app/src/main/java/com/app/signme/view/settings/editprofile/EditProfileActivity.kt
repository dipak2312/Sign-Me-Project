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
import com.app.signme.commonUtils.utility.extension.*
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityEditProfileBinding
import com.app.signme.dataclasses.DeleteUserProfile
import com.app.signme.dataclasses.RelationshipType
import com.app.signme.dataclasses.UserImage
import com.app.signme.scheduler.aws.AwsService
import com.app.signme.viewModel.UserProfileViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
    var userProfile = ArrayList<UserImage>()
    var lookingFor: ArrayList<String>? = null
    var selectedGender = ""
    var selectedLookingFor = ""
    var genders: Array<String>? = null
    var uploadedFiles = ArrayList<String>()
    var deleteUserProfile=ArrayList<DeleteUserProfile>()
    var isImageUploading: Boolean = false

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
        genders = arrayOf(
            "Man",
            "Woman",
            "Transgender",
            getString(R.string.label_non_binary),
            getString(R.string.label_not_respond)
        )
        if (status.equals(getString(R.string.label_edit))) {
            binding!!.tvEditProfile.text = getString(R.string.label_edit_profile_toolbar_text)
            binding!!.btnUpdate.text = getString(R.string.label_save_profile)
            binding!!.linFirstLastName.visibility = View.VISIBLE
            editProfile()
        } else {
            AddGender(genders!!)
            AddLokingFor(genders!!)
            binding!!.tvEditProfile.text = getString(R.string.label_complete_profile_toolbar_text)
            binding!!.btnUpdate.text = getString(R.string.label_get_started_profile)
            binding!!.linFirstLastName.visibility = View.GONE
        }

        initListener()
        addObservers()
        getCityAndState()
        userProfile.add(UserImage("", "", "", ""))
        userProfile.add(UserImage("", "", "", ""))
        userProfile.add(UserImage("", "", "", ""))
        userProfile.add(UserImage("", "", "", ""))
        userProfile.add(UserImage("", "", "", ""))
        userProfile.add(UserImage("", "", "", ""))
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
        AddLokingFor(genders!!)
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

           btnRefreshRelationship.setOnClickListener{
               getRelationshipStatus()
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
                cMin.set(Calendar.YEAR, cMin.get(Calendar.YEAR) - 90)
                val c = Calendar.getInstance()
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 18)
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

                dpd.datePicker.minDate = cMin.timeInMillis
                dpd.datePicker.maxDate = c.timeInMillis
                dpd.show()
            }


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

        if((binding!!.tietFirstName.text.toString().isNullOrEmpty()))
        { showMessage(getString(R.string.alert_enter_first_name), IConstants.SNAKBAR_TYPE_ERROR) }
        else if(!isOnlyAlphabateAndSpace(binding!!.tietFirstName.text.toString()))
        { showMessage(getString(R.string.alert_invalid_first_name_character), IConstants.SNAKBAR_TYPE_ERROR) }
        else if((binding!!.tietLastName.text.toString().isNullOrEmpty()))
        { showMessage(getString(R.string.alert_enter_last_name), IConstants.SNAKBAR_TYPE_ERROR) }
        else if(!isOnlyAlphabateAndSpace(binding!!.tietLastName.text.toString()))
        { showMessage(getString(R.string.alert_invalid_last_name_character), IConstants.SNAKBAR_TYPE_ERROR) }
        else if (selectMyGender.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_select_gender), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (DOB.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_select_dob), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (binding!!.editAboutYou.text.toString().isNullOrEmpty()) {
            showMessage(getString(R.string.alert_add_about_you), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (lookingForGender.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_looking_for_gender), IConstants.SNAKBAR_TYPE_ERROR)
        } else if (lookingForRelation.isNullOrEmpty()) {
            showMessage(getString(R.string.alert_select_relationship), IConstants.SNAKBAR_TYPE_ERROR)
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
                firstName = binding?.tietFirstName!!.getTrimText(),
                lastName = binding?.tietLastName!!.getTrimText(), //imagePath,
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

                    addLookingFor(response.data!!)
                    binding!!.btnRefreshRelationship.visibility=View.GONE
                }
            }
        }


        (application as AppineersApplication).awsFileUploader.observe(this@EditProfileActivity) {
            if (it != null && mAdapter!!.itemCount > it.position) {
                when (it.status) {
                    AwsService.UPLOADING_SUCCESS -> {
                        isImageUploading = false
                        val url = URL(URLDecoder.decode(it.callbackKey, "UTF-8"))
                        Log.i("TAG", "addObservers: location " + url)
                        uploadedFiles.add(url.toString())
                        if(!uploadedFiles.isNullOrEmpty())
                        {
                            sharedPreference.userProfileUrl= uploadedFiles!!.joinToString(separator = ",")
                        }
                        handleResponse(it.position, IConstants.DONE, 0)
                    }
                    AwsService.UPLOADING_FAILED -> {
                        isImageUploading = false
                        Log.i("TAG", "addObservers: exception " + it.message)
                        handleResponse(it.position, IConstants.PENDING, 0)
                    }
                    else -> {
                        isImageUploading = true
                        Log.i("TAG", "addObservers: " + it.status)
                        handleResponse(it.position, IConstants.IN_PROGRESS, it.status)

                    }
                }
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }

        viewModel.statusRelationshipCodeLiveData.observe(this) { serverError ->
            binding!!.btnRefreshRelationship.visibility=View.VISIBLE
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }

        viewModel.validationObserver.observe(this@EditProfileActivity) {
            binding?.root?.focusOnField(it.failedViewId)
            if (it.failType != null)
                focusInvalidInput(it.failType!!)
        }


    }


    private fun handleResponse(index: Int, uploadingStatus: String, percent: Int) {

        var position = index
        if (index >= 6) {
            position = index - 1
        }
        val localItem = mAdapter!!.getItem(position)
        if (index >= 0) {
            mAdapter!!.replaceItem(
                position,
                UserImage(
                    imageId = "",
                    localImageId = "",
                    imageUrl = localItem.imageUrl,
                    imageUri = localItem.imageUri,
                    uploadStatus = if (percent == 100) IConstants.DONE else uploadingStatus,
                    progress = percent
                )
            )
        }
    }

    private fun focusInvalidInput(failType: Int) {
        binding?.apply {
            when (failType) {

            }
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
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
                                openImageFromGalleryCamera()
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
                IConstants.REQUEST_CODE_GALLERY -> {
                    // get path of selected file
                    val fileUri = data?.data
                    captureUri = fileUri
                    val path: String? = captureUri!!.convertIntoPath()
                    startService(
                        AwsService.getStartIntent(
                            this@EditProfileActivity,
                            path!!,
                            1
                        )
                    )
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

            if (imageFile != null) {
                val newUri = FileProvider.getUriForFile(
                    this@EditProfileActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
                )
                captureUri = newUri

                val availableIndex =
                    mAdapter!!.getAllItems().filter { !it.imageUrl.isNullOrEmpty() }.size
                mAdapter!!.replaceItem(
                    availableIndex, UserImage(
                        imageId = "",
                        localImageId = "",
                        imageUrl = imageFile.absolutePath,
                        imageUri = imageFile.absolutePath,
                        uploadStatus = IConstants.IN_PROGRESS
                    )
                )

                startService(
                    AwsService.getStartIntent(
                        this@EditProfileActivity,
                        imageFile.absolutePath,
                        availableIndex
                    )
                )

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


    override fun onDestroy() {
        super.onDestroy()
        deleteAllUploadedFiles()
    }

    private fun deleteAllUploadedFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!uploadedFiles.isNullOrEmpty()) {
                uploadedFiles.forEach { image ->
                    AwsService.deleteFile(image.substringAfter(".com/"))
                }
                uploadedFiles.clear()
                (application as AppineersApplication).awsFileUploader.postValue(
                    null
                )
                sendBroadcast(Intent(AwsService.UPLOAD_CANCELLED_ACTION))
            }
        }
    }


    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

        when (viewId) {

            R.id.ibtnAddImage -> {
                checkPermission()
            }
            R.id.ivRetry -> {
                val image = mAdapter!!.getItem(position)
                startService(
                    AwsService.getStartIntent(
                        this@EditProfileActivity,
                        image.imageUrl!!,
                        position
                    )
                )
            }

            R.id.ibtnRemoveImage -> {

                if (isImageUploading) {
                    "Image uploading in-progress wait a moment".showSnackBar(this@EditProfileActivity)
                    return
                }
                val item = mAdapter!!.getItem(position)
                if (item.imageId!!.isNotEmpty()
                    && item.imageUrl!!.isNotEmpty()
                ) {
                    if (mAdapter!!.getAllItems() != null
                        && mAdapter!!.getAllItems().size > 0
                    ) {
                        //val userImageList: ArrayList<UserImage> = adapterImages.getAllItems()
                        deletedImageId = if (deletedImageId.equals("")) {
                            item.imageId
                        } else {
                            deletedImageId + "," + item.imageId!!
                        }
                    }

                }
                if (item.uploadStatus.equals(IConstants.DONE)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        showProgressDialog(
                            isCheckNetwork = true,
                            isSetTitle = false,
                            title = IConstants.EMPTY_LOADING_MSG
                        )

                        try {
                            var uploadedFileIndex = 0
                            if (position < 0) {
                                uploadedFileIndex = (position - 1)
                            } else if (position == 0) {
                                uploadedFileIndex = position
                            }

                            val uploadedFile = uploadedFiles[uploadedFileIndex]

                            if (AwsService.deleteFile(uploadedFile.substringAfter(".com/"))) {
                                //mAdapter!!.replaceItem(position, UserImage("", "", "", ""))
                                mAdapter!!.removeItem(position)
                                mAdapter!!.addItem(UserImage("","","","",))
                                uploadedFiles.removeAt(uploadedFileIndex)
                                if(!uploadedFiles.isNullOrEmpty())
                                {
                                    sharedPreference.userProfileUrl= uploadedFiles!!.joinToString(separator = ",")
                                }
                                else
                                {
                                    sharedPreference.userProfileUrl=""
                                }

                                "Image Deleted !".showSnackBar(
                                    this@EditProfileActivity,
                                    IConstants.SNAKBAR_TYPE_SUCCESS
                                )
                            } else {
                                "Failed to delete".showSnackBar(this@EditProfileActivity)
                            }

                        } catch (e: Exception) {

                        }
                        hideProgressDialog()
                    }
                    return
                } else {
                   // mAdapter!!.replaceItem(position, UserImage("", "", "", ""))
                    mAdapter!!.removeItem(position)
                    mAdapter!!.addItem(UserImage("","","","",))

                }

            }
        }
    }


    override fun onLoadMore(itemCount: Int, nextPage: Int) {
    }


}