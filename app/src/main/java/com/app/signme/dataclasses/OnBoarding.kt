package com.app.signme.dataclasses

import com.app.signme.R

data class OnBoarding(
    val image: Int,
    val title: String,
    val description: String
)

val boardingFirstScreen = OnBoarding(
    image = R.drawable.ic_onboarding1,
    title = "Horoscope Compatibility App",
    description = "Tired of trying to find someone off of just looks, we are to! Time to dig deeper to find your perfect match with SignME!"
)

val boardingSecondScreen = OnBoarding(
    image = R.drawable.ic_onboarding2,
    title = "Your Matches",
    description = "We go beyond what you are looking for, we connect based off of your compatibility with your astrology sign."
)

val boardingThirdScreen = OnBoarding(
    image = R.mipmap.ic_onboarding3,
    title = "Profile Summary",
    description = "Express yourself through various profile features including 6 profile photos available to upload and fun facts where individuals can see who they are potentially matching up with."
)

val onBoardings = arrayListOf(
    boardingFirstScreen, boardingSecondScreen, boardingThirdScreen)