package com.app.signme.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.app.signme.commonUtils.common.Resource
import com.app.signme.dataclasses.VersionConfigResponse
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.mainactivitytest.errorMsg
import com.app.signme.mainactivitytest.getErrorResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.HomeRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class HomeViewModelTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockHomeRepositoryTest = mock<HomeRepository>()
    private val configParamsObserver = mock<Observer<TAListResponse<VersionConfigResponse>>>()
    private val showingDialogObserver = mock<Observer<Boolean>>()
    private val messageStringObserver = mock<Observer<Resource<String>>>()

    private val configResponse = TAListResponse<VersionConfigResponse>()
    private val emptyResponseList : ArrayList<VersionConfigResponse> = arrayListOf()

    private val viewModel by lazy {
        HomeViewModel(
            testSchedulerProvider,
            mockCompositeDisposable,
            mockNetworkHelper,
            mockHomeRepositoryTest
        )
    }

    @Before
    fun setUp() {
        Mockito.reset(
            mockCompositeDisposable,
            mockNetworkHelper,
            mockHomeRepositoryTest,
            showingDialogObserver,
            messageStringObserver
        )
    }

    @Test
    fun verifyConfigError(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callConfigParameters())
            .thenReturn(Single.error(getErrorResponse()))
        whenever(mockHomeRepositoryTest.callConfigParameters())
            .thenReturn(Single.error(getErrorResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.messageString.observeForever(messageStringObserver)
        viewModel.callGetConfigParameters()

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
    fun verifyConfigSuccess(){
        whenever(mockNetworkHelper.isNetworkConnected()).thenReturn(true)
        whenever(mockNetworkService.callConfigParameters())
            .thenReturn(Single.just(TAListResponse<VersionConfigResponse>()))
        whenever(mockHomeRepositoryTest.callConfigParameters())
            .thenReturn(Single.just(getConfigSuccessResponse()))

        viewModel.showDialog.observeForever(showingDialogObserver)
        viewModel.configParamsPhoneLiveData.observeForever(configParamsObserver)
        viewModel.callGetConfigParameters()

        val argumentCaptorShowDialog = ArgumentCaptor.forClass(Boolean::class.java)
        argumentCaptorShowDialog.run {
            Mockito.verify(showingDialogObserver, Mockito.times(1)).onChanged(capture())
            Mockito.verify(configParamsObserver, Mockito.times(1)).onChanged(
                configResponse
            )
        }
    }

    private fun getConfigSuccessResponse() : TAListResponse<VersionConfigResponse>?{
        configResponse.data = emptyResponseList
        return configResponse
    }
}

