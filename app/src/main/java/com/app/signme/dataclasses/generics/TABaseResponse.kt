package com.app.signme.dataclasses.generics

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Base response of an api
 * @property settings [ERROR : null type]
 */
open class TABaseResponse {
    @JsonProperty("settings")
    var settings: Settings? = null
}