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

class LoginWithEmailSocialRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockloginWithEmailSocialRepositoryTest = mock<LoginWithEmailSocialRepository>()
    private val loginWithEmailSocialRepository by lazy {
        LoginWithEmailSocialRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, loginWithEmailSocialRepository.networkService)
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
        whenever(mockloginWithEmailSocialRepositoryTest.callLoginWithEmail(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithEmailSocialRepository.callLoginWithEmail(map = map)
            .test().assertComplete()
    }

    @Test
    fun verifyLoginWithSocial(){
        val map = HashMap<String, String>()
        map["social_login_type"] = "facebook"
        map["social_login_id"] = "xyz@123"
        map["device_type"] = IConstants.DEVICE_TYPE_ANDROID
        map["device_model"] = getDeviceName()
        map["device_os"] = getDeviceOSVersion()
        map["device_token"] = "bzV6dFNLJjE2MTE1NTkxNTM="
        whenever(mockNetworkService.loginWithSocial(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockloginWithEmailSocialRepositoryTest.callLoginWithEmailSocial(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithEmailSocialRepository.callLoginWithEmailSocial(map = map)
            .test().assertComplete()
    }

    @Test
    fun callResendLink(){
        val map = HashMap<String, String>()
        map["email"] = "vaibhavg@theappinners.com"

        whenever(mockNetworkService.callSendVerificationLink(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockloginWithEmailSocialRepositoryTest.callResendLink(map = map))
            .thenReturn(Single.just(TAListResponse()))
        loginWithEmailSocialRepository.callResendLink(map = map)
            .test().assertComplete()
    }
}