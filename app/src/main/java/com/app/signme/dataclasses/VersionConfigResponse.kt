package com.app.signme.dataclasses

import android.content.Context
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getAppVersion
import com.app.signme.dataclasses.response.PurchasedSubscription
import com.app.signme.dataclasses.response.MandatoryFlags
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionConfigResponse(

    @JsonProperty("iphone_version_number")
    val iphoneVersionNumber: String? = null,

    @JsonProperty("android_version_number")
    val androidVersionNumber: String? = null,

    @JsonProperty("version_update_mandatory_android")
    val versionUpdateMandatoryAndroid: String? = null,

    @JsonProperty("version_update_mandatory_ios")
    val versionUpdateMandatoryIos: String? = null,

    @JsonProperty("version_update_check")
    val versionUpdateCheck: String? = null,

    @JsonProperty("version_check_message")
    val versionCheckMessage: String? = null,

    @JsonProperty("terms_conditions_version")
    val termsConditionsVersion: String? = null,

    @JsonProperty("privacy_policy_version")
    val privacyPolicyVersion: String? = null,

    @JsonProperty("terms_conditions_version_application")
    val termsConditionsVersionApplication: String? = null,

    @JsonProperty("privacy_policy_version_application")
    val privacyPolicyVersionApplication: String? = null,

    @JsonProperty("log_status_updated")
    val logStatusUpdated: String = "",

    @JsonProperty("android_app_id")
    var androidAppId: String? = null,

    @JsonProperty("android_banner_id")
    var androidBannerId: String? = null,

    @JsonProperty("android_interstitial_id")
    var androidInterstitialId: String? = null,

    @JsonProperty("android_native_id")
    var androidNativeId: String? = null,

    @JsonProperty("android_rewarded_id")
    var androidRewardedId: String? = null,

    @JsonProperty("project_debug_level")
    var projectDebugLevel: String? = null,

    @JsonProperty("android_mopub_banner_id")
    val androidMoPubBannerId: String? = null,

    @JsonProperty("android_mopub_interstitial_id")
    val androidMopubInterstitialId: String? = null,

    @JsonProperty("is_first_login")
    val isFirstLogin: String? = null,

    @JsonProperty("address")
    val address: String? = null,

    @JsonProperty("subscription")
    val subscription: ArrayList<PurchasedSubscription>? = null,

    @JsonProperty("mandatory_array")
    val mandatoryArray: ArrayList<MandatoryFlags>? = null,
    @JsonProperty("is_updated")
    val isUpdated: String? = null


) {
    fun shouldShowVersionDialog(context: Context) =
        (((androidVersionNumber?.compareTo(getAppVersion(context, true))
            ?: 0) > 0) && versionUpdateCheck.equals("1"))

    fun isUpdateMandatory() = versionUpdateMandatoryAndroid.equals("1")

    /*  fun shouldShowTNCUpdated() = termsConditionsUpdated.equals("1")

      fun shouldShowPrivacyPolicyUpdated() = privacyPolicyUpdated.equals("1")
  */
    fun isAppInDevelopment() = projectDebugLevel.equals("development", true)


    fun isAdsFree(): Boolean {
        var isAddsFree = false
        if (subscription != null && subscription.size > 0 && subscription.filter { it.subscriptionStatus == "1" && (it.productId == IConstants.ANDROID_AD_FREE_ID || it.productId == IConstants.IOS_AD_FREE_ID) }
                .isNotEmpty()) {
            isAddsFree = true
        }
        return isAddsFree
    }

    fun isSubscriptionTaken(): Boolean {
        var isSubscriptionTaken = false
        if (subscription != null && subscription.size > 0 && subscription.filter { it.subscriptionStatus == "1" }
                .isNotEmpty()) {
            isSubscriptionTaken = true
        }
        return isSubscriptionTaken
    }

}