package com.app.signme.dataclasses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
class LikesResponse (
    @JsonProperty("like_id")
    val likeId: String?="",
    @JsonProperty("url")
    val url: String?=""
)