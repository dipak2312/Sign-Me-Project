package com.app.signme.view.authentication.signup.signupconfig

import com.google.gson.annotations.SerializedName

/**
 * This Data config file is used to config SignUp Screen
 * @property zip [ERROR : null type]
 * @property dateofbirth [ERROR : null type]
 * @property firstname [ERROR : null type]
 * @property city [ERROR : null type]
 * @property phonenumber [ERROR : null type]
 * @property skip String
 * @property streetaddress [ERROR : null type]
 * @property profilepictureoptional String
 * @property state [ERROR : null type]
 * @property email [ERROR : null type]
 * @property lastname [ERROR : null type]
 * @property username [ERROR : null type]
 * @constructor
 */
data class SignUpConfig(

        @field:SerializedName("zip")
        var zip: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("dateofbirth")
        var dateofbirth: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("firstname")
        var firstname: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("city")
        var city: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("phonenumber")
        var phonenumber: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("skip")
        var skip: String = "0",

        @field:SerializedName("streetaddress")
        var streetaddress: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("profilepictureoptional")
        var profilepictureoptional: String = "1",

        @field:SerializedName("state")
        var state: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("email")
        var email: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("lastname")
        var lastname: SignUpConfigItem = SignUpConfigItem(),

        @field:SerializedName("username")
        var username: SignUpConfigItem = SignUpConfigItem()
)