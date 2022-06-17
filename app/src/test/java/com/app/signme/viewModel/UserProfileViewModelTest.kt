package com.app.signme.viewModel

import androidx.lifecycle.Observer
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.commonUtils.common.Resource
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.UserProfileRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito


class UserProfileViewModelTest : KotlinBaseMockObjectsClass(){

    private val mockUserProfileRepositoryTest = mock<UserProfileRepository>()
    private val mockUpdateUserObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    private val updateProfileResponse = TAListResponse<LoginResponse>()
    private val emptyResponseList : ArrayList<LoginResponse> = arrayListOf()

    private val viewModel by lazy {
        UserProfileViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockUserProfileRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockUserProfileRepositoryTest,
            showDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyUpdateProfileError(){

        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["first_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["last_name"] = WebServiceUtils.getStringRequestBody("xyz")
        map["dob"] = WebServiceUtils.getStringRequestBody("07/01/1993")
        map["address"] = WebServiceUtils.getStringRequestBody("Magarpata")
        map["city"] = WebServiceUtils.getStringRequestBody("pune")
        map["latitude"] = WebServiceUtils.getStringRequestBody("17.123456")
        map["longitude"] = WebServiceUtils.getStringRequestBody("93.6273636")
        map["state_name"] = WebServiceUtils.getStringRequestBody("Maharashtra")
        map["zipcode"] = WebServiceUtils.getStringRequestBody("441101")
        map["mobile_number"] = WebServiceUtils.getStringRequestBody("8585858585")
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(
            "nvjffgjggjkgjkg=")

        val file: MultipartBody.Part? = null

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callUpdateUserProfile(map, file))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockUserProfileRepositoryTest.updateUserProfile(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyUpdateProfileSuccess(){

        val file: MultipartBody.Part? = null

        val request = LoginResponse(
            firstName = "abc",
            lastName = "xyz",
            userName = "abc_xyz",
            mobileNo = "8585858585",
            dob = "07/11/1993",
            address = "hadapsar",
            stateName = "Maharashtra",
            city = "Pune",
            zipCode = "411011",
            profileImage = "fdjfkdjfg",
            latitude =  "0.0",
            longitude =  "0.0",
        )

        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["first_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["last_name"] = WebServiceUtils.getStringRequestBody("xyz")
        map["dob"] = WebServiceUtils.getStringRequestBody("07/01/1993")
        map["address"] = WebServiceUtils.getStringRequestBody("Magarpata")
        map["city"] = WebServiceUtils.getStringRequestBody("pune")
        map["latitude"] = WebServiceUtils.getStringRequestBody("17.123456")
        map["longitude"] = WebServiceUtils.getStringRequestBody("93.6273636")
        map["state_name"] = WebServiceUtils.getStringRequestBody("Maharashtra")
        map["zipcode"] = WebServiceUtils.getStringRequestBody("441101")
        map["mobile_number"] = WebServiceUtils.getStringRequestBody("8585858585")
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(
            "nvjffgjggjkgjkg=")

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callUpdateUserProfile(map, file))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockUserProfileRepositoryTest.updateUserProfile(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.just(getUpdateProfileSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.updateUserLiveData.observeForever(mockUpdateUserObserver)
        viewModel.updateUserProfile(request)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockUpdateUserObserver, Mockito.times(1)).onChanged(
                updateProfileResponse
            )
        }
    }

    private fun getUpdateProfileSuccessResponse() : TAListResponse<LoginResponse>{
        updateProfileResponse.data = emptyResponseList
        return updateProfileResponse
    }
}