package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.OTPResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ChangePhoneNumberRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

// TODO: 1/29/2021 Need to check mobile number field when put as a parameter.
class ChangePhoneNumberViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockChangePhoneNumberRepositoryTest = mock<ChangePhoneNumberRepository>()
    private val changePhoneNumberObserver = mock<Observer<TAListResponse<OTPResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for otp response
    private val changePhoneNumberResponse = TAListResponse<OTPResponse>()
    private val emptyResponseList : ArrayList<OTPResponse> = arrayListOf()

    private val viewModel by lazy {
        ChangePhoneNumberViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockChangePhoneNumberRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockChangePhoneNumberRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyGetOtpForChangePhoneNumberError(){

        val map = HashMap<String, String>()
        map["type"] = "email"
        map["email"] = "akshaykondekar81194@gmaiil.com"
        map["mobile_number"] = "9545954400"
        map["user_name"] = "erroruser"

        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockChangePhoneNumberRepositoryTest.checkUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        val argumentCaptorShoeDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShoeDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(0)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(0)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
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
        whenever(mockChangePhoneNumberRepositoryTest.checkUniqueUser(ArgumentMatchers.anyMap<String, String>() as HashMap<String, String>))
            .thenReturn(Single.just(getChangePhoneNumberSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.changePhoneNumberLiveData.observeForever(changePhoneNumberObserver)
        viewModel.checkUniqueUser("phone","akshaykondekar81194@gmaiil.com","9545954400","akshaykondekar")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(changePhoneNumberObserver, Mockito.times(1)).onChanged(
                changePhoneNumberResponse
            )
        }
    }

    private fun getChangePhoneNumberSuccessResponse() : TAListResponse<OTPResponse>?{
        changePhoneNumberResponse.data = emptyResponseList
        return changePhoneNumberResponse
    }
}