package com.app.signme.mainactivitytest

import com.app.signme.db.entity.WeatherEntity
import java.net.ConnectException

val weatherEntity = WeatherEntity(
    1,
    "Delhi",
    "01a",
    "Cloudy",
    100.0,
    100.0,
    100,
    1000,
    100.0,
    1600000,
    1600000,
    270,
    10.0
)
val errorMsg = "Oops!! Something went wrong"
fun getErrorResponse(): Throwable {
    val errorResponse = Throwable(errorMsg)
    errorResponse.addSuppressed(ConnectException())
    return errorResponse
}