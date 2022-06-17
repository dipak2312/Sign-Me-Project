package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ChangePasswordRepository
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

class ChangePasswordViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockChangePasswordRepositoryTest = mock<ChangePasswordRepository>()
    private val changePasswordObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    private val changePasswordResponse = TAListResponse<JsonElement>()
    private val emptyResponseList : ArrayList<JsonElement> = arrayListOf()

    private val viewModel by lazy {
        ChangePasswordViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockChangePasswordRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockChangePasswordRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyChangePasswordError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callChangePassword("Aksh@81194","Aksh@84594"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockChangePasswordRepositoryTest.callChangePassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.callChangePassword("Aksh@81194","Aksh@86694")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(messageStringObserver, Mockito.times(1)).onChanged(
                Resource.error(
                    errorMsg
                )
            )
        }
    }

    @Test
    fun verifyChangePasswordSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callChangePassword("Akshay@81194","Akshay@81194"))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockChangePasswordRepositoryTest.callChangePassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getChangePasswordSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.changePasswordLiveData.observeForever(changePasswordObserver)
        viewModel.callChangePassword("Akshay@81194","Akshay@81194")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(changePasswordObserver, Mockito.times(1)).onChanged(
                changePasswordResponse
            )
        }
    }

    private fun getChangePasswordSuccessResponse() : TAListResponse<JsonElement>?{
        changePasswordResponse.data = emptyResponseList
        return changePasswordResponse
    }
}