package com.app.signme.viewModel

import android.telephony.PhoneNumberUtils
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
import com.app.signme.repository.SignUpRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.google.gson.JsonElement
import com.squareup.okhttp.RequestBody
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class SignUpViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockSignUpRepositoryTest = mock<SignUpRepository>()
    private val mockOtpObserver = mock<Observer<TAListResponse<OTPResponse>>>()
    private val mockSignUpObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val mockSignUpSocialObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()
    private val signUpRequestModel = SignUpRequestModel()

    private val otpRequestResponse = TAListResponse<OTPResponse>()
    private val emptyResponseListForOTP : ArrayList<OTPResponse> = arrayListOf()

    private val signUpResponse = TAListResponse<JsonElement>()
    private val emptyResponseListForSignUp : ArrayList<JsonElement> = arrayListOf()

    private val socialSignUpResponse = TAListResponse<LoginResponse>()
    private val emptyResponseListForSocialSignUp : ArrayList<LoginResponse> = arrayListOf()

    private val viewModel by lazy {
        SignUpViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockSignUpRepositoryTest,
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockSignUpRepositoryTest,
            showDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyRequestOtpError(){
        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = "akshaykondekar81194@gmail.com"
        map["mobile_number"] = PhoneNumberUtils.normalizeNumber("9545954400")
        map["user_name"] = "akshaykondekar"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockSignUpRepositoryTest
            .callCheckUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyRequestOtpSuccess(){
        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = "akshaykondekar81194@gmail.com"
        map["mobile_number"] = PhoneNumberUtils.normalizeNumber("9545954400")
        map["user_name"] = "akshaykondekar"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.just(TAListResponse<OTPResponse>()))
        whenever(mockSignUpRepositoryTest
            .callCheckUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getOtpRequestSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.otpLiveData.observeForever(mockOtpObserver)
        viewModel.callCheckUnique("phone","akshaykondekar81194@gmail.com","9545954400","akshaykondekar")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockOtpObserver, Mockito.times(1)).onChanged(
                otpRequestResponse
            )
        }
    }

    private fun getOtpRequestSuccessResponse() : TAListResponse<OTPResponse>{
        otpRequestResponse.data = emptyResponseListForOTP
        return otpRequestResponse
    }

    @Test
    fun verifySignUpWithSocialError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockSignUpRepositoryTest
            .callSignUpWithSocial(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifySignUpWithSocialSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockSignUpRepositoryTest
            .callSignUpWithSocial(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.just(getSignUpSocialSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.signUpLiveDataSocial.observeForever(mockSignUpSocialObserver)
        viewModel.callSignUpWithSocial()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockSignUpSocialObserver, Mockito.times(1)).onChanged(
                socialSignUpResponse
            )
        }
    }

    private fun getSignUpSocialSuccessResponse() : TAListResponse<LoginResponse>{
        socialSignUpResponse.data = emptyResponseListForSocialSignUp
        return socialSignUpResponse
    }


    @Test
    fun verifySignUpWithEmailError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithEmail(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockSignUpRepositoryTest
            .callSignUpWithSocial(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifySignUpWithEmailSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSignUpWithEmail(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockSignUpRepositoryTest
            .callSignUpWithEmail(ArgumentMatchers.anyMap<String, RequestBody>() as HashMap<String, okhttp3.RequestBody>, ArgumentMatchers.any()))
            .thenReturn(Single.just(getSignUpEmailSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.signUpLiveData.observeForever(mockSignUpObserver)
        viewModel.callSignUpWithEmail()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockSignUpObserver, Mockito.times(1)).onChanged(
                signUpResponse
            )
        }
    }

    private fun getSignUpEmailSuccessResponse() : TAListResponse<JsonElement>{
        signUpResponse.data = emptyResponseListForSignUp
        return signUpResponse
    }
}
