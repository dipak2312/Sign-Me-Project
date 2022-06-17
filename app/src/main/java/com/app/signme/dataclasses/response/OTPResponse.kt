package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class hold response of "check_unique_user" api
 * @property otp String?
 * @constructor
 */
data class OTPResponse(
        @JsonProperty("otp")
        val otp: String? = null
)