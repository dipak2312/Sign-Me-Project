package com.app.signme.dataclasses.request

import android.os.Parcelable
import com.app.signme.dataclasses.UserMediaList
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


/**
 * This class is used for create Signup request
 * @property profileImage String   User's profile image.
 * @property userName String
 * @property firstName String
 * @property lastName String
 * @property mobileNumber String
 * @property email String
 * @property dob String
 * @property address String
 * @property city String
 * @property state String
 * @property stateId String
 * @property latitude String
 * @property longitude String
 * @property zipCode String
 * @property password String
 * @property confirmPassword String
 * @property socialType String
 * @property socialId String
 * @property tnc Boolean
 * @constructor
 */
@Parcelize
data class SignUpRequestModel(
    @field:SerializedName("user_profile")
    var profileImage: String = "",
    @field:SerializedName("user_name")
    var userName: String = "",
    @field:SerializedName("first_name")
    var firstName: String = "",
    @field:SerializedName("last_name")
    var lastName: String = "",
    @field:SerializedName("mobile_number")
    var mobileNumber: String = "",
    @field:SerializedName("email")
    var email: String = "",
    @field:SerializedName("dob")
    var dob: String = "",
    @field:SerializedName("address")
    var address: String = "",
    @field:SerializedName("city")
    var city: String = "",
    @field:SerializedName("state_name")
    var state: String = "",
    @field:SerializedName("latitude")
    var latitude: String = "",
    @field:SerializedName("longitude")
    var longitude: String = "",
    @field:SerializedName("zipcode")
    var zipCode: String = "",
    @field:SerializedName("delete_user_profile")
    var deleteUserProfile: String = "",
    @field:SerializedName("password")
    var password: String = "",
    var confirmPassword: String = "",
    @field:SerializedName("social_login_type")
    var socialType: String = "",
    @field:SerializedName("social_login_id")
    var socialId: String = "",
    var tnc: Boolean = true,
    @field:SerializedName("device_type")
    var deviceType: String = "",
    @field:SerializedName("device_model")
    var deviceModel: String = "",
    @field:SerializedName("device_os")
    var deviceOs: String = "",
    @field:SerializedName("device_token")
    var deviceToken: String = "",
    @field:SerializedName("terms_conditions_version")
    var termsConditionsVersion: String = "",
    @field:SerializedName("privacy_policy_version")
    var privacyPolicyVersion: String = "",
    @field:SerializedName("user_images")
    var userImages: String = "",
    @field:SerializedName("delete_image_ids")
    var deleteImageIds: String = "",
    @field:SerializedName("gender")
    var gender: String = "",
    @field:SerializedName("about_me")
    var aboutMe: String = "",
    @field:SerializedName("looking_for_gender")
    var lookingForGender: String = "",
    @field:SerializedName("looking_for_relation")
    var lookingForRelation: String = "",
    @field:SerializedName("max_distance")
    var maxDistance: String = "",
    @field:SerializedName("age_lower_limit")
    var ageLowerLimt: String = "",
    @field:SerializedName("age_upper_limit")
    var ageUpperLimt: String = "",
    @JsonProperty("user_media")
    var userMedia: String?=""

) : Parcelable