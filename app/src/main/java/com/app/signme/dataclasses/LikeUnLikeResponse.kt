package com.app.signme.dataclasses


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LikeUnLikeResponse(
    @JsonProperty("user_rejected_id")
    val userRejectedId: String?="",
    @JsonProperty("like_id")
    val likeId: String?=""
)