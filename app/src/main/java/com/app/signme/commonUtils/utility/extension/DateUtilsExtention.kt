@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.app.signme.commonUtils.utility.extension

import android.annotation.SuppressLint
import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("SimpleDateFormat")
fun Date.toDDMMYYYYStr(): String {
    val format = SimpleDateFormat("dd/MM/yyyy")
    return format.format(this)
}

@SuppressLint("SimpleDateFormat")
fun Date.toMMDDYYYStr(): String {
    val format = SimpleDateFormat("MMM dd yyyy")
    return format.format(this)
}


@SuppressLint("SimpleDateFormat")
fun String.toMMDDYYYDate(): Date {
    return try {
        SimpleDateFormat("yyy-MM-dd").parse(this)
    } catch (ex: Exception) {
        Calendar.getInstance().time
    }
}

@SuppressLint("SimpleDateFormat")
fun Date.toHH_MM_AStr(): String {
    val format = SimpleDateFormat("hh:mm a")
    return format.format(this)
}

@SuppressLint("SimpleDateFormat")
fun Date.toAmPm(): String {
    val format = SimpleDateFormat("hh:mm a")
    return format.format(this)
}

@SuppressLint("SimpleDateFormat")
fun String.toAmPm(): Date {
    val format = SimpleDateFormat("hh:mm a")
    return format.parse(this)
}

@SuppressLint("SimpleDateFormat")
fun Date.to24HoursFormat(): String {
    val format = SimpleDateFormat("HH:mm")
    return format.format(this)
}

@SuppressLint("SimpleDateFormat")
fun String.to24HoursFormat(): Date {
    val format = SimpleDateFormat("HH:mm")
    return format.parse(this)
}

@SuppressLint("SimpleDateFormat")
fun String.from24HoursTo12Hours(): String {
    try {
        val sdf = SimpleDateFormat("HH:mm")
        val dateObj = sdf.parse(this)
        return SimpleDateFormat("hh:mm a").format(dateObj)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return this
}

@SuppressLint("SimpleDateFormat")
fun String.toMonthDayYearString(): String {
    var theDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    var date = Date()
    try {
        date = theDateFormat.parse(this)
    } catch (exception: Exception) {

    }
//    theDateFormat = SimpleDateFormat("MMM dd, yyyy")
//    return theDateFormat.format(date)

    val format = SimpleDateFormat("MMM dd, yyyy")
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun convertDate(inputDate: String): String {
    var theDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    var date: Date? = null
    try {
        date = theDateFormat.parse(inputDate)
    } catch (exception: Exception) {

    }

    theDateFormat = SimpleDateFormat("MMM dd, yyyy")
    return theDateFormat.format(date)
}

@SuppressLint("SimpleDateFormat")
fun Date.toServerDateFormatString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(this)
}

@SuppressLint("SimpleDateFormat")
fun String?.toServerDateFormatString(): String {
    if (this.isNullOrEmpty())
        return ""
    val theDateFormat: DateFormat = SimpleDateFormat("MMM dd yyyy")
    var date = Date()
    try {
        date = theDateFormat.parse(this)
    } catch (exception: Exception) {

    }

    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun String?.fromServerDatetoYYYYMMDD(): String {

    if (this.isNullOrEmpty())
        return ""
    try {
        if (this.contains("-")) {
            val str = this.replace("-", "")
            if (str.toInt() <= 0)
                return ""
        } else {
            return ""
        }
        val date = SimpleDateFormat("yyyy-MM-dd").parse(this)
        return SimpleDateFormat("MMM dd yyyy").format(date)
    } catch (exception: Exception) {
        exception.printStackTrace()
        return ""
    }
}

/**
 * Method to get days hours minutes seconds from milliseconds
 * @param millisUntilFinished Long
 * @return String
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

    // Format the string
    return String.format(
            Locale.getDefault(),
            "%02d", seconds
    )
}

fun Date.getFormattedDate(): String {
    val smsTime = Calendar.getInstance()
    smsTime.timeInMillis = this.time
    val now = Calendar.getInstance()
    val format = SimpleDateFormat("EEEE, dd MMM yyyy")
    return try {
        if (now[Calendar.DATE] === smsTime[Calendar.DATE]) {
            "Today"
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] === 1) {
            "Yesterday"
        } else if (now[Calendar.YEAR] === smsTime[Calendar.YEAR]) {
            format.format(this)
        } else {
            format.format(this)
        }
    } catch (e: java.lang.Exception) {
        Log.e("TAG", "getFormattedDate: ", e)
        ""
    }

}