package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ChangePhoneNumberOTPRepository
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

class ChangePhoneNumberOTPViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockChangePhoneNumberOTPRepositoryTest = mock<ChangePhoneNumberOTPRepository>()
    private val changePhoneNumberObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val changePhoneNumberOtpObserver = mock<Observer<TAListResponse<OTPResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for change phone number
    private val changePhoneNumberResponse = TAListResponse<JsonElement>()
    private val emptyResponseList : ArrayList<JsonElement> = arrayListOf()

    //for otp response
    private val changePhoneNumberOtpResponse = TAListResponse<OTPResponse>()
    private val emptyResponseListForOtp : ArrayList<OTPResponse> = arrayListOf()

    private val viewModel by lazy {
        ChangePhoneNumberOTPViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockApplication,
            mockChangePhoneNumberOTPRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockApplication,
            mockChangePhoneNumberOTPRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyChangePhoneNumberError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callChangePhoneNumber("9545847842"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.callChangePhoneNumber(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.callChangePhoneNumber("9545847842")

        val argumentCaptorShoeDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShoeDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyGetOtpForChangePhoneNumberError(){
        val map = HashMap<String, String>()
        map["type"] = "email"
        map["email"] = "akshaykondekar81194@gmaiil.com"
        map["mobile_number"] = "6987458952"
        map["user_name"] = "erroruser"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.checkUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.callResendOTP("email","akshaykondekar81194@gmaiil.com","9574865413","erroruser")

        val argumentCaptorShoeDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShoeDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyChangePhoneNumberSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callChangePhoneNumber("9545954400"))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.callChangePhoneNumber(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getChangePhoneNumberSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.changePhoneNumberLiveData.observeForever(changePhoneNumberObserver)
        viewModel.callChangePhoneNumber("9545954400")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(changePhoneNumberObserver, Mockito.times(1)).onChanged(
                changePhoneNumberResponse
            )
        }
    }

    private fun getChangePhoneNumberSuccessResponse() : TAListResponse<JsonElement>?{
        changePhoneNumberResponse.data = emptyResponseList
        return changePhoneNumberResponse
    }

    @Test
    fun verifyGetOtpForChangePhoneNumberSuccess(){
        val map = HashMap<String, String>()
        map["type"] = "email"
        map["email"] = "akshaykondekar81194@gmaiil.com"
        map["mobile_number"] = "6987458952"
        map["user_name"] = "erroruser"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.just(TAListResponse<OTPResponse>()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.checkUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getOtpForChangePhoneNumberSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.otpPhoneNumberLiveData.observeForever(changePhoneNumberOtpObserver)
        viewModel.callResendOTP("phone","akshaykondekar81194@gmaiil.com","9545954400","akshaykondekar")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(changePhoneNumberOtpObserver, Mockito.times(1)).onChanged(
                changePhoneNumberOtpResponse
            )
        }
    }

    private fun getOtpForChangePhoneNumberSuccessResponse() : TAListResponse<OTPResponse>?{
        changePhoneNumberOtpResponse.data = emptyResponseListForOtp
        return changePhoneNumberOtpResponse
    }
}