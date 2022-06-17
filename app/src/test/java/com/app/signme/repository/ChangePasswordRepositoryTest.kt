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

class ChangePasswordRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockChangePasswordRepositoryTest = mock<ChangePasswordRepository>()
    private val changePasswordRepository by lazy {
        ChangePasswordRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, changePasswordRepository.networkService)
    }

    @Test
    fun callChangePassword(){
        whenever(mockNetworkService.callChangePassword("Pass@123","9545954400"))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockChangePasswordRepositoryTest.callChangePassword(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
        changePasswordRepository.callChangePassword("Pass@123","9545954400")
            .test().assertComplete()
    }
}