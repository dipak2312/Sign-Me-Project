package com.app.signme.dataclasses.response

import android.content.Context
import com.app.signme.commonUtils.utility.extension.getAppVersion
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class VersionConfigResponse(

    @field:SerializedName("iphone_version_number")
    val iphoneVersionNumber: String? = null,

    @field:SerializedName("android_version_number")
    val androidVersionNumber: String? = null,

    @JsonProperty("version_update_mandatory_android")
    val versionUpdateMandatory: String? = null,

    @JsonProperty("version_update_mandatory_ios")
    val versionUpdateMandatoryIos: String? = null,
    @field:SerializedName("version_update_check")
    val versionUpdateCheck: String? = null,

    @field:SerializedName("version_check_message")
    val versionCheckMessage: String? = null,

    @field:SerializedName("terms_conditions_version")
    val termsConditionsVersion: String? = null,

    @field:SerializedName("privacy_policy_version")
    val privacyPolicyVersion: String? = null,

    @field:SerializedName("terms_conditions_version_application")
    val termsConditionsVersionApplication: String? = null,

    @field:SerializedName("privacy_policy_version_application")
    val privacyPolicyVersionApplication: String? = null,

    @field:SerializedName("log_status_updated")
    val logStatusUpdated: String? = null

) {
    fun shouldShowVersionDialog(context: Context) =
        (((androidVersionNumber?.compareTo(getAppVersion(context, true))
            ?: 0) > 0) && versionUpdateCheck.equals("1"))

    fun isUpdateMandatory() = versionUpdateMandatory.equals("1")
    /* fun shouldShowTNCUpdated() = termsConditionsUpdated.equals("1")

     fun shouldShowPrivacyPolicyUpdated() = privacyPolicyUpdated.equals("1")*/

}