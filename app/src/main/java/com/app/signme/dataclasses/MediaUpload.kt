package com.app.signme.dataclasses

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaUpload(

    @JsonProperty("image_id")
    val imageId: String?,

    @JsonProperty("url")
    val url: String?,

    @SerializedName("user_id")
    @JsonIgnore
    var userId: String? = ""

)


