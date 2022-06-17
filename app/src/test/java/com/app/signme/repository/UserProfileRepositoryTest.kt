import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.api.network.WebServiceUtils
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.LoginResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.repository.UserProfileRepository
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import okhttp3.RequestBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class UserProfileRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockUserProfileRepositoryTest = mock<UserProfileRepository>()
    private val UserProfileRepository by lazy {
        UserProfileRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, UserProfileRepository.networkService)
    }

    @Test
    fun callLoginWithPhoneNumber(){
        val request = LoginResponse(
            firstName = "abc",
            lastName = "xyz",
            userName = "abc_xyz",
            mobileNo = "8585858585",
            dob = "07/11/1993",
            address = "hadapsar",
            stateName = "Maharashtra",
            city = "Pune",
            zipCode = "411011",
            profileImage = "fdjfkdjfg",
            latitude =  "0.0",
            longitude =  "0.0",
        )
        val map = HashMap<String, RequestBody>()
        map["user_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["first_name"] = WebServiceUtils.getStringRequestBody("abc")
        map["last_name"] = WebServiceUtils.getStringRequestBody("xyz")
        map["dob"] = WebServiceUtils.getStringRequestBody("07/01/1993")
        map["address"] = WebServiceUtils.getStringRequestBody("Magarpata")
        map["city"] = WebServiceUtils.getStringRequestBody("pune")
        map["latitude"] = WebServiceUtils.getStringRequestBody("17.123456")
        map["longitude"] = WebServiceUtils.getStringRequestBody("93.6273636")
        map["state_name"] = WebServiceUtils.getStringRequestBody("Maharashtra")
        map["zipcode"] = WebServiceUtils.getStringRequestBody("441101")
        map["mobile_number"] = WebServiceUtils.getStringRequestBody("8585858585")
        map["device_type"] = WebServiceUtils.getStringRequestBody(IConstants.DEVICE_TYPE_ANDROID)
        map["device_model"] = WebServiceUtils.getStringRequestBody(getDeviceName())
        map["device_os"] = WebServiceUtils.getStringRequestBody(getDeviceOSVersion())
        map["device_token"] = WebServiceUtils.getStringRequestBody(
            "nvjffgjggjkgjkg=")
        whenever(mockNetworkService.callUpdateUserProfile(map,  file = if ( request.profileImage!!.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
            "user_profile",
            request.profileImage!!
        )))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockUserProfileRepositoryTest.updateUserProfile(map,  file = if ( request.profileImage!!.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
            "user_profile",
            request.profileImage!!
        )))
            .thenReturn(Single.just(TAListResponse()))
        UserProfileRepository.updateUserProfile(
            map,
            file = if ( request.profileImage!!.isEmpty()) null else WebServiceUtils.getStringMultipartBodyPart(
                "user_profile",
                request.profileImage!!
            )
        )
    }




}