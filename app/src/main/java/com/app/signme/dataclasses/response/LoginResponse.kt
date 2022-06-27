package com.app.signme.dataclasses.response


import android.telephony.PhoneNumberUtils
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.fromServerDatetoYYYYMMDD
import com.app.signme.dataclasses.SelectedRelationshipType
import com.app.signme.dataclasses.UserImage
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class is used to hold login response
 * @property address String?
 * @property emailVerified String?
 * @property deviceModel String?
 * @property city String?
 * @property userName String?
 * @property latitude String?
 * @property mobileNo String?
 * @property lastName String?
 * @property deviceType String?
 * @property deviceOs String?
 * @property zipCode String?
 * @property accessToken String?
 * @property addedAt String?
 * @property profileImage String?
 * @property updatedAt String?
 * @property userId String?
 * @property dob String?
 * @property deviceToken String?
 * @property stateId String?
 * @property firstName String?
 * @property email String?
 * @property longitude String?
 * @property status String?
 * @property loginType String?
 * @property socialLoginId String?
 * @property purchaseStatus String?
 * @property purchaseReceiptData String?
 * @property notification String?
 * @constructor
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginResponse(

    @JsonProperty("address")
    val address: String? = null,

    @JsonProperty("email_verified")
    val emailVerified: String? = null,

    @JsonProperty("device_model")
    val deviceModel: String? = null,

    @JsonProperty("city")
    val city: String? = null,

    @JsonProperty("user_name")
    val userName: String? = null,

    @JsonProperty("latitude")
    val latitude: String? = null,

    @JsonProperty("mobile_no")
    var mobileNo: String? = null,

    @JsonProperty("last_name")
    val lastName: String? = null,

    @JsonProperty("device_type")
    val deviceType: String? = null,

    @JsonProperty("device_os")
    val deviceOs: String? = null,

    @JsonProperty("zip_code")
    val zipCode: String? = null,

    @JsonProperty("access_token")
    val accessToken: String? = null,

    @JsonProperty("added_at")
    val addedAt: String? = null,

    @JsonProperty("profile_image")
    val profileImage: String? = null,

    @JsonProperty("updated_at")
    val updatedAt: String? = null,

    @JsonProperty("user_id")
    val userId: String? = null,

    @JsonProperty("dob")
    val dob: String? = null,

    @JsonProperty("device_token")
    val deviceToken: String? = null,

    @JsonProperty("state_name")
    val stateName: String? = null,

    @JsonProperty("first_name")
    val firstName: String? = null,

    @JsonProperty("email")
    val email: String? = null,

    @JsonProperty("longitude")
    val longitude: String? = null,

    @JsonProperty("status")
    val status: String? = null,

    @JsonProperty("social_login_type")
    val loginType: String? = "",

    @JsonProperty("social_login_id")
    val socialLoginId: String? = "",

    @JsonProperty("purchase_status")
    val purchaseStatus: String? = "",  //Yes/No

    @JsonProperty("purchase_receipt_data")
    val purchaseReceiptData: String? = "",

    @JsonProperty("push_notify")
    var notification: String? = "",

    @JsonProperty("log_status_updated")
    var logStatusUpdated: String? = "",

    @JsonProperty("terms_conditions_version")
    var terms_conditions_version: String? = "",

    @JsonProperty("privacy_policy_version")
    var privacy_policy_version: String? = "",

   /* var offerDate: String? = "",*/

    @JsonProperty("is_first_login")
    val isFirstLogin: String? = null,


    @JsonProperty("subscription")
    var subscription: ArrayList<PurchasedSubscription>? = null,

   // @field:SerializedName("user_images")
    @JsonProperty("user_images")
   // var userImages: ArrayList<UserImage>? = ArrayList<UserImage>()
    var userImages: ArrayList<UserImage>? = null,
    @JsonProperty("gender")
    val gender: String? = "",
    @JsonProperty("about_me")
    val aboutMe: String? = "",
    @JsonProperty("looking_for_gender")
    val lookingForGender: String? = "",
    @JsonProperty("max_distance")
    val maxDistance: String? = "",
    @JsonProperty("age_lower_limit")
    val ageLowerLimit: String? = "",
    @JsonProperty("age_upper_limit")
    val ageUpperLimit: String? = "",
    @JsonProperty("sign_name")
    val signName: String? = "",
    @JsonProperty("logo_file_name")
    val logoFileName: String? = "",
    @JsonProperty("age")
    val age: String? = "",
    @JsonProperty("looking_for_relation_type")
    var lookingForRelationType: ArrayList<SelectedRelationshipType>? = null)

{
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getFullNameAndAge(): String {
        return "$firstName $age"
    }

    fun getCityAndStateName(): String {
        return "$city,$stateName"
    }

    fun isSocialLogin(): Boolean {
        return !socialLoginId.isNullOrEmpty()
    }

    fun isNotificationOn(): Boolean {
        return notification.equals("Yes", true)
    }

    fun getDOBStr(): String {
        return dob.fromServerDatetoYYYYMMDD()
    }

    fun getFormattedPhoneNumber(): String {
        return if (mobileNo.isNullOrEmpty()) "" else PhoneNumberUtils.formatNumber(mobileNo, "US")
    }

    fun isSubscribed() :Boolean {
        var isSubscribed = false
        if (subscription != null && subscription!!.size > 0 &&subscription!!.filter { it.subscriptionStatus == "1" && (it.productId == IConstants.ANDROID_MONTHLY_SUB_ID||it.productId == IConstants.IOS_MONTHLY_SUB_ID) }.isNotEmpty()) {
            isSubscribed = true
        }
        return isSubscribed
    }
    fun isSubscriptionTaken(): Boolean {
        var isSubscriptionTaken = false
        if (subscription != null && subscription!!.size > 0 && subscription!!.filter { it.subscriptionStatus == "1" }
                .isNotEmpty()) {
            isSubscriptionTaken = true
        }
        return isSubscriptionTaken
    }

    fun isAdsFree(): Boolean {
        var isAddsFree = false
        if (subscription != null && subscription!!.size > 0 && subscription!!.filter { it.subscriptionStatus == "1" && (it.productId == IConstants.ANDROID_AD_FREE_ID ||it.productId == IConstants.IOS_AD_FREE_ID )}.isNotEmpty()) {
            isAddsFree = true
        }
        return isAddsFree
    }

}