package com.app.signme.viewModel


import android.app.Application
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.isValidInputText
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.core.BaseViewModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.repository.AddAddressRepository
import com.app.signme.view.authentication.signup.signupconfig.SignUpConfigItem
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.util.*


class AddAddressViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val application: Application,
    private val addressRepository: AddAddressRepository,
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {
        checkForInternetConnection()
    }

    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var signUpLiveData = MutableLiveData<TAListResponse<LoginResponse>>()
   var signUpRequestModel = SignUpRequestModel()
    val updateAddressLiveData = MutableLiveData<TAListResponse<LoginResponse>>()

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
    /**
     * Validate Signup request
     * @param request SignUpRequestModel
     * @return Boolean Return true if all required fields are valid
     */
    fun isValid(request: SignUpRequestModel): Boolean {
        return when {
            //Street Address
//            !isValidInputText(
//                text = request.address, emptyFailType = ADDRESS_EMPTY, viewId = R.id.tietAddress,
//                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
//            ) -> {
//                false
//            }

            //City
//            !isValidInputText(
//               // text = request.city, emptyFailType = CITY_EMPTY, viewId = R.id.tietCity,
//                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
//            ) -> {
//                false
//            }

            //State
//            !isValidInputText(
//                //text = request.state, emptyFailType = STATE_EMPTY, viewId = R.id.tietState,
//                validationObserver = validationObserver, config = SignUpConfigItem("1", "1")
//            ) -> {
//                false
//            }

            //Zip Code
            !isValidInputText(
                text = request.zipCode, emptyFailType = ZIP_CODE_EMPTY,
               // invalidFailType = ZIP_CODE_INVALID, viewId = R.id.tietZipCode,
                minimumLength = application.resources.getInteger(R.integer.zip_code_min_length),
                maximumLength = application.resources.getInteger(R.integer.zip_code_max_length),
                validationObserver = validationObserver, config = SignUpConfigItem("1", "0")
            ) -> {
                false
            }

            else -> true
        }
    }
    /**
     * Update user profile details
     */
    fun callUpdateUserAddress(request: SignUpRequestModel) {
        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody(request.userName)
        map["first_name"] = WebServiceUtils.getStringRequestBody(request.firstName)
        map["last_name"] = WebServiceUtils.getStringRequestBody(request.lastName)
        map["username"] = WebServiceUtils.getStringRequestBody(request.userName)
        map["address"] = WebServiceUtils.getStringRequestBody(request.address)
        map["city"] = WebServiceUtils.getStringRequestBody(request.city)
        map["latitude"] = WebServiceUtils.getStringRequestBody(request.latitude)
        map["longitude"] = WebServiceUtils.getStringRequestBody(request.longitude)
        map["state_name"] = WebServiceUtils.getStringRequestBody(request.state)
        map["zipcode"] = WebServiceUtils.getStringRequestBody(request.zipCode)
        map["mobile_number"] =
            WebServiceUtils.getStringRequestBody(PhoneNumberUtils.normalizeNumber(request.mobileNumber))
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(
            AppineersApplication.sharedPreference.deviceToken
                ?: ""
        )
        compositeDisposable.addAll(
            addressRepository.updateUserProfile(
                map = map,
                file = if (request.profileImage.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                    "user_profile",
                    request.profileImage
                )
            )
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        updateAddressLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }
    /**
     * Perform edit profile request
     */
    fun getEditProfileRequest(
        userProfileImage: String = "", userName: String = "", firstName: String = "",
        lastName: String = "", dob: String = "", phoneNumber: String = "",
        address: String = "", latitude: String = "",
        longitude: String = "", city: String = "",
        state: String = "", zip: String = ""
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
        return request
    }


    /**
     * Save logged in user information in shared preference
     */
    fun saveUserDetails(loginResponse: LoginResponse?) {
        AppineersApplication.sharedPreference.isSkip = false
        AppineersApplication.sharedPreference.userDetail = loginResponse
        AppineersApplication.sharedPreference.isLogin = true
        AppineersApplication.sharedPreference.authToken = loginResponse?.accessToken ?: ""
        AppineersApplication.sharedPreference.isAdRemoved = loginResponse!!.isAdsFree()
        AppineersApplication.sharedPreference.logStatusUpdated =
            loginResponse.logStatusUpdated?.toLowerCase(
            Locale.getDefault()) ?: IConstants.INACTIVE_LOG_STATUS
    }

}