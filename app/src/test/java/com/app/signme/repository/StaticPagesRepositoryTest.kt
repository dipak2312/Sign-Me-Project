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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class StaticPagesRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockStaticPagesRepositoryTest = mock<StaticPagesRepository>()
    private val staticPagesRepository by lazy {
        StaticPagesRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, staticPagesRepository.networkService)
    }

    @Test
    fun getStaticPageData(){
        whenever(mockNetworkService.callGetStaticPageData("Pass@123"))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockStaticPagesRepositoryTest.getStaticPageData(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
        staticPagesRepository.getStaticPageData("Pass@123")
            .test().assertComplete()
    }

  @Test
    fun updateTNCPrivacyPolicy(){
        whenever(mockNetworkService.callUpdateTNCPrivacyPolicy("privacy"))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockStaticPagesRepositoryTest.updateTNCPrivacyPolicy(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
        staticPagesRepository.updateTNCPrivacyPolicy("privacy")
            .test().assertComplete()
    }
}