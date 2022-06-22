package com.app.signme.dataclasses.response


import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageCustomDialog(

    @field:JsonProperty("title")
    val title: String? = "",

    @field:JsonProperty("message")
    val message: String? = "",

    @field:JsonProperty("positiveButtonText")
    val positiveButtonText: String? = "Yes",

    @field:JsonProperty("negativeButtonText")
    val negativeButtonText: String? = "No"
) : Parcelable {

    fun isSingleButton():Boolean{
        return negativeButtonText!!.isEmpty()
    }
}
