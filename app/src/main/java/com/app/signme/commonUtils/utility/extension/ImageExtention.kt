@file:Suppress("DEPRECATION")

package com.app.signme.commonUtils.utility.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.helper.LOGApp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun getCameraUri(context: Context): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            File(getOutputMediaFileUri(IConstants.MEDIA_TYPE_IMAGE).path!!)
        )
    } else {
        getOutputMediaFileUri(IConstants.MEDIA_TYPE_IMAGE)
    }
}

fun getOutputMediaFileUri(context: Context, file: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
    } else {
        //return getOutputMediaFileUri();
        Uri.fromFile(file)
    }
}

fun getOutputMediaFileUriNew(context: Context, file: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
    } else {
        Uri.fromFile(file)
    }
}

//image take from camera or gallery
fun getOutputMediaFileUri(type: Int): Uri {
    return Uri.fromFile(getOutputMediaFile(type))
}

fun getOutputMediaFileUri(): Uri {
    return Uri.fromFile(getOutputMediaFile())
}

fun getOutputMediaFile(fileName: String = ""): File? {
    val imageFileName =
        if (fileName.isEmpty()) "TheAppineers" + "_" + System.currentTimeMillis() else fileName

    var image: File? = null
    val storageDirFile = File(IConstants.IMAGES_FOLDER_PATH)
    if (!storageDirFile.exists()) {
        storageDirFile.mkdirs()
    }
    if (storageDirFile.exists()) {
        try {
            image = File.createTempFile(imageFileName, ".jpeg", storageDirFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return image
    }
    return null
}

//image take from camera or gallery
fun getOutputMediaFile(type: Int): File? {
    // External sdcard location
    val mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        IConstants.IMAGE_DIRECTORY_NAME
    )
    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
            LOGApp.d(
                IConstants.IMAGE_DIRECTORY_NAME,
                "Oops! Failed create " + IConstants.IMAGE_DIRECTORY_NAME + " directory"
            )
            return null
        }
    }
    // Create a media file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val mediaFile: File
    if (type == IConstants.MEDIA_TYPE_IMAGE) {
        mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
    } else {
        return null
    }
    return mediaFile
}

fun getAppMediaFolderPath(): String {
    val mediaFolderPath = (Environment.getExternalStorageDirectory().absolutePath
            + "/" + IConstants.FOLDER_NAME)
    createMediaFolderIfNotExist(mediaFolderPath)
    return mediaFolderPath
}

/**
 * Create media folder if not exist.
 *
 * @param mediaPath the media path
 */
fun createMediaFolderIfNotExist(mediaPath: String) {
    val mediaFile = File(mediaPath)
    if (!mediaFile.exists()) {
        mediaFile.mkdirs()
    }
}


/**
 * Extention function for load image with glide
 */
fun ImageView.loadImage(url: String?, placeHolder: Int = R.drawable.user_profile) {
    Glide.with(this.context)
        .load(url)
        .apply(applyPlaceholder(placeHolder))
        .into(this)

}

/**
 * Extention function for load image with glide
 */
fun ShapeableImageView.circularLoadImage(url: String?, placeHolder: Int = R.drawable.user_profile) {
    Glide.with(this.context)
        .load(url)
        .apply(applyPlaceholder(placeHolder))
        .into(this)
}

/**
 * Extention function for load image with glide
 */
fun ImageView.loadCircleImage(url: String?, placeHolder: Int = R.drawable.user_profile) {
    Glide.with(this.context)
        .load(url)
        .apply(RequestOptions.circleCropTransform().placeholder(placeHolder).dontAnimate())
        .into(this)
}

@SuppressLint("CheckResult")
fun applyPlaceholder(placeholderImage: Int): RequestOptions {
    val requestOptions = RequestOptions()
    requestOptions.placeholder(placeholderImage)
    requestOptions.error(placeholderImage)
    requestOptions.dontAnimate()
    return requestOptions
}

//@BindingAdapter("bind:imageUrl", "placeHolder", requireAll = false)
@BindingAdapter(value = ["imageUrl", "default"], requireAll = false)
fun loadGlideImage(view: ImageView, imageUrl: String?, default: Int?) {
    if (default == null) view.loadImage(imageUrl) else view.loadImage(imageUrl, default)

}

@BindingAdapter("bind:imageUrl")
fun loadImage(view: ImageView, imageUrl: String?) {
    view.loadImage(imageUrl)
}

@BindingAdapter("app:imageUrl")
fun bindImageUrl(imageView: ImageView, imageUrl: String?) {
    val circularProgressDrawable = CircularProgressDrawable(imageView.context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()
    imageUrl.let {
        Glide.with(imageView.context).load(it).apply(RequestOptions.noAnimation().placeholder(/*circularProgressDrawable*/
            R.drawable.login_logo))
            .into(imageView)
    }
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

/**
 * This method is used to resize image
 *
 * @param context
 * @param dstWidth
 * @param dstHeight
 * @param scalingLogic
 * @param rotationNeeded
 * @param currentPhotoPath
 * @return scaledBitmap
 */
fun getResizeImage(
    context: Context,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic,
    rotationNeeded: Boolean,
    currentPhotoPath: String,
    IMAGE_CAPTURE_URI: Uri?
): Bitmap? {
    var rotate = 0
    try {
        val imageFile = File(currentPhotoPath)

        val exif = ExifInterface(imageFile.absolutePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }


    try {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        bmOptions.inJustDecodeBounds = false
        return if (bmOptions.outWidth < dstWidth && bmOptions.outHeight < dstHeight) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
            getRotatedBitmap(
                setSelectedImage(
                    bitmap,
                    context,
                    currentPhotoPath,
                    IMAGE_CAPTURE_URI!!
                ), rotate
            )
        } else {
            var unscaledBitmap = decodeResource(currentPhotoPath, dstWidth, dstHeight, scalingLogic)
            val matrix = Matrix()
            if (rotationNeeded) {
                matrix.setRotate(
                    getCameraPhotoOrientation(
                        context,
                        Uri.fromFile(File(currentPhotoPath)),
                        currentPhotoPath
                    ).toFloat()
                )
                unscaledBitmap = Bitmap.createBitmap(
                    unscaledBitmap,
                    0,
                    0,
                    unscaledBitmap.width,
                    unscaledBitmap.getHeight(),
                    matrix,
                    false
                )
            }
            val scaledBitmap = createScaledBitmap(unscaledBitmap, dstWidth, dstHeight, scalingLogic)
            unscaledBitmap.recycle()
            getRotatedBitmap(scaledBitmap, rotate)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

}

fun getRotatedBitmap(bitmap: Bitmap, rotate: Int): Bitmap {
    return if (rotate == 0) {
        bitmap
    } else {
        val mat = Matrix()
        mat.postRotate(rotate.toFloat())
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)
    }
}


/**
 * This method is used get orientation of camera photo
 *
 * @param context
 * @param imageUri  This parameter is Uri type
 * @param imagePath This parameter is String type
 * @return rotate
 */
private fun getCameraPhotoOrientation(context: Context, imageUri: Uri?, imagePath: String): Int {
    var rotate = 0
    try {
        try {
            if (imageUri != null) context.contentResolver.notifyChange(imageUri, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val imageFile = File(imagePath)
        val exif = ExifInterface(imageFile.absolutePath)
        when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            ExifInterface.ORIENTATION_NORMAL -> rotate = 0
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return rotate
}

fun setSelectedImage(
    orignalBitmap: Bitmap,
    context: Context,
    imagePath: String,
    IMAGE_CAPTURE_URI: Uri
): Bitmap {
    return try {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (manufacturer.equals("samsung", ignoreCase = true) || model.equals(
                "samsung",
                ignoreCase = true
            )
        ) {
            rotateBitmap(context, orignalBitmap, imagePath, IMAGE_CAPTURE_URI)
        } else {
            orignalBitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        orignalBitmap
    }

}


fun rotateBitmap(context: Context, bit: Bitmap, imagePath: String, IMAGE_CAPTURE_URI: Uri): Bitmap {

    val rotation = getCameraPhotoOrientation(context, IMAGE_CAPTURE_URI, imagePath)
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat())
    return Bitmap.createBitmap(bit, 0, 0, bit.width, bit.height, matrix, true)
}

/**
 * Utility function for decoding an image resource. The decoded bitmap will
 * be optimized for further scaling to the requested destination dimensions
 * and scaling logic.
 *
 * @param dstWidth
 * Width of destination area
 * @param dstHeight
 * Height of destination area
 * @param scalingLogic
 * Logic to use to avoid image stretching
 * @return Decoded bitmap
 */
fun decodeResource(
    filePath: String,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic
): Bitmap {

    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, bmOptions)

    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = calculateSampleSize(
        bmOptions.outWidth,
        bmOptions.outHeight,
        dstWidth,
        dstHeight,
        scalingLogic
    )

    return BitmapFactory.decodeFile(filePath, bmOptions)
}

/**
 * Utility function for creating a scaled version of an existing bitmap
 *
 * @param unscaledBitmap
 * Bitmap to scale
 * @param dstWidth
 * Wanted width of destination bitmap
 * @param dstHeight
 * Wanted height of destination bitmap
 * @param scalingLogic
 * Logic to use to avoid image stretching
 * @return New scaled bitmap object
 */
fun createScaledBitmap(
    unscaledBitmap: Bitmap,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic
): Bitmap {
    val srcRect = calculateSrcRect(
        unscaledBitmap.width,
        unscaledBitmap.height,
        dstWidth,
        dstHeight,
        scalingLogic
    )
    val dstRect = calculateDstRect(
        unscaledBitmap.width,
        unscaledBitmap.height,
        dstWidth,
        dstHeight,
        scalingLogic
    )
    val scaledBitmap =
        Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(scaledBitmap)
    canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))

    return scaledBitmap
}

/**
 * ScalingLogic defines how scaling should be carried out if source and
 * destination image has different aspect ratio.
 *
 * CROP: Scales the image the minimum amount while making sure that at least
 * one of the two dimensions fit inside the requested destination area.
 * Parts of the source image will be cropped to realize this.
 *
 * FIT: Scales the image the minimum amount while making sure both
 * dimensions fit inside the requested destination area. The resulting
 * destination dimensions might be adjusted to a smaller size than
 * requested.
 */
enum class ScalingLogic {
    CROP, FIT
}

/**
 * Calculate optimal down-sampling factor given the dimensions of a source
 * image, the dimensions of a destination area and a scaling logic.
 *
 * @param srcWidth
 * Width of source image
 * @param srcHeight
 * Height of source image
 * @param dstWidth
 * Width of destination area
 * @param dstHeight
 * Height of destination area
 * @param scalingLogic
 * Logic to use to avoid image stretching
 * @return Optimal down scaling sample size for decoding
 */
fun calculateSampleSize(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic
): Int {
    if (scalingLogic == ScalingLogic.FIT) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            srcWidth / dstWidth
        } else {
            srcHeight / dstHeight
        }
    } else {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            srcHeight / dstHeight
        } else {
            srcWidth / dstWidth
        }
    }
}

/**
 * Calculates source rectangle for scaling bitmap
 *
 * @param srcWidth
 * Width of source image
 * @param srcHeight
 * Height of source image
 * @param dstWidth
 * Width of destination area
 * @param dstHeight
 * Height of destination area
 * @param scalingLogic
 * Logic to use to avoid image stretching
 * @return Optimal source rectangle
 */
fun calculateSrcRect(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic
): Rect {
    if (scalingLogic == ScalingLogic.CROP) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        return if (srcAspect > dstAspect) {
            val srcRectWidth = (srcHeight * dstAspect).toInt()
            val srcRectLeft = (srcWidth - srcRectWidth) / 2
            Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight)
        } else {
            val srcRectHeight = (srcWidth / dstAspect).toInt()
            val scrRectTop = (srcHeight - srcRectHeight) / 2
            Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight)
        }
    } else {
        return Rect(0, 0, srcWidth, srcHeight)
    }
}

/**
 * Calculates destination rectangle for scaling bitmap
 *
 * @param srcWidth
 * Width of source image
 * @param srcHeight
 * Height of source image
 * @param dstWidth
 * Width of destination area
 * @param dstHeight
 * Height of destination area
 * @param scalingLogic
 * Logic to use to avoid image stretching
 * @return Optimal destination rectangle
 */
fun calculateDstRect(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    scalingLogic: ScalingLogic
): Rect {
    return if (scalingLogic == ScalingLogic.FIT) {
        val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
        val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()

        if (srcAspect > dstAspect) {
            Rect(0, 0, dstWidth, (dstWidth / srcAspect).toInt())
        } else {
            Rect(0, 0, (dstHeight * srcAspect).toInt(), dstHeight)
        }
    } else {
        Rect(0, 0, dstWidth, dstHeight)
    }
}

/**
 * to convert bitmap to file.
 */
fun bitmapToFile(bitmap: Bitmap?, dstPath: String): File? {
    return try {
        val file = File(dstPath)
        if (file.exists()) file.delete()
        file.createNewFile()
        val fOut: FileOutputStream
        fOut = FileOutputStream(file)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, fOut)
        fOut.flush()
        fOut.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}

fun getScaledImagePath(
    mContext: Context,
    imageUploadMaxWidth: Int,
    imageUploadMaxHeight: Int,
    path: String
): String {
    val previewBitmap = getResizeImage(
        mContext,
        imageUploadMaxWidth,
        imageUploadMaxHeight,
        ScalingLogic.FIT,
        false,
        path,
        null
    )
    val file = bitmapToFile(
        previewBitmap,
        mContext.cacheDir.path + "/" + +System.currentTimeMillis() + ".jpg"
    )
    return if (file != null) file.path else path
}

/**
 * Save bitmap in gallery and notify to app
 * @param image Bitmap
 * @param imageFileName String
 * @return String Return saved image path
 */
fun saveBitmapImage(image: Bitmap, imageFileName: String, context: Context): String {
    var savedImagePath = ""

    val storageDir = File("${context.cacheDir}/${IConstants.FOLDER_NAME}")
    var success = true
    if (!storageDir.exists()) {
        success = storageDir.mkdirs()
    }
    if (success) {
        val imageFile = File(storageDir, imageFileName)
        savedImagePath = imageFile.absolutePath
        try {
            val fOut = FileOutputStream(imageFile)
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Add the image to the system gallery
        galleryAddPic(savedImagePath, context)
    }
    return savedImagePath
}

fun galleryAddPic(imagePath: String, context: Context) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val f = File(imagePath)
    val contentUri = Uri.fromFile(f)
    mediaScanIntent.data = contentUri
    context.sendBroadcast(mediaScanIntent)
}

/**
 * This method will return resized image of same height and width
 */
fun String.getResizedImageUrl(size: String): String {
    return this.getResizedImageUrl(size, size)
}

/**
 * This method will return resized image of different height and width
 */
fun String.getResizedImageUrl(height: String, width: String): String {
    if (this.isBlank()) {
        return this
    } else {
        val uri = Uri.parse(this)
        val params = uri.queryParameterNames
        val newUri = uri.buildUpon().clearQuery()
        for (param in params) {
            when (param) {
                "height" -> newUri.appendQueryParameter(
                    param,
                    height
                )
                "width" -> newUri.appendQueryParameter(
                    param,
                    width
                )
                else -> newUri.appendQueryParameter(param, uri.getQueryParameter(param))
            }
        }
        return newUri.build().toString()
    }
}