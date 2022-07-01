package com.app.signme.viewModel

import android.app.Application
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.isValidInputText
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.RelationshipType
import com.app.signme.dataclasses.UserMediaList
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.UserProfileRepository
import com.app.signme.view.authentication.signup.signupconfig.SignUpConfigItem
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody

class UserProfileViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val application: Application,
    private val userProfileRepository: UserProfileRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    val updateUserLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    val getUserLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
    val getRelationshipStatus=MutableLiveData<TAListResponse<RelationshipType>>()

    /**
     * Update user profile details
     */

    fun updateUserProfile(request: SignUpRequestModel) {
        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody(request.userName)
        map["first_name"] = WebServiceUtils.getStringRequestBody(request.firstName)
        map["last_name"] = WebServiceUtils.getStringRequestBody(request.lastName)
        map["dob"] = WebServiceUtils.getStringRequestBody(request.dob)
        map["address"] = WebServiceUtils.getStringRequestBody(request.address)
        map["city"] = WebServiceUtils.getStringRequestBody(request.city)
        map["latitude"] = WebServiceUtils.getStringRequestBody(request.latitude)
        map["longitude"] = WebServiceUtils.getStringRequestBody(request.longitude)
        map["state_name"] = WebServiceUtils.getStringRequestBody(request.state)
        map["zipcode"] = WebServiceUtils.getStringRequestBody(request.zipCode)
        map["delete_user_profile"] = WebServiceUtils.getStringRequestBody(request.deleteUserProfile)
        map["delete_image_ids"] = WebServiceUtils.getStringRequestBody(request.deleteImageIds)
        map["mobile_number"] =
            WebServiceUtils.getStringRequestBody(PhoneNumberUtils.normalizeNumber(request.mobileNumber))
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(sharedPreference.deviceToken ?: "")
        map["gender"]=WebServiceUtils.getStringRequestBody(request.gender)
        map["about_me"]=WebServiceUtils.getStringRequestBody(request.aboutMe)
        map["looking_for_gender"]=WebServiceUtils.getStringRequestBody(request.lookingForGender)
        map["looking_for_relation"]=WebServiceUtils.getStringRequestBody(request.lookingForRelation)
        map["max_distance"]=WebServiceUtils.getStringRequestBody(request.maxDistance)
        map["age_lower_limit"]=WebServiceUtils.getStringRequestBody(request.ageLowerLimt)
        map["age_upper_limit"]=WebServiceUtils.getStringRequestBody(request.ageUpperLimt)
        map["user_media"]=WebServiceUtils.getStringRequestBody(request.userMedia.toString())

        compositeDisposable.addAll(
            userProfileRepository.updateUserProfile(
                map
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        updateUserLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
            )
    }

    fun callGetRelationshipStatus() {
        compositeDisposable.addAll(
            userProfileRepository.callGetRelationshipStatus()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        // showDialog.postValue(false)
                        getRelationshipStatus.postValue(response)
                    },
                    { error ->
                        statusRelationshipCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun callGetProfile() {
        compositeDisposable.addAll(
            userProfileRepository.callGetProfile()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        // showDialog.postValue(false)
                        getUserLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }

  /*  fun getDateFromPicker(year: Int, month: Int, dayOfMonth: Int): String {
        val calender = Calendar.getInstance()
        calender.set(Calendar.YEAR, year)
        calender.set(Calendar.MONTH, month)
        calender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calender.time.toMMDDYYYStr()
    }
*/
    /**
     * Validate sign up inputs
     */
    fun isValid(request: SignUpRequestModel): Boolean {
        return when {

            //First Name
            !isValidInputText(
                text = request.firstName, emptyFailType = FIRST_NAME_EMPTY,
                invalidFailType = FIRST_NAME_INVALID, viewId = R.id.tietFirstName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }


            //Last Name
            !isValidInputText(
                text = request.lastName, emptyFailType = LAST_NAME_EMPTY,
                invalidFailType = LAST_NAME_INVALID, viewId = R.id.tietLastName,
                minimumLength = application.resources.getInteger(R.integer.first_name_min_length),
                maximumLength = application.resources.getInteger(R.integer.first_name_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }
            else -> true
        }
    }


    /**
     * Perform edit profile request
     */

    fun getEditProfileRequest(
        userProfileImage: String = "",
        userName: String = "",
        firstName: String = "",
        lastName: String = "",
        dob: String = "",
        phoneNumber: String = "",
        address: String = "",
        latitude: String = "",
        longitude: String = "",
        city: String = "",
        state: String = "",
        zip: String = "",
        deleteImageProfile: String = "",
        deleteImageIds: String = "",
        gender:String="",
        aboutMe:String="",
        lookingForGender:String="",
        lookingForRelation:String="",
        maxDistance:String="",
        ageLowerLimt:String="",
        ageUpperLimt:String="",
        userMediaList: String?

    ): SignUpRequestModel {
        val request = SignUpRequestModel()
        request.profileImage = userProfileImage
        request.userName = userName
        request.firstName = firstName
        request.lastName = lastName
        request.mobileNumber = phoneNumber
        request.dob = dob
        request.address = address
        request.latitude = latitude
        request.longitude = longitude
        request.city = city
        request.state = state
        request.zipCode = zip
        request.deleteUserProfile = deleteImageProfile
        request.deleteImageIds = deleteImageIds
        request.gender=gender
        request.aboutMe=aboutMe
        request.lookingForGender=lookingForGender
        request.lookingForRelation=lookingForRelation
        request.maxDistance=maxDistance
        request.ageLowerLimt=ageLowerLimt
        request.ageUpperLimt=ageUpperLimt
        request.userMedia=userMediaList
        return request
    }

}