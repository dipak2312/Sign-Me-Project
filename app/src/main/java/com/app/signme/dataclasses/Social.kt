package com.app.signme.dataclasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Social(var name: String? = "",
                  var accessToken: String? = "",
                  var profileImageUrl: String? = "",
                  var socialId: String? = "",
                  var emailId: String? = "",
                  var firstName: String? = "",
                  var lastName: String? = "",
                  var type: String? = "") : Parcelable