package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.LoginWithPhoneNumberSocialRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class LoginWithPhoneNumberSocialViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockLoginWithPhoneNumberSocialRepositoryTest = mock<LoginWithPhoneNumberSocialRepository>()
    private val phoneNumberLoginObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val socialLoginObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for phone login
    private val phoneNumberLoginResponse = TAListResponse<LoginResponse>()
    private val emptyListResponseForPhone : ArrayList<LoginResponse> = arrayListOf()

    //for social login
    private val socialLoginResponse = TAListResponse<LoginResponse>()
    private val emptyListResponseForSocial : ArrayList<LoginResponse> = arrayListOf()

    private val viewModel by lazy {
        LoginWithPhoneNumberSocialViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithPhoneNumberSocialRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithPhoneNumberSocialRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyPhoneNumberLoginError(){

        val map = HashMap<String, String>()
        map["mobile_number"] = "9545954400"
        map["password"] = "Pass@123"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callLoginWithPhone(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockLoginWithPhoneNumberSocialRepositoryTest.callLoginWithPhoneNumber(
            ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
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
    fun verifyPhoneNumberLoginSuccess(){

        val map = HashMap<String, String>()
        map["mobile_number"] = "9545954400"
        map["password"] = "Pass@123"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callLoginWithPhone(map))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockLoginWithPhoneNumberSocialRepositoryTest
            .callLoginWithPhoneNumber(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getPhoneNumberLoginSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.callLoginWithPhoneNumber("9545954400","Pas@123")
        viewModel.phoneNumberLoginMutableLiveData.observeForever(phoneNumberLoginObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(phoneNumberLoginObserver, Mockito.times(1)).onChanged(
                phoneNumberLoginResponse
            )
        }
    }

    private fun getPhoneNumberLoginSuccessResponse() : TAListResponse<LoginResponse>?{
        phoneNumberLoginResponse.data = emptyListResponseForPhone
        return phoneNumberLoginResponse
    }

    @Test
    fun verifySocialLoginError(){

        val map = HashMap<String, String>()
        map["social_login_type"] = "SOCIAL_TYPE"
        map["social_login_id"] = "SOCIAL_ID"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.loginWithSocial(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockLoginWithPhoneNumberSocialRepositoryTest
            .callLoginWithPhoneNumberSocial(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
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
    fun verifySocialLoginSuccess(){

        val map = HashMap<String, String>()
        map["social_login_type"] = "SOCIAL_TYPE"
        map["social_login_id"] = "SOCIAL_ID"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.loginWithSocial(map))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockLoginWithPhoneNumberSocialRepositoryTest
            .callLoginWithPhoneNumberSocial(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getSocialLoginSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.callLoginWithSocial("SOCIAL_TYPE","SOCIAL_ID")
        viewModel.loginSocialMutableLiveData.observeForever(socialLoginObserver)

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(socialLoginObserver, Mockito.times(1)).onChanged(
                socialLoginResponse
            )
        }
    }

    private fun getSocialLoginSuccessResponse() : TAListResponse<LoginResponse>?{
        socialLoginResponse.data = emptyListResponseForSocial
        return socialLoginResponse
    }
}