package com.app.signme.dataclasses.response.forgotpasswordwithphone

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResetWithPhone(
        @JsonProperty("otp")
        var otp: String = "",
        @JsonProperty("reset_key")
        var resetKey: String = ""
)