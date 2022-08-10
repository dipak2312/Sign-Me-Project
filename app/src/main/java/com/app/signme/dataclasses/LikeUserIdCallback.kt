package com.app.signme.dataclasses

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
class LikeUserIdCallback(
    @JsonProperty("user_id")
    @SerializedName("user_id")
    var userId: String? = "",


    ) : Parcelable