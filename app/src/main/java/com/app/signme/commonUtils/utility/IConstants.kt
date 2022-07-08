package com.app.signme.commonUtils.utility

import android.os.Environment

/**
 * This class contains all the constants value that is used in application
 * All values defined here, it can not be changed in application lifecycle
 */
interface IConstants {
    @Suppress("DEPRECATION")
    companion object {

        const val CITY_SEARCH: Int=10
        const val ADDRESS_SEARCH: Int=11

        const val SNAKBAR_TYPE_ERROR = 1
        const val SNAKBAR_TYPE_SUCCESS = 2
        const val SNAKBAR_TYPE_MESSAGE = 3
        const val REQUEST_STATIC_PAGE: Int = 4
        const val RESULT_PER_PAGE: String="15"
        const val APP_UPDATE_REQUEST_CODE : Int = 5
        const val APP_FORCE_UPDATE_REQUEST_CODE : Int = 6
        const val NOTIFICATION_PERMISSION_REQUEST_CODE : Int = 7
        const val REQUEST_CODE_LOCATION = 108
        const val MEDIA_TYPE_IMAGE = 1
        private val storageDir = Environment.getExternalStorageDirectory().toString() + "/TheAppineers/"
        val IMAGES_FOLDER_PATH = "$storageDir/Images"
        const val IMAGE_DIRECTORY_NAME = ".theappineers"
        const val FOLDER_NAME = "TheAppineers"
        const val STATUS="status"
        const val EDIT="edit"
        const val ADD="add"
        const val USERID="userid"
        const val SP_NOTIFICATION_CHANNEL_DEFAULT = "sp_notification_channel_default"
        const val PARAM_NOTIFICATION_TYPE = "type"
        const val OTHERS:String="others"
        const val BUNDLE_TAB_ID: String = "bundle_tab_id"

        const val SPLASH_TIME = 2000L//splash time 3second
        const val SNAKE_BAR_SHOW_TIME = 3000L
        const val SNAKE_BAR_SHOW_TIME_INT = 3000
        val INCLUDE_HEADER = true

        const val REQUEST_SEARCH_PLACE = 115
        const val REQUEST_CODE_GALLERY = 500
        const val REQUEST_CODE_CAMERA = 600
        const val REQUEST_CODE_CROP_RESULT = 106
        const val CAMERA_REQUEST_CODE = 103
        const val REQUEST_CODE_CAPTURE_IMAGE = 107

        const val LOCATION_REQUEST_CODE = 108
        const val CHECK_UPGRADE_DOWNGRADE = "check_upgrade_downgrade"


        val STATIC_PAGE_ABOUT_US: String = "aboutus"
        val STATIC_PAGE_TERMS_CONDITION: String = "termsconditions"
        val STATIC_PAGE_PRIVACY_POLICY: String = "privacypolicy"
        val STATIC_PAGE_EULA_POLICY: String = "eula"



        const val BUNDLE_CROP_URI = "bundle_crop_uri"
        const val BUNDLE_IS_CROP_CANCEL = "is_crop_cancel"
        const val BUNDLE_IMG_URI = "bundle_img_uri"
        const val BUNDLE_CROP_SQUARE = "bundle_crop_square"
        const val BUNDLE_CROP_MIN_SIZE = "bundle_crop_min_size"
        const val EVENT_PURCHASED = "Click Purchased"
        const val SNAKE_BAR_PROFILE_SHOW_TIME = 2000L



        const val SOCIAL_TYPE_FB = "facebook"
        const val SOCIAL_TYPE_GOOGLE = "google"
        const val SOCIAL_TYPE_APPLE = "apple"
        const val DEVICE_TYPE_ANDROID = "android"
        const val REQUEST_CODE_FACEBOOK_LOGIN = 1010
        const val REQUEST_CODE_GOOGLE_LOGIN = 1011
        const val REQUEST_CODE_APPLE_LOGIN = 1012
        const val LOGIN_TYPE_EMAIL = "email"
        const val LOGIN_TYPE_EMAIL_SOCIAL = "email_social"
        const val LOGIN_TYPE_PHONE = "phone"
       // const val LOGIN_TYPE_PHONE_SOCIAL = "phone_social"
        const val COUNT_DOWN_TIMER = 30000L // 30 second
        const val ENABLED = "Enabled"
        const val DISABLED = "Disabled"
        var AUTOCOMPLETE_REQUEST_CODE = 1
        val MULTI_IMAGE_REQUEST_CODE = 303
        const val EVENT_CLICK = "Click Event"
        const val BUNDLE_SELECTED_IMAGE = "bundle_selected_image"


        val ANDROID_AD_FREE_ID: String="com.appineers.whitelabel.goadfree"
        val ANDROID_MONTHLY_SUB_ID: String="com.appineers.whitelabel.one_month_subscription"

        val IOS_AD_FREE_ID: String="com.appineers.whitelabel.goadfree"
        val IOS_MONTHLY_SUB_ID: String="com.appineers.whitelabel.monthly"

        const val EMPTY_LOADING_MSG = ""
        const val IS_ADDRESS_MANDATORY = "Yes"
        const val ACTIVE_LOG_STATUS = "active"
        const val INACTIVE_LOG_STATUS = "inactive"

        const val DONE: String = "done"
        const val PENDING: String = "pending"
        const val IN_PROGRESS: String = "inProgress"
        const val IMAGE_CATEGORY: String = "user_images"
        const val PHOTO: String = "Photo"
        const val VIDEO: String = "Video"
        const val AUDIO: String = "Audio"
    }
}
