package com.app.signme.view.settings.changepassword

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.rule.ActivityTestRule
import com.app.signme.util.Util
import org.junit.After
import org.junit.Before
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.app.signme.R
import org.junit.Rule
import org.junit.Test

class ChangePasswordActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<ChangePasswordActivity>
        = ActivityTestRule(ChangePasswordActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
        initialTest()
    }

    @Test
    fun initialTest(){
        validateOldPassword()
        Util.waitThread(2)
        validateNewPassword()
        Util.waitThread(2)
        validateConfirmPassword()
    }

    private fun validateOldPassword() {
        onView(withId(R.id.tietOldPassword))
            .perform(typeText(""), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())

        onView(withId(R.id.tietOldPassword))
            .check(matches(hasErrorText("Please enter old password")))

        onView(withId(R.id.tietOldPassword))
            .perform(replaceText("Pass@123"), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())
    }

    private fun validateNewPassword() {
        onView(withId(R.id.tietOldPassword))
            .perform(replaceText("Pass@123"), closeSoftKeyboard())

        onView(withId(R.id.tietNewPassword))
            .perform(typeText(""), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())

        onView(withId(R.id.tietNewPassword))
            .perform(replaceText("password"), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())

        onView(withId(R.id.tietNewPassword))
            .check(matches(hasErrorText("Password must contain at least 6 to 15 characters, including lower case, upper case and at least one number.")))

        onView(withId(R.id.tietNewPassword))
            .perform(replaceText("Password@123"), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())
    }

    private fun validateConfirmPassword() {
        onView(withId(R.id.tietOldPassword))
            .perform(replaceText("Pass@123"), closeSoftKeyboard())

        onView(withId(R.id.tietNewPassword))
            .perform(replaceText("Password@123"), closeSoftKeyboard())

        onView(withId(R.id.tietConfirmPassword))
            .perform(replaceText(""), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())

        onView(withId(R.id.tietConfirmPassword))
            .perform(replaceText("password"), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())

        onView(withId(R.id.tietConfirmPassword))
            .check(matches(hasErrorText("New password and confirm password must be same")))

        onView(withId(R.id.tietConfirmPassword))
            .perform(replaceText("Password@123"), closeSoftKeyboard())

        onView(withId(R.id.btnUpdate))
            .perform(click())
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}