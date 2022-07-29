package com.app.signme.dataclasses

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockedUser(

    @field:JsonProperty("u_user_id")
    var userId: String? = "",

    @field:JsonProperty("u_first_name")
    var firstName: String? = "",

    @field:JsonProperty("u_last_name")
    var lastName: String? = "",

    @field:JsonProperty("u_profile_image")
    var profileImage: String? = "",

    @field:JsonProperty("u_address")
    var address: String? = "",

    @field:JsonProperty("u_city")
    var city: String? = "",

    @field:JsonProperty("u_state_name")
    var statename: String? = "",

    @field:JsonProperty("u_zip_code")
    var zipcode: String? = "",

    @field:JsonProperty("u_latitude")
    var latitude: String? = "",

    @field:JsonProperty("u_longitude")
    var longitude: String? = "",

    @field:JsonProperty("block_status")
    var blockedstatus: String? = ""
) : Parcelable {

//    fun getFormattedPhoneNumber(): String {
//        return if (mobileNumber.isNullOrEmpty()) "" else PhoneNumberUtils.formatNumber(mobileNumber, "US")
//    }
}
