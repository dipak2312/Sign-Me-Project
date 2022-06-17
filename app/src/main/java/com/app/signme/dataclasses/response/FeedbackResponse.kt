package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedbackResponse(
    @JsonFormat(with = arrayOf(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
    @JsonProperty("images") val images: List<String>,
    @JsonProperty("feedback") val feedback: String,
    @JsonProperty("user_id") val user_id: Int,
    @JsonProperty("note") val note: String,
    @JsonProperty("device_type") val device_type: String,
    @JsonProperty("device_model") val device_model: String,
    @JsonProperty("device_os") val device_os: Int,
    @JsonProperty("status") val status: String,
    @JsonProperty("added_at") val added_at: String,
    @JsonProperty("updated_at") val updated_at: String,
    @JsonProperty("app_version") val app_version: String
)