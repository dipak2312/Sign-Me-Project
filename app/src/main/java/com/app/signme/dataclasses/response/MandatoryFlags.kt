package com.app.signme.dataclasses.response

import com.fasterxml.jackson.annotation.JsonProperty

data class MandatoryFlags(
    @JsonProperty("is_address_mandatory")
    val isAddressMandatory: String? = "Yes", // // Yes -> Mandatory, No ->Not Mandatory
    @JsonProperty("is_email_id_mandatory")
    val isEmailMandatory: String? = "Yes",
    @JsonProperty("is_mobile_no_mandatory")
    val isMobileMandatory: String? = "Yes"
)

