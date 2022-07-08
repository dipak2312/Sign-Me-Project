package com.app.signme.dataclasses


import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class HigherCompatibilityData(
    @JsonProperty("compatibility_percentage")
    val compatibilityPercentage: String?="",
    @JsonProperty("description")
    val description: String?="",
    @JsonProperty("relationship_status")
    val relationshipStatus: String?="",
    @JsonProperty("relationship_status_id")
    val relationshipStatusId: String?="",
    @JsonProperty("relationship_title")
    val relationshipTitle: String?=""
):Parcelable
{
    fun relationPercent():String
    {
        return compatibilityPercentage +"%"
    }
}