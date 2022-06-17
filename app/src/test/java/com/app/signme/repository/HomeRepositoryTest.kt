package com.app.signme.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class HomeRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockHomeRepositoryTest = mock<HomeRepository>()
    private val homeRepository by lazy {
        HomeRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, homeRepository.networkService)
    }

    @Test
    fun callLogout(){
        whenever(mockNetworkService.callConfigParameters())
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockHomeRepositoryTest.callConfigParameters())
            .thenReturn(Single.just(TAListResponse()))
        homeRepository.callConfigParameters()
            .test().assertComplete()
    }

}