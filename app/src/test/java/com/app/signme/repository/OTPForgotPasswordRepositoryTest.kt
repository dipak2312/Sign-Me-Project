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

class OTPForgotPasswordRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockOTPForgotPasswordRepositoryTest = mock<OTPForgotPasswordRepository>()
    private val oTPForgotPasswordRepository by lazy {
        OTPForgotPasswordRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, oTPForgotPasswordRepository.networkService)
    }

    @Test
    fun OTPForgotPasswordRepository(){
        whenever(mockNetworkService.callForgotPasswordWithPhone("9545954400"))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockOTPForgotPasswordRepositoryTest.getOTPForgotPasswordPhoneResponse(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
        oTPForgotPasswordRepository.getOTPForgotPasswordPhoneResponse("9545954400")
            .test().assertComplete()
    }
}