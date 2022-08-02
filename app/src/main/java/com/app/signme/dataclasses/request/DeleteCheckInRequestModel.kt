package com.app.signme.dataclasses.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteCheckInRequestModel(
    @field:JsonProperty("notification_id")
    @field:SerializedName("notification_id")
    var notificationId: String? = ""
)