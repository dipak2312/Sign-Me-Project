package com.app.signme.dataclasses.response

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaUploadResponse(
    @field:JsonProperty("image_id")
    val imageId: String?,

    @field:JsonProperty("local_image_id")
    val localImageId: String?,

    @field:JsonProperty("url")
    val url: String?
):Parcelable