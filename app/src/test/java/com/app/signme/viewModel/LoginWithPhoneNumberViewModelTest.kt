package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.LoginWithPhoneNumberRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class LoginWithPhoneNumberViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockLoginWithPhoneNumberRepositoryTest = mock<LoginWithPhoneNumberRepository>()
    private val phoneNumberLoginObserver = mock<Observer<TAListResponse<LoginResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for phone number login
    private val phoneNumberLoginResponse = TAListResponse<LoginResponse>()
    private val emptyListResponseForPhone : ArrayList<LoginResponse> = arrayListOf()

    private val viewModel by lazy {
        LoginWithPhoneNumberViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithPhoneNumberRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockLoginWithPhoneNumberRepositoryTest,
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
        whenever(mockLoginWithPhoneNumberRepositoryTest.callLoginWithPhoneNumber(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
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
        whenever(mockLoginWithPhoneNumberRepositoryTest
            .callLoginWithPhoneNumber(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getPhoneLoginSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.phoneNumberLoginMutableLiveData.observeForever(phoneNumberLoginObserver)
        viewModel.callLoginWithPhoneNumber("9545954400","Pass@123")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(phoneNumberLoginObserver, Mockito.times(1)).onChanged(
                phoneNumberLoginResponse
            )
        }
    }

    private fun getPhoneLoginSuccessResponse() : TAListResponse<LoginResponse>?{
        phoneNumberLoginResponse.data = emptyListResponseForPhone
        return phoneNumberLoginResponse
    }
}