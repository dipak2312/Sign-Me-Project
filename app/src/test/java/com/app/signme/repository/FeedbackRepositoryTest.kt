package com.app.signme.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.signme.commonUtils.utility.extension.getStringMultipartBodyPart
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.dataclasses.FeedbackImageModel
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.objectclasses.KotlinBaseMockObjectsClass
import com.app.signme.utils.mock
import com.app.signme.utils.whenever
import com.app.signme.viewModel.FeedbackViewModel
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.HashMap

class FeedbackRepositoryTest : KotlinBaseMockObjectsClass() {

    @Rule
    @JvmField
    val run = InstantTaskExecutorRule()

    private val mockFeedbackRepositoryTest = mock<FeedbackRepository>()
    private val feedbackRepository by lazy {
        FeedbackRepository(mockNetworkService)
    }

    @Before
    fun setUp() {
        Mockito.reset(mockNetworkService, mockApplication)
    }

    @Test
    fun verifyConstructorParameter(){
        assertEquals(mockNetworkService, feedbackRepository.networkService)
    }

    @Test
    fun sendFeedback(){
        val map = HashMap<String, RequestBody>()
        map["feedback"] = getStringRequestBody("App feedback")
        map["images_count"] = getStringRequestBody("1")
        map["device_type"] = getStringRequestBody(FeedbackViewModel.DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())

        whenever(mockNetworkService.callSendFeedback(map, getImageFiles()   ))
            .thenReturn(Single.just(TAListResponse()))
        whenever(mockFeedbackRepositoryTest.sendFeedback(map, getImageFiles()))
            .thenReturn(Single.just(TAListResponse()))
        feedbackRepository.sendFeedback(map, getImageFiles())
            .test().assertComplete()
    }
    private fun getImageFiles(): ArrayList<MultipartBody.Part>? {
        var imageList = ArrayList<FeedbackImageModel>()

        val files = ArrayList<MultipartBody.Part>()
        imageList.filter { it.contentUri != null }.forEachIndexed { index, feedbackImageModel ->
            getStringMultipartBodyPart("image_" + (index + 1), feedbackImageModel.imagePath)?.let {
                files.add(it)
            }
        }
        return if (files.isEmpty()) null else files
    }


}