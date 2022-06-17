package com.app.signme.view.authentication.forgotpassword.phone

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.app.signme.R
import org.junit.After
import org.junit.Before
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test

class ForgotPasswordWithPhoneActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<ForgotPasswordWithPhoneActivity>
        = ActivityTestRule(ForgotPasswordWithPhoneActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun checkIsDataValid(){

        //enter some numbers in edittext
        onView(withId(R.id.tietMobileNumber))
                .perform(typeText("954595"), closeSoftKeyboard())

        //is button disable
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //showing enter Enter 10 digit mobile number error
        onView(withId(R.id.tietMobileNumber))
                .check(matches(hasErrorText("Enter valid mobile number")))

        //remove numbers in edittext
        onView(withId(R.id.tietMobileNumber))
                .perform(replaceText(""), closeSoftKeyboard())

        //is button disable
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //showing enter Enter 10 digit mobile number error
        onView(withId(R.id.tietMobileNumber))
                .check(matches(hasErrorText("Enter valid mobile number")))

        //enter some numbers in edittext
        onView(withId(R.id.tietMobileNumber))
                .perform(typeText("9545954400"), closeSoftKeyboard())

        //is button disable
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //showing enter Enter 10 digit mobile number error
        onView(withId(R.id.tietMobileNumber))
            .check(matches(not(hasErrorText("Enter valid mobile number"))))

    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}