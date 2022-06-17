package com.app.signme.commonUtils.common

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build

class CommonUtils() {

    companion object {

     /*   const val FRAGMENT_TAG = "WALKTHROUGH_FRAGMENT"
        const val COUNT = 3
        const val DURATION = 500

        fun getDateOrTimeFromSpecificFormat(
            format: String,
            date: Long
        ): String? {
            return try {

                val sdf1 = SimpleDateFormat(format)
                sdf1.timeZone = TimeZone.getDefault()
                sdf1.format(Date(date).time)
            } catch (e: ParseException) {
                Timber.e(e)
                ""
            }
        }*/


        var bitmap: Bitmap? = null

        /**
         * Open Application Settings for Application
         */
        fun openApplicationSettings(mActivity: Activity?) {
            if (mActivity != null) {
                val intent = Intent()
                intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", mActivity.packageName, null)
                intent.data = uri
                mActivity.startActivity(intent)
            }
        }

        /**
         * Open System Notification Settings for Application
         */
        fun openAppNotificationSettings(context: Context) {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    else -> {
                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:" + context.packageName)
                    }
                }
            }
            context.startActivity(intent)
        }

        /**
         * Extention function for load image with glide
         *//*
        fun ImageView.loadCircleImage(url: String?, placeHolder: Int = R.drawable.user_profile) {
            Glide.with(this.context)
                .load(url)
                .apply(RequestOptions.circleCropTransform().placeholder(placeHolder).dontAnimate())
                .into(this)
        }*/
    }



}