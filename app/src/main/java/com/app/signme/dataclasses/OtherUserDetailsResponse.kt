package com.app.signme.dataclasses


import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class OtherUserDetailsResponse(
    @JsonProperty("about_me")
    val aboutMe: String?="",
    @JsonProperty("age")
    val age: String?="",
    @JsonProperty("city")
    val city: String?="",
    @JsonProperty("compatibility_data")
    val compatibilityData: List<CompatibilityData>?=null,
    @JsonProperty("email")
    val email: String?="",
    @JsonProperty("first_name")
    val firstName: String?="",
    @JsonProperty("higher_compatibility_data")
    val higherCompatibilityData: List<HigherCompatibilityData>?=null,
    @JsonProperty("last_name")
    val lastName: String?="",
    @JsonProperty("looking_for_relation_type")
    val lookingForRelationType: List<RelationshipType>?=null,
    @JsonProperty("name")
    val name: String?="",
    @JsonProperty("other_user_id")
    val otherUserId: String?="",
    @JsonProperty("sign_logo")
    val signLogo: String?="",
    @JsonProperty("sign_name")
    val signName: String?="",
    @JsonProperty("state_name")
    val stateName: String?="",
    @JsonProperty("user_media")
    val userMedia: List<UserMedia>?=null
):Parcelable
{
    fun getNameAndage():String
    {
        return firstName+" "+age
    }

    fun getCityAndState():String
    {
        return city+" "+stateName
    }

    fun getAboutName():String
    {
       return  "About"+" "+firstName
    }
}