package com.app.signme.view.authentication.forgotpassword.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.repository.ForgotPasswordEmailRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.app.signme.viewModel.ForgotPasswordEmailViewModel
import com.google.gson.JsonElement
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class ForgotPasswordEmailViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockForgotPasswordEmailRepositoryTest = mock<ForgotPasswordEmailRepository>()
    private val forgotPasswordObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val showDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    val forgotPasswordEmailResponse = TAListResponse<JsonElement>()
    val emptyResponseList : ArrayList<JsonElement> = arrayListOf()

    private val viewModel by lazy {
        ForgotPasswordEmailViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockForgotPasswordEmailRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockForgotPasswordEmailRepositoryTest,
            showDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyForgotPasswordByEmailError() {
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithEmail("akshaykondekar81194@gmail.com"))
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockForgotPasswordEmailRepositoryTest.getForgotPasswordEmailResponse(ArgumentMatchers.anyString()))
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)

        viewModel.getForgotPasswordWithEmail("aksh@gmial.com")

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
    fun verifyForgotPasswordByEmailSuccess() {
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callForgotPasswordWithEmail("akshaykondekar81194@gmail.com"))
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockForgotPasswordEmailRepositoryTest.getForgotPasswordEmailResponse(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(getForgotPasswordEmailSuccessResponse()))

        viewModel.showDialog.observeForever(showDialogObserver)
        viewModel.forgotPasswordWithEmailLiveData.observeForever(forgotPasswordObserver)
        viewModel.getForgotPasswordWithEmail("akshaykondekar81194@gmail.com")

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(forgotPasswordObserver, Mockito.times(1)).onChanged(
                forgotPasswordEmailResponse
            )
        }
    }

    private fun getForgotPasswordEmailSuccessResponse() : TAListResponse<JsonElement>?{
        forgotPasswordEmailResponse.data = emptyResponseList
        return forgotPasswordEmailResponse
    }
}