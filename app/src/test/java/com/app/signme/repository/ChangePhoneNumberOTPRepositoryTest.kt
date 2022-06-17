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
import java.util.HashMap

class ChangePhoneNumberOTPRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockChangePhoneNumberOTPRepositoryTest = mock<ChangePhoneNumberOTPRepository>()
    private val changePhoneNumberOTPRepository by lazy {
        ChangePhoneNumberOTPRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, changePhoneNumberOTPRepository.networkService)
    }

    @Test
    fun checkUniqueUser(){
        val map = HashMap<String, String>()
        map["type"] = "phone" // phone/email
        map["email"] = "xyz@gmail.com"
        map["mobile_number"] = "8585885855"
        map["user_name"] = "abc"
        whenever(mockNetworkService.callCheckUniqueUser(map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.checkUniqueUser(map))
            .thenReturn(Single.just(TAListResponse()))
        changePhoneNumberOTPRepository.checkUniqueUser(map)
            .test().assertComplete()
    }

@Test
    fun callChangePassword(){
        whenever(mockNetworkService.callChangePhoneNumber("9545954400"))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockChangePhoneNumberOTPRepositoryTest.callChangePhoneNumber(ArgumentMatchers.anyString()))
            .thenReturn(Single.just(TAListResponse()))
    changePhoneNumberOTPRepository.callChangePhoneNumber("9545954400")
            .test().assertComplete()
    }
}