package com.app.signme.view.authentication.resetpassword

import android.os.SystemClock
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

class ResetPasswordActivityTest {

    @get : Rule
    public var activityRule: ActivityTestRule<ResetPasswordActivity> =
        ActivityTestRule(ResetPasswordActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun testResetPasswordFlow() {

        //enter new password value
        onView(withId(R.id.tietPassword))
            .perform(typeText("pas"), closeSoftKeyboard())

        //enter nothing into confirm password edittext
        onView(withId(R.id.tietConfirmPassword))
            .perform(typeText(""), closeSoftKeyboard())

        //click on next button
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //check is error showing in new password edittext or not
        onView(withId(R.id.tietPassword))
            .check(matches(hasErrorText("Enter valid password")))

        SystemClock.sleep(4000)

        //remove entered password from new password edittext
        onView(withId(R.id.tietPassword))
            .perform(replaceText(""))

        //enter valid password into new password value
        onView(withId(R.id.tietPassword))
            .perform(typeText("Akshay@81194"), closeSoftKeyboard())

        //enter wrong password into confirm password edittext
        onView(withId(R.id.tietConfirmPassword))
            .perform(typeText("Aksha"), closeSoftKeyboard())

        //click on button
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //enter wrong password into confirm password edittext
        onView(withId(R.id.tietConfirmPassword))
            .perform(replaceText(""), closeSoftKeyboard())

        //enter wrong password into confirm password edittext
        onView(withId(R.id.tietConfirmPassword))
            .perform(typeText("Akshay@81194"), closeSoftKeyboard())

        //click on button
        onView(withId(R.id.mbtnNext))
            .perform(click())

        //check weather the toast is showing or not
        /*onView(withText("New and Confirm password should same"))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()))*/

        /*onView(withText("New and Confirm password should same"))
            .inRoot(ToastMatcher())
            .check(matches(not(isDisplayed())))*/
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}