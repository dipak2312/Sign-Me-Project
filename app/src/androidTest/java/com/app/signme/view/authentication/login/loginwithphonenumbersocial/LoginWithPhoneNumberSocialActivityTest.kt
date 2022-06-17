package com.app.signme.view.authentication.login.loginwithphonenumbersocial

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.rule.ActivityTestRule
import com.app.signme.util.Util
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.app.signme.R
import com.app.signme.view.authentication.forgotpassword.phone.ForgotPasswordWithPhoneActivity

class LoginWithPhoneNumberSocialActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<LoginWithPhoneNumberSocialActivity>
        = ActivityTestRule(LoginWithPhoneNumberSocialActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
        initialTest()
    }

    @Test
    fun initialTest(){
        validateUserName()
        Util.waitThread(2)
        validatePassword()
        Util.waitThread(2)
        //clickOnNewAccount()
        clickOnForgotPassword()
    }

    private fun validateUserName() {
        onView(withId(R.id.tietUsername))
            .perform(typeText("954595"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())

        onView(withId(R.id.tietUsername))
            .check(matches(hasErrorText("Please enter valid phone number")))

        onView(withId(R.id.tietUsername))
            .perform(replaceText("9545954400"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())
    }

    private fun validatePassword() {
        onView(withId(R.id.tietUsername))
            .perform(replaceText("9545954400"), closeSoftKeyboard())

        onView(withId(R.id.tietPassword))
            .perform(typeText("pass"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())

        onView(withId(R.id.tietPassword))
            .check(matches(hasErrorText("Password must contain at least 6 to 15 characters, including lower case, upper case and at least one number.")))

        onView(withId(R.id.tietPassword))
            .perform(replaceText("Pass@123"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())
    }

    private fun clickOnNewAccount() {
        Intents.init()
        onView(withId(R.id.tvCreateNewAccount))
            .perform(click())

        intended(hasComponent(SignUpWithPhoneNumberActivity::class.java.name))
        Intents.release()
    }

    private fun clickOnForgotPassword() {
        Intents.init()
        onView(withId(R.id.tvForgotPassword))
            .perform(click())

        intended(hasComponent(ForgotPasswordWithPhoneActivity::class.java.name))
        Intents.release()
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}