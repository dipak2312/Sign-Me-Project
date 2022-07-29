package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
class BlockUnblockResponse (
    @field:JsonProperty("user_block")
    val blockstatus: String? = "",
        )