package com.app.signme.view.settings.changephonenumber

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.app.signme.R

class ChangePhoneNumberActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<ChangePhoneNumberActivity>
        = ActivityTestRule(ChangePhoneNumberActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
        initialTest()
    }

    @Test
    fun initialTest(){
        validatePhoneNumber()
    }

    private fun validatePhoneNumber() {
        onView(withId(R.id.tietNewPhoneNumber))
            .perform(typeText(""), closeSoftKeyboard())

        onView(withId(R.id.btnSend))
            .perform(click())

        onView(withId(R.id.tietNewPhoneNumber))
            .perform(replaceText("9545954"), closeSoftKeyboard())

        onView(withId(R.id.btnSend))
            .perform(click())

        onView(withId(R.id.tietNewPhoneNumber))
            .check(matches(hasErrorText("Please enter valid phone number")))

        onView(withId(R.id.tietNewPhoneNumber))
            .perform(replaceText("9545954400"), closeSoftKeyboard())

        onView(withId(R.id.btnSend))
            .perform(click())
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}