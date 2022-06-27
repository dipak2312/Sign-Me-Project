package com.app.signme.dataclasses


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class SelectedRelationshipType(
    @JsonProperty("relationship_id")
    val relationshipId: String,
    @JsonProperty("relationship_status")
    val relationshipStatus: String
)