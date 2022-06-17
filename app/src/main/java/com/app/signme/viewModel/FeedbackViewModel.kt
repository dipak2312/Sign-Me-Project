package com.app.signme.viewModel


import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.FEEDBACK_EMPTY
import com.app.signme.R
import com.app.signme.api.network.NetworkHelper
import com.app.signme.commonUtils.rx.SchedulerProvider
import com.app.signme.commonUtils.utility.extension.getStringMultipartBodyPart
import com.app.signme.commonUtils.utility.extension.getStringRequestBody
import com.app.signme.commonUtils.utility.getDeviceName
import com.app.signme.commonUtils.utility.getDeviceOSVersion
import com.app.signme.dataclasses.FeedbackImageModel
import com.app.signme.dataclasses.createValidationResult
import com.app.signme.dataclasses.generics.TAListResponse
import com.app.signme.dataclasses.response.FeedbackResponse
import com.app.signme.core.BaseViewModel
import com.app.signme.repository.FeedbackRepository
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MultipartBody
import okhttp3.RequestBody

class FeedbackViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val feedbackRepository: FeedbackRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    companion object {
        //max image which could be attached in feedback
        const val MAX_IMAGE = 5
        const val DEVICE_TYPE_ANDROID = "android"
    }

    val feedbackLiveData = MutableLiveData<TAListResponse<FeedbackResponse>>()
    val checkForInternetConnectionLiveData = MutableLiveData<Boolean>()
    var imageList = ArrayList<FeedbackImageModel>()

    init {
        imageList.add(FeedbackImageModel())
    }

    override fun onCreate() {
        checkForInternetConnection()
    }

    private fun checkForInternetConnection() {
        when {
            checkInternetConnection() -> checkForInternetConnectionLiveData.postValue(true)
            else -> checkForInternetConnectionLiveData.postValue(false)
        }
    }
    fun callReportProblem(message: String) {
        val map = HashMap<String, RequestBody>()
        map["feedback"] = getStringRequestBody(message)
        map["images_count"] = getStringRequestBody(imageList.size.toString())
        map["device_type"] = getStringRequestBody(DEVICE_TYPE_ANDROID)
        map["device_model"] = getStringRequestBody(getDeviceName())
        map["device_os"] = getStringRequestBody(getDeviceOSVersion())

        compositeDisposable.addAll(
            feedbackRepository.sendFeedback(map, getImageFiles())
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { response ->
                        feedbackLiveData.postValue(response)
                    },
                    { error ->
                        statusCodeLiveData.postValue(handleServerError(error))
                    }
                )
        )
    }

    fun isValid(feedback : String):Boolean {
        return when {
              feedback.isEmpty()-> {
                  validationObserver.value = createValidationResult(FEEDBACK_EMPTY, R.id.inputBrief)
                  false
              }
            else-> true
        }
    }
    /**
     * Get MultiPartBody list of selected images
     */
    private fun getImageFiles(): ArrayList<MultipartBody.Part>? {
        val files = ArrayList<MultipartBody.Part>()
        imageList.filter { it.contentUri != null }.forEachIndexed { index, feedbackImageModel ->
            getStringMultipartBodyPart("image_" + (index + 1), feedbackImageModel.imagePath)?.let {
                files.add(it)
            }
        }
        return if (files.isEmpty()) null else files
    }

    /**
     * Return list of selected images by user
     * @param imageModel selected image
     */
    fun getSelectedImage(imageModel: FeedbackImageModel): ArrayList<FeedbackImageModel> {
        // if user added 5th image then remove add image option.
        return imageList.apply {
            if (size == MAX_IMAGE) {
                removeAt(0)
            }
            add(imageModel)
        }
    }

    /**
     * Return list of images after removed selected image
     * @param position position of selected image, which need to be removed.
     */
    fun getImagesAfterRemove(position: Int): ArrayList<FeedbackImageModel> {
        return imageList.apply {
            removeAt(position)
            // if user added 5 option and remove any one, then show add image option.
            if (find { it.contentUri == null } == null) {
                add(0, FeedbackImageModel())
            }
        }
    }
}