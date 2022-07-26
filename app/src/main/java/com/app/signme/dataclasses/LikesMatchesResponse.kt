package com.app.signme.dataclasses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class LikesMatchesResponse(
    @JsonProperty("user_id")
    val userId: String? = "",
    @JsonProperty("first_name")
    val firstName: String? = "",
    @JsonProperty("last_name")
    val lastName: String? = "",
    @JsonProperty("profile_image")
    val profileImage: String? = "",
    @JsonProperty("sign_name")
    val signName: String? = "",
    @JsonProperty("sign_logo")
    val signLogo: String? = ""

)