package com.app.signme.view.authentication.forgotpassword.phone

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.forgotpasswordwithphone.ResetWithPhone
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.ForgotPasswordPhoneRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class ForgotPasswordPhoneRepositoryTest : KotlinBaseMockObjectsClass(){

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    val mockForgotPasswordWithPhoneRepositoryTest = mock<ForgotPasswordPhoneRepository>()
    val forgotPasswordRepository by lazy {
        ForgotPasswordPhoneRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameters(){
        assertEquals(mockNetworkService, forgotPasswordRepository.networkService)
    }

    @Test
    fun verifyForgotPasswordByPhone(){
        whenever(mockNetworkService.callForgotPasswordWithPhone("9545954400"))
                .thenReturn(Single.just(TAListResponse<ResetWithPhone>()))
        whenever(mockForgotPasswordWithPhoneRepositoryTest.getForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
                .thenReturn(Single.just(TAListResponse<ResetWithPhone>()))
        forgotPasswordRepository.getForgotPasswordPhoneResponse("9545954400").test().assertComplete()
    }

    @After
    fun tearDown() {
    }
}