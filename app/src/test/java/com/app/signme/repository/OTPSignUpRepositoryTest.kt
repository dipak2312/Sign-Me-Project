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

class OTPSignUpRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val oTPSignUpRepositoryTest = mock<OTPSignUpRepository>()
    private val oTPSignUpRepository by lazy {
        OTPSignUpRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, oTPSignUpRepository.networkService)
    }
    // Todo ramandeep
  /*  @Test
    fun callSignUpWithPhone(){
        val signUpRequestModel = SignUpRequestModel()

       signUpRequestModel.profileImage = ""
       signUpRequestModel.firstName = "Abc"
       signUpRequestModel.lastName = "xyz"
        signUpRequestModel.userName = "abc_xyz"
       signUpRequestModel.email = "abc@gmail.com"
       signUpRequestModel.mobileNumber ="8585858585"
      signUpRequestModel.address = "pune"
        signUpRequestModel.latitude = "17.52585685"
        signUpRequestModel.longitude ="93.5656565"
       signUpRequestModel.city = "pune"
        signUpRequestModel.state = "Maharashtra"
        signUpRequestModel.zipCode = "441101"
       signUpRequestModel.dob = "07/11/1993"
        signUpRequestModel.password = "123456"
        signUpRequestModel.confirmPassword ="123456"
       signUpRequestModel.socialType = "gmail"
        signUpRequestModel.socialId = "g@1234"
        signUpRequestModel.tnc = true
        signUpRequestModel.deviceType = IConstants.DEVICE_TYPE_ANDROID
       signUpRequestModel.deviceModel = getDeviceName()
       signUpRequestModel.deviceOs = "1.4"
        signUpRequestModel.deviceToken = "hfjksdhfjhgjfhgj="



        whenever(mockNetworkService.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse()))
        whenever(oTPSignUpRepositoryTest.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse()))
        oTPSignUpRepository.callSignUpWithPhone(signUpRequestModel.toFieldRequestBodyMap())
            .test().assertComplete()
    }

    @Test
    fun callSignUpWithSocial(){
        val signUpRequestModel = SignUpRequestModel()

        signUpRequestModel.profileImage = ""
        signUpRequestModel.firstName = "Abc"
        signUpRequestModel.lastName = "xyz"
        signUpRequestModel.userName = "abc_xyz"
        signUpRequestModel.email = "abc@gmail.com"
        signUpRequestModel.mobileNumber ="8585858585"
        signUpRequestModel.address = "pune"
        signUpRequestModel.latitude = "17.52585685"
        signUpRequestModel.longitude ="93.5656565"
        signUpRequestModel.city = "pune"
        signUpRequestModel.state = "Maharashtra"
        signUpRequestModel.zipCode = "441101"
        signUpRequestModel.dob = "07/11/1993"
        signUpRequestModel.password = "123456"
        signUpRequestModel.confirmPassword ="123456"
        signUpRequestModel.socialType = "gmail"
        signUpRequestModel.socialId = "g@1234"
        signUpRequestModel.tnc = true
        signUpRequestModel.deviceType = IConstants.DEVICE_TYPE_ANDROID
        signUpRequestModel.deviceModel = getDeviceName()
        signUpRequestModel.deviceOs = "1.4"
        signUpRequestModel.deviceToken = "hfjksdhfjhgjfhgj="

        whenever(mockNetworkService.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse()))
        whenever(oTPSignUpRepositoryTest.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap()))
            .thenReturn(Single.just(TAListResponse()))
        oTPSignUpRepository.callSignUpWithSocial(signUpRequestModel.toFieldRequestBodyMap())
            .test().assertComplete()
    }
*/
 @Test
    fun callCheckUniqueUser(){
        val map = HashMap<String, String>()
     map["type"] = "phone" // phone/email
     map["email"] = ""
     map["mobile_number"] = "8054545454"
     map["user_name"] = ""
        whenever(mockNetworkService.callCheckUniqueUser(map = map))
            .thenReturn(Single.just(TAListResponse()))
        whenever(oTPSignUpRepositoryTest.callCheckUniqueUser(map = map))
            .thenReturn(Single.just(TAListResponse()))
        oTPSignUpRepository.callCheckUniqueUser(map = map)
            .test().assertComplete()
    }

    private fun signUp() {

    }
}