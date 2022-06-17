package com.app.signme.view.authentication.otp.otpforgotpassword

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.app.signme.R
import org.junit.After
import org.junit.Before

import org.junit.Rule
import org.junit.Test

class OTPForgotPasswordActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<OTPForgotPasswordActivity> = ActivityTestRule(OTPForgotPasswordActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun checkIsDataValid() {

        //enter otp
       // onView(withId(R.id.otpView)).perform(typeText("123"), closeSoftKeyboard())


        onView(withId(R.id.btnSubmitOtp)).perform(click())

//        onView(withId(R.id.otpView)).check(matches(withText(R.string.alert_invalid_otp)))
//        onView(withText("Entered OTP is invalid"))
//            .inRoot(ToastMatcher())
//            .check(matches(isDisplayed()))

//        onView(withId(R.id.otpView)).perform(typeText(""), closeSoftKeyboard())
//
//        onView(withId(R.id.btnSubmitOtp)).perform(click())
//
//        onView(withId(R.id.otpView)).check(matches(hasErrorText("Please enter OTP")))


        //checking  message is displaying or not
//        onView(withId(R.id.tvOtpVerificationMessage)).check(matches(isDisplayed()))

        //checking display text matches with string text
//        onView(withId(R.id.tvOtpVerificationMessage)).check(matches(withText(R.string.label_enter_verification_code_message_text)))

        //checking phone number is displaying or not
//        onView(withId(R.id.tvPhoneNumber)).check(matches(isDisplayed()))

    }

    @Test
    fun navigateToResetPassword() {

      //  onView(withId(R.id.otpView)).perform(typeText("1234"), closeSoftKeyboard())
        onView(withId(R.id.btnSubmitOtp)).perform(click())

        onView(withId(R.id.resetActivity)).check(matches(isDisplayed()))

    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}