package com.app.signme.view.authentication.forgotpassword.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ForgotPasswordEmailRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class ForgotPasswordEmailRepositoryTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    val mockForgotPasswordWithEmailRepositoryTest = mock<ForgotPasswordEmailRepository>()
    val forgotPasswordRepository by lazy {
        ForgotPasswordEmailRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, forgotPasswordRepository.networkService)
    }

    @Test
    fun verifyForgotPasswordByEmail(){
        whenever(mockNetworkService.callForgotPasswordWithEmail("akshaykondekar81194@gmail.com"))
                .thenReturn(Single.just(TAListResponse()))
        whenever(mockForgotPasswordWithEmailRepositoryTest.getForgotPasswordEmailResponse(ArgumentMatchers.anyString()))
                .thenReturn(Single.just(TAListResponse()))
        forgotPasswordRepository.getForgotPasswordEmailResponse("akshaykondekar81194@gmail.com").test().assertComplete()
    }

    @After
    fun tearDown() {
    }
}