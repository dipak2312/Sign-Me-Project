package com.app.signme.view.authentication.signup.signupconfig

import com.google.gson.annotations.SerializedName

data class SignUpConfigItem(

        @field:SerializedName("visible")
        var visible: String = "0",

        @field:SerializedName("optional")
        var optional: String = "0"
) {
    fun shouldShow(): Boolean {
        return visible == "1"
    }

    fun getHint(hint: String, optionalHint:String): String {
        return if(optional == "1") optionalHint
        else hint
    }
    fun isOptional(): Boolean {
        return optional == "1"
    }

}