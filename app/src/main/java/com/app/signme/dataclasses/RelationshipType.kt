package com.app.signme.dataclasses


import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class RelationshipType(
    @JsonProperty("relationship_status")
    val relationshipStatus: String?="",
    @JsonProperty("relationship_status_id")
    val relationshipStatusId: String?=""
):Parcelable