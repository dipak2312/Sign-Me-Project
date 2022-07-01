package com.app.signme.dataclasses


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserMedia(
    @JsonProperty("image_url")
    val imageUrl: String?="",
    @JsonProperty("media_id")
    val mediaId: String?=""
)