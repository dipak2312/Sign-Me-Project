package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.SettingsRepository
import com.app.signme.repository.inappbilling.BillingRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.google.gson.JsonElement
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class SettingsViewModelTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockSettingsRepositoryTest = mock<SettingsRepository>()
    private val mockBillingRepositoryTest = mock<BillingRepository>()
    private val mockLogoutObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val mockDeleteAccountObserver = mock<Observer<TAListResponse<JsonElement>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    //for logout response
    private val logoutResponse = TAListResponse<JsonElement>()
    private val emptyListResponseForLogout : ArrayList<JsonElement> = arrayListOf()

    //for delete account response
    private val deleteAccountResponse = TAListResponse<JsonElement>()
    private val emptyListResponseForDeleteAccount : ArrayList<JsonElement> = arrayListOf()

    private val viewModel by lazy {
        SettingsViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockBillingRepositoryTest,
            mockSettingsRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockSettingsRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyLogoutError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callLogOut())
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockSettingsRepositoryTest.callLogout())
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
    fun verifyLogoutSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callLogOut())
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockSettingsRepositoryTest.callLogout())
            .thenReturn(Single.just(getLogoutSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.logoutLiveData.observeForever(mockLogoutObserver)
        viewModel.callLogout()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockLogoutObserver, Mockito.times(1)).onChanged(
                logoutResponse
            )
        }
    }

    private fun getLogoutSuccessResponse() : TAListResponse<JsonElement>{
        logoutResponse.data = emptyListResponseForLogout
        return logoutResponse
    }

    @Test
    fun verifyDeleteAccountError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callDeleteAccount())
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockSettingsRepositoryTest.callDeleteAccount())
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
    fun verifyDeleteAccountSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callDeleteAccount())
            .thenReturn(Single.just(TAListResponse<JsonElement>()))
        whenever(mockSettingsRepositoryTest.callDeleteAccount())
            .thenReturn(Single.just(getDeleteAccountSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.deleteAccountLiveData.observeForever(mockDeleteAccountObserver)
        viewModel.callDeleteAccount()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(mockDeleteAccountObserver, Mockito.times(1)).onChanged(
                deleteAccountResponse
            )
        }
    }

    private fun getDeleteAccountSuccessResponse() : TAListResponse<JsonElement>{
        deleteAccountResponse.data = emptyListResponseForDeleteAccount
        return deleteAccountResponse
    }

}