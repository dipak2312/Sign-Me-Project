package com.app.signme.view.authentication.forgotpassword.email

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

class ForgotPasswordWithEmailActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<ForgotPasswordWithEmailActivity>
        =   ActivityTestRule(ForgotPasswordWithEmailActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun checkIsValueNotNull() {

        //enter some string in editText
        onView(withId(R.id.tietEMail))
            .perform(typeText("akshay.com"), closeSoftKeyboard())

        //click on button
        onView(withId(R.id.mbtnSendResetLink))
            .perform(click())

        //showing enter valid email error
        onView(withId(R.id.tietEMail))
            .check(matches(hasErrorText("Enter valid email")))

        //remove all text from editText
        onView(withId(R.id.tietEMail))
            .perform(replaceText(""), closeSoftKeyboard())

        //click on button
        onView(withId(R.id.mbtnSendResetLink))
            .perform(click())

        //showing enter valid email error
        onView(withId(R.id.tietEMail))
            .check(matches(hasErrorText("Enter valid email")))

       //add right email
        onView(withId(R.id.tietEMail))
            .perform(typeText("aksha@gmail.com"), closeSoftKeyboard())

        //click on button
        onView(withId(R.id.mbtnSendResetLink))
            .perform(click())
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}