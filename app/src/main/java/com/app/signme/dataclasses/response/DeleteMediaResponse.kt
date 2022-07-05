package com.app.signme.dataclasses.response


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteMediaResponse(
    @JsonProperty("media_id")
    val mediaId: String?=""
)