package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.LoginWithEmailRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.google.gson.JsonElement
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class LoginWithEmailViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockLoginWithEmailRepositoryTest = mock<LoginWithEmailRepository>()
    private val mockLoginEmailObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val mockResendLinkObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for email login
    private val emailLoginResponse = TAListResponse<LoginResponse>()
    private val emptyListResponseForEmail : ArrayList<LoginResponse> = arrayListOf()

    //for resend link
    private val resendLinkResponse = TAListResponse<JsonElement>()
    private val emptyListResponseForLink : ArrayList<JsonElement> = arrayListOf()

    private val viewModel by lazy {
        LoginWithEmailViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithEmailRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithEmailRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyEmailLoginError(){
        val map = HashMap<String, String>()
        map["email"] = "akshayko81194@gmal"
        map["password"] = "Pass123@"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.loginWithEmail(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockLoginWithEmailRepositoryTest.callLoginWithEmail(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
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
    fun verifyResendLinkError(){

        val map = HashMap<String, String>()
        map["email"] = "akshayko81194@gmal"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSendVerificationLink(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockLoginWithEmailRepositoryTest.callResendLink(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
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
    fun verifyEmailLoginSuccess(){

        val map = HashMap<String, String>()
        map["email"] = "akshayko81194@gmal"
        map["password"] = "Pass123@"
        map["device_type"] = "ANDROID"
        map["device_model"] = "LENOVO MOTO C PLUS"
        map["device_os"] = "Lollipop 5.1"
        map["device_token"] = "DEVICE TOKEN"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.loginWithEmail(map))
            .thenReturn(Single.just(TAListResponse<LoginResponse>()))
        whenever(mockLoginWithEmailRepositoryTest.callLoginWithEmail(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getEmailLoginSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.loginEmailMutableLiveData.observeForever(mockLoginEmailObserver)
        viewModel.callLoginWithEmail("email","password")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockLoginEmailObserver, Mockito.times(1)).onChanged(
                emailLoginResponse
            )
        }
    }

    private fun getEmailLoginSuccessResponse() : TAListResponse<LoginResponse>?{
        emailLoginResponse.data = emptyListResponseForEmail
        return emailLoginResponse
    }


    @Test
    fun verifyResendLinkSuccess(){

        val map = HashMap<String, String>()
        map["email"] = "akshayko81194@gmal"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callSendVerificationLink(map))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockLoginWithEmailRepositoryTest.callResendLink(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getResendLinkSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.resendLinkMutableLiveData.observeForever(mockResendLinkObserver)
        viewModel.callResendLink("akshaykondekar81194@gmail.com")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockResendLinkObserver, Mockito.times(1)).onChanged(
                resendLinkResponse
            )
        }
    }

    private fun getResendLinkSuccessResponse() : TAListResponse<JsonElement>?{
        resendLinkResponse.data = emptyListResponseForLink
        return resendLinkResponse
    }
}