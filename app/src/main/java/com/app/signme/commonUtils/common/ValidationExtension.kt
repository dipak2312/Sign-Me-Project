package com.app.signme.commonUtils.common

import android.text.TextUtils
import android.util.Patterns
import android.webkit.URLUtil
import java.util.*
import java.util.concurrent.TimeUnit

fun String.isValidEmail(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidMobileNumber(): Boolean {
    return this.matches("[0-9]*".toRegex()) && !TextUtils.isEmpty(this) && this.length in 10..14
}

fun String.isValidMobileNumberLength(): Boolean {
    return this.length in 10..14
}

fun String.isValidText(): Boolean {
    return !TextUtils.isEmpty(this)
}

fun String.isValidTextLength(): Boolean {
    return !TextUtils.isEmpty(this) && this.length in 1..80
}

fun String.isValidUserNameLength(): Boolean {
    return !TextUtils.isEmpty(this) && this.length in 5..20
}


fun String.isValidZipCodeLength(): Boolean {
    return !TextUtils.isEmpty(this) && this.length in 5..10
}
fun String.isValidWebUrl(): Boolean {
    return URLUtil.isValidUrl(this)
}

/**
 * Validate string with only alpha numeric characters
 */
fun isOnlyAlphanumeric(string: String): Boolean {
    return string.matches("^[a-zA-Z0-9]*\$".toRegex())
}
/**
 * Validate otp is empty or not and checking length of otp
 */
fun String.isValidateOtp():Boolean {
    return !TextUtils.isEmpty(this) && this.length in 0..3
}
/**
 * Validate string with only alpha numeric and space characters
 */
fun isOnlyAlphanumericAndSpace(string: String): Boolean {
    return string.matches("^[a-zA-Z0-9 ]*\$".toRegex())
}

/**
 * Validate string with only alphaphate and space characters
 */
fun isOnlyAlphabetAndSpace(string: String): Boolean {
    return string.matches("^[a-zA-Z ]*\$".toRegex())
}

/**
 * password validation
 */
/*fun String.isValidPassword(): Boolean {

    val pattern: Pattern
    val matcher: Matcher

    val password_pattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,15}$"

    pattern = Pattern.compile(password_pattern)
    matcher = pattern.matcher(this)

    return !TextUtils.isEmpty(this) && matcher.matches()
}*/

/**
 * Convert time to minute seconds
 */
fun Long.timeToMinuteSecond(): String {
    var millisUntilFinished: Long = this
    val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
    millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
    millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
    millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

    return String.format(
        Locale.getDefault(),
        "%02d", seconds
    )
}