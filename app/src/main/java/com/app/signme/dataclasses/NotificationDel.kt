package com.app.quicklook.dataclasses


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationDel(
    @JsonProperty("affected_rows")
    val affectedRows: String?="",

)