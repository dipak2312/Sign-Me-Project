package com.app.signme.dataclasses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class MatchesResponse(
    @JsonProperty("first_name")
    val firstName: String?="",
    @JsonProperty("last_name")
    val lastName: String?="",
    @JsonProperty("profile_image")
    val profileImage: String?="",
    @JsonProperty("user_id")
    val userId: String?=""
)