package com.app.signme.dataclasses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
@JsonIgnoreProperties(ignoreUnknown = true)
class DeleteUserProfile(
    @field:SerializedName("url")
    @field:JsonProperty("url")
    var imageUrl: String? = ""
)