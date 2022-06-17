package com.app.signme.dataclasses.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StaticPage(
    var pageCode: String? = "",
    var forceUpdate: Boolean? = false
) : Parcelable