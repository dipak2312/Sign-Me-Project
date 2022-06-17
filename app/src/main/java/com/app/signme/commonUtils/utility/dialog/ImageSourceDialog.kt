package com.app.signme.commonUtils.utility.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.databinding.CvDialogSelectImageProviderBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hb.logger.Logger

class ImageSourceDialog(
    private val activity: Activity,
    val onPhotoRemove:() -> Unit
) : DialogFragment() {

    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        val binding = DataBindingUtil.inflate<CvDialogSelectImageProviderBinding>(
            inflater,
            R.layout.cv_dialog_select_image_provider,
            container,
            false
        )

        binding.mbtnCamera.setOnClickListener {
            logger.dumpCustomEvent(IConstants.EVENT_CLICK,"User click on camera to get image")
            ImagePicker.with(activity)
                .cameraOnly()
                .cropSquare()//Crop image(Optional), Check Customization for more option
                .compress(1024) //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .start(IConstants.MULTI_IMAGE_REQUEST_CODE)
            this.dismiss()
        }

        binding.mbtnGallery.setOnClickListener {
            logger.dumpCustomEvent(IConstants.EVENT_CLICK,"User click on gallery to get image")
            ImagePicker.with(activity)
                .galleryOnly()
                .cropSquare()//Crop image(Optional), Check Customization for more option
                .compress(1024) //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .galleryMimeTypes( //Exclude gif images
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                ).start(IConstants.MULTI_IMAGE_REQUEST_CODE)
            this.dismiss()
        }

        binding.mbtnRemoveProfile.setOnClickListener {
            logger.dumpCustomEvent(IConstants.EVENT_CLICK,"User want to remove profile image")
            onPhotoRemove()
            this.dismiss()
        }

        binding.mbtnCancel.setOnClickListener {
            this.dismiss()
        }
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }
}