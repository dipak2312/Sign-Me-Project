package com.app.signme.dataclasses


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserNotification(
    @JsonProperty("created_at")
    val createdAt: String?="",
    @JsonProperty("entity_id")
    val entityId: String?="",
    @JsonProperty("entity_type")
    val entityType: String?="",
    @JsonProperty("notification_id")
    val notificationId: String?="",
    @JsonProperty("notification_message")
    val notificationMessage: String?="",
    @JsonProperty("redirection_type")
    val redirectionType: String?="",
    @JsonProperty("sender_first_name")
    val senderFirstName: String?="",
    @JsonProperty("sender_id")
    val senderId: String?="",
    @JsonProperty("sender_last_name")
    val senderLastName: String?="",
    @JsonProperty("sender_profile_image")
    val senderProfileImage: String?="",
    @JsonProperty("status")
    var notificationStatus: String?="",
    @JsonProperty("match_date")
    var matchDate: String?=""

)