package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ResetPasswordRepository
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

class ResetPasswordViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockResetPasswordRepositoryTest = mock<ResetPasswordRepository>()
    private val resetPasswordObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    val resetPasswordResponse = TAListResponse<JsonElement>()
    val emptyResponseList : ArrayList<JsonElement> = arrayListOf()

    private val viewModel by lazy {
        ResetPasswordViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockResetPasswordRepositoryTest
        )
    }


    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockResetPasswordRepositoryTest,
            showDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyResetPasswordError(){
        whenever(mockNetworkHelper.isNetworkConnected())
            .thenReturn(true)
        whenever(mockNetworkService.callResetPassword("Pass123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM="))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockResetPasswordRepositoryTest.callResetPassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.callResetPassword("Pass123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM=")

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
    fun verifyResetPasswordSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callResetPassword("Pass@123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM="))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockResetPasswordRepositoryTest.callResetPassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getResetPasswordSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.resetPasswordLiveData.observeForever(resetPasswordObserver)
        viewModel.callResetPassword("Pass123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM=")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(resetPasswordObserver, Mockito.times(1)).onChanged(
                resetPasswordResponse
            )
        }
    }

    private fun getResetPasswordSuccessResponse() : TAListResponse<JsonElement>?{
        resetPasswordResponse.data = emptyResponseList
        return resetPasswordResponse
    }
}