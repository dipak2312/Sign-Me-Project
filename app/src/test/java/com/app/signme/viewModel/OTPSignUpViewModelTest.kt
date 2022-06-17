package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.commonUtils.utility.extension.toFieldRequestBodyMap
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.request.SignUpRequestModel
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.OTPSignUpRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.squareup.okhttp.RequestBody
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import kotlin.collections.HashMap

class OTPSignUpViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockOtpSignUpRepositoryTest = mock<OTPSignUpRepository>()
    private val mockOtpObserver = mock<Observer<TAListResponse<OTPResponse>>>()
    private val mockSignUpObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for otp response
    private val otpRequestResponse = TAListResponse<OTPResponse>()
    private val emptyListResponseForOtp : ArrayList<OTPResponse> = arrayListOf()

    //for otp response
    private val signUpResponse = TAListResponse<LoginResponse>()
    private val emptyListResponseForSignUp : ArrayList<LoginResponse> = arrayListOf()

    private val signUpRequestModel = SignUpRequestModel()

    private val viewModel by lazy {
        OTPSignUpViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockOtpSignUpRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockOtpSignUpRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyOtpRequestError(){

        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = ""
        map["mobile_number"] = "9545967814"
        map["user_name"] = ""

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockOtpSignUpRepositoryTest
            .callCheckUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyOtpRequestSuccess(){

        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = ""
        map["mobile_number"] = "9545954400"
        map["user_name"] = ""

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.just(TAListResponse<OTPResponse>()))
        whenever(mockOtpSignUpRepositoryTest.callCheckUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getRequestOtpSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.otpLiveData.observeForever(mockOtpObserver)
        viewModel.callResendOtp("9545954400")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
        }
    }

    private fun getRequestOtpSuccessResponse() : TAListResponse<OTPResponse>{
        otpRequestResponse.data = emptyListResponseForOtp
        return otpRequestResponse
    }

    @Test
    fun verifySignUpPhoneError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockOtpSignUpRepositoryTest
            .callSignUpWithPhone(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifySignUpPhoneSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockOtpSignUpRepositoryTest
            .callSignUpWithPhone(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>))
            .thenReturn(Single.just(getSignUpPhoneNumberSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.signUpLiveData.observeForever(mockSignUpObserver)
        viewModel.callSignUpWithPhone()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockSignUpObserver, Mockito.times(1)).onChanged(
                signUpResponse
            )
        }
    }


    @Test
    fun verifySignUpSocialError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockOtpSignUpRepositoryTest
            .callSignUpWithSocial(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifySignUpSocialSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockOtpSignUpRepositoryTest
            .callSignUpWithSocial(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>))
            .thenReturn(Single.just(getSignUpPhoneNumberSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.signUpLiveData.observeForever(mockSignUpObserver)
        viewModel.callSignUpWithSocial()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockSignUpObserver, Mockito.times(1)).onChanged(
                signUpResponse
            )
        }
    }

    private fun getSignUpPhoneNumberSuccessResponse() : TAListResponse<LoginResponse> {
        signUpResponse.data = emptyListResponseForSignUp
        return signUpResponse
    }
}