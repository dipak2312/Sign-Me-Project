package com.app.signme.repository



import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
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

class LoginWithEmailRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockloginWithEmailRepositoryTest = mock<LoginWithEmailRepository>()
    private val loginWithEmailRepository by lazy {
        LoginWithEmailRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, loginWithEmailRepository.networkService)
    }

    @Test
    fun verifyLoginWithEmail(){
        val map = HashMap<String, String>()
        map["email"] = "vaibhavg@theappinners.com"
        map["password"] = "Test@123"
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = "bzV6dFNLJjE2MTE1NTkxNTM="
        whenever(mockNetworkService.loginWithEmail(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockloginWithEmailRepositoryTest.callLoginWithEmail(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithEmailRepository.callLoginWithEmail(map = map)
            .test().assertComplete()
    }

    @Test
    fun callResendLink(){
        val map = HashMap<String, String>()
        map["email"] = "vaibhavg@theappinners.com"

        whenever(mockNetworkService.callSendVerificationLink(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockloginWithEmailRepositoryTest.callResendLink(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithEmailRepository.callResendLink(map = map)
            .test().assertComplete()
    }
}