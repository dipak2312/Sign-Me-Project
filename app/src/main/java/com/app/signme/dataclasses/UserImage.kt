package com.app.signme.dataclasses

import android.os.Parcelable
import com.app.signme.commonUtils.utility.IConstants
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserImage(
    @field:SerializedName("image_id")
    @field:JsonProperty("image_id")
    var imageId: String? = "",

    @field:SerializedName("local_image_id")
    @field:JsonProperty("local_image_id")
    var localImageId: String? = "",

    @field:SerializedName("url")
    @field:JsonProperty("url")
    var imageUrl: String? = "",

    @field:SerializedName("uri")
    @field:JsonProperty("uri")
    var imageUri: String? = "",

    var uploadStatus: String = IConstants.DONE,

    var progress: Int =0,

    ) : Parcelable {
    fun showAddButton(): Boolean {
        return imageUri == "" && return imageUrl == ""
    }

    fun isUploadingFinished():Boolean{
        return when(uploadStatus){
            IConstants.DONE->true
            IConstants.PENDING->false
            IConstants.IN_PROGRESS->false
            else->true
        }
    }


    fun isUploadingFailed():Boolean{
        return when(uploadStatus){
            IConstants.DONE->false
            IConstants.PENDING->true
            IConstants.IN_PROGRESS->false
            else->false
        }
    }
}