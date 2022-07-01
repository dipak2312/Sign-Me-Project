package com.app.signme.dataclasses

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
class UserMediaList
    ( @JsonProperty("user_media")
      val userMedia: List<String>?=null):Parcelable