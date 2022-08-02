package com.app.quicklook.dataclasses

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize


@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationCount(
    @field:JsonProperty("notification_count")
    var notifyCount: String? = ""
) : Parcelable