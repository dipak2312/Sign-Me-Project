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
import java.util.HashMap

class ChangePhoneNumberRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockChangePhoneNumberRepositoryTest = mock<ChangePhoneNumberRepository>()
    private val changePhoneNumberRepository by lazy {
        ChangePhoneNumberRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, changePhoneNumberRepository.networkService)
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
        whenever(mockChangePhoneNumberRepositoryTest.checkUniqueUser(map))
            .thenReturn(Single.just(TAListResponse()))
        changePhoneNumberRepository.checkUniqueUser(map)
            .test().assertComplete()
    }


}