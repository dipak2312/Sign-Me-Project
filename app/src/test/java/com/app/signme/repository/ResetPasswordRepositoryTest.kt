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

class ResetPasswordRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockResetPasswordRepositoryTest = mock<ResetPasswordRepository>()
    private val resetPasswordRepository by lazy {
        ResetPasswordRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, resetPasswordRepository.networkService)
    }

    @Test
    fun verifyResetPassword(){
        whenever(mockNetworkService.callResetPassword("Pass@123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM="))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockResetPasswordRepositoryTest.callResetPassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
        resetPasswordRepository.callResetPassword("Pass@123","9545954400","bzV6dFNLJjE2MTE1NTkxNTM=")
            .test().assertComplete()
    }
}