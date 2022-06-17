package com.app.signme.dataclasses.generics

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *Generic response for data if API return json object
 * */
class TAGenericResponse<T> : TABaseResponse() {
    @JsonProperty("data")
    var data: T? = null

}
