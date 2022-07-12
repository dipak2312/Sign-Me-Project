package com.app.signme.dataclasses


import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class SwiperViewResponse(
    @JsonProperty("age")
    val age: String?="",
    @JsonProperty("astrology_sign_id")
    val astrologySignId: String?="",
    @JsonProperty("dob")
    val dob: String?="",
    @JsonProperty("name")
    val name: String?="",
    @JsonProperty("profile_image")
    val profileImage: String?="",
    @JsonProperty("sign_logo")
    val signLogo: String?="",
    @JsonProperty("sign_name")
    val signName: String?="",
    @JsonProperty("user_id")
    val userId: String?="",
    @JsonProperty("relationship_percent")
    val relationshipPercent: String?="",
    @JsonProperty("relationship_description")
    val relationshipDescription: String?="",
    @JsonProperty("is_like")
    val isLike: String?=""
): Parcelable
{
    fun getUserNameAndAge():String
    {
        return name+" "+age
    }

    fun getRelationPercent():String
    {
        return relationshipPercent+"%"
    }
}