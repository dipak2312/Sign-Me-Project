package com.app.signme.commonUtils.utility.extension

import android.util.Patterns
import android.webkit.URLUtil
import androidx.lifecycle.MutableLiveData
import com.app.signme.utility.validation.*
import com.app.signme.R
import com.app.signme.commonUtils.utility.validation.createValidationResult
import com.app.signme.dataclasses.ValidationResult
import com.app.signme.view.authentication.signup.signupconfig.SignUpConfigItem
import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.isValidEmail(): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.isValidMobileNumber(): Boolean {
    return Patterns.PHONE.matcher(this).matches()
}

fun String.isValidMobileLenght(): Boolean {
    return (this.length in 10..14)
}

fun String.isValidWebUrl(): Boolean {
    return URLUtil.isValidUrl(this)
}

/**
 * phone number validation
 */
fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.matches("[0-9]*".toRegex()) && phoneNumber.length in 10..12
}

/**
 * Validate string with only alpha numeric characters
 */
fun isOnlyAlphanumric(string: String): Boolean {
    return string.matches("^[a-zA-Z0-9]*\$".toRegex())
}

/**
 * Validate string with only alpha numeric and space characters
 */
fun isOnlyAlphanumricAndSpace(string: String): Boolean {
    return string.matches("^[a-zA-Z0-9 ]*\$".toRegex())
}

/**
 * Validate string with only alphaphate and space characters
 */
fun isOnlyAlphabateAndSpace(string: String): Boolean {
    return string.matches("^[a-zA-Z ]*\$".toRegex())
}

/**
 * password validation
 */

//fun String.isValidPassword(): Boolean {
//    return this.length in 8..15
//}
fun String.isValidPassword(): Boolean {

    val pattern: Pattern
    val matcher: Matcher

    val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,15}$"

    pattern = Pattern.compile(passwordPattern)
    matcher = pattern.matcher(this)

    return matcher.matches()
}


/**
 * Check valid input text
 * @param text String text which need to validate
 * @param emptyFailType String FailType for empty field like email
 * @param invalidFailType String FailType for invalid field like invalid email
 * @param viewId Int Id of view, which need to be focus
 * @param validationObserver MutableLiveData<ValidationResult>? setting observer to notify activity/fragment to show validation message
 * @param config SignUpConfig? SignUp Configuration to check whether it is optional or hidden field
 * @return Boolean
 */
fun isValidInputText(text: String, emptyFailType: Int? = null, invalidFailType: Int? = null,
                     viewId: Int? = null,
                     minimumLength: Int = -1, maximumLength: Int = -1, validationObserver: MutableLiveData<ValidationResult>? = null,
                     config: SignUpConfigItem? = null): Boolean {

    if (config?.visible.equals("1") && config?.optional.equals("0")) {
        // If user name field is visible and it is not optional then need to check validation
        return if (text.isEmpty()) {
            validationObserver?.value = createValidationResult(emptyFailType, viewId)
            false
        } else if (!isValidTextLength(text = text, failType = invalidFailType, viewId = viewId, minimumLength = minimumLength, maximumLength = maximumLength, validationObserver = validationObserver)) {
            false
        } else checkForValidCharacters(text = text, invalidFailType = invalidFailType, viewId = viewId, validationObserver = validationObserver)

    } else if (config?.visible.equals("1") && config?.optional.equals("1") && text.isNotEmpty()) {
        // If user name field is visible and it is optional and if user name is not empty then check validation
        return if (isValidTextLength(text = text, failType = invalidFailType, viewId = viewId, minimumLength = minimumLength, maximumLength = maximumLength, validationObserver = validationObserver)) {
            checkForValidCharacters(text = text, invalidFailType = invalidFailType, viewId = viewId, validationObserver = validationObserver)
        } else {
            false
        }
    }
    return true
}


/**
 * Check for valid input characters
 * @param text String Text which need to be validate
 * @param invalidFailType Int? It can be username. first name or last name
 * @param viewId Int? Id of view
 * @param validationObserver MutableLiveData<ValidationResult>?
 * @return Boolean
 */
fun checkForValidCharacters(text: String, invalidFailType: Int? = null, viewId: Int? = null,
                            validationObserver: MutableLiveData<ValidationResult>? = null): Boolean {
    when (invalidFailType) {
        FIRST_NAME_INVALID -> return if (!isOnlyAlphabateAndSpace(text)) {
            validationObserver?.value = createValidationResult(FIRST_NAME_CHARACTER_INVALID, viewId)
            false
        } else {
            true
        }
        LAST_NAME_INVALID -> return if (!isOnlyAlphabateAndSpace(text)) {
            validationObserver?.value = createValidationResult(LAST_NAME_CHARACTER_INVALID, viewId)
            false
        } else {
            true
        }
        USER_NAME_INVALID -> return if (!isOnlyAlphanumric(text)) {
            validationObserver?.value = createValidationResult(USER_NAME_CHARACTER_INVALID, viewId)
            false
        } else {
            true
        }

        ZIP_CODE_INVALID -> return if (!isOnlyAlphanumric(text)) {
            validationObserver?.value = createValidationResult(ZIP_CODE_CHARACTER_INVALID, viewId)
            false
        } else {
            true
        }
        else -> return true
    }
}

/**
 * Check minimum length of text
 * @param text String text which need to validate
 * @param failType String FailType for invalid field like invalid email
 * @param viewId Int Id of view, which need to be focus
 * @param minimumLength Int Required minimum length of text
 * @param validationObserver MutableLiveData<ValidationResult>? setting observer to notify activity/fragment to show validation message
 */
fun isValidTextLength(text: String, failType: Int? = null, viewId: Int? = null,
                      minimumLength: Int, maximumLength: Int, validationObserver: MutableLiveData<ValidationResult>? = null): Boolean {
    return if (minimumLength != -1 && text.length < minimumLength) {
        validationObserver?.value = createValidationResult(failType, viewId)
        false
    } else if (maximumLength != -1 && text.length > maximumLength) {
        validationObserver?.value = createValidationResult(failType, viewId)
        false
    } else {
        true
    }
}

/**
 * Check valid Email
 * @param email String email which need to validate
 * @param validationObserver MutableLiveData<Settings>? setting observer to notify activity/fragment to show validation message
 * @return Boolean
 */
fun isValidInputEmail(email: String, validationObserver: MutableLiveData<ValidationResult>? = null): Boolean {

    if (email.isEmpty()) {
        validationObserver?.value = createValidationResult(EMAIL_EMPTY, R.id.tietEmail)
        return false
    } else if (!email.isValidEmail()) {
        validationObserver?.value = createValidationResult(EMAIL_INVALID, R.id.tietEmail)
        return false
    } else if (email.length > 50) {
        validationObserver?.value = createValidationResult(EMAIL_LENGTH, R.id.tietEmail)
        return false
    }
    return true
}

/**
 * Check valid phone number
 * @param phoneNumber String phone number which need to validate
 * @param validationObserver MutableLiveData<Settings>? setting observer to notify activity/fragment to show validation message
 * @param config SignUpConfig? SignUp Configuration to check whether it is optional or hidden field
 * @return Boolean
 */
//fun isValidInputPhone(phoneNumber: String, validationObserver: MutableLiveData<ValidationResult>? = null,
//                      config: SignUpConfigItem? = null): Boolean {
//
//    if (config?.visible.equals("1") && config?.optional.equals("0")) {
//        // If phone number field is visible and it is not optional then need to check validation
//        if (phoneNumber.isEmpty()) {
//            validationObserver?.value = createValidationResult(PHONE_NUMBER_EMPTY
//                    , R.id.tietMobileNumber)
//            return false
//        } else if (!phoneNumber.isValidMobileNumber()) {
//            validationObserver?.value = createValidationResult(PHONE_NUMBER_INVALID
//                    , R.id.tietMobileNumber)
//            return false
//        } else if (!phoneNumber.isValidMobileLenght()) {
//            validationObserver?.value = createValidationResult(PHONE_NUMBER_INVALID_LENGHT
//                    , R.id.tietMobileNumber)
//            return false
//        }
//
//    } else if (config?.visible.equals("1") && config?.optional.equals("1") && phoneNumber.isNotEmpty()) {
//        // If phone number field is visible and it is optional and if phone number is not empty then check validation
//        if (!phoneNumber.isValidMobileNumber()) {
//            validationObserver?.value = createValidationResult(PHONE_NUMBER_INVALID
//                    , R.id.tietMobileNumber)
//            return false
//        } else if (!phoneNumber.isValidMobileLenght()) {
//            validationObserver?.value = createValidationResult(PHONE_NUMBER_INVALID_LENGHT
//                    , R.id.tietMobileNumber)
//            return false
//        }
//    }
//    return true
//}

