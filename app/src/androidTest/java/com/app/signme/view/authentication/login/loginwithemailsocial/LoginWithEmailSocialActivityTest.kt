package com.app.signme.view.authentication.login.loginwithemailsocial

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.app.signme.R
import com.app.signme.util.Util
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.app.signme.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity

class LoginWithEmailSocialActivityTest {
    @get:Rule
    public var activityRule : ActivityTestRule<LoginWithEmailSocialActivity>
            = ActivityTestRule(LoginWithEmailSocialActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun initialTest(){
        validateUsername()
        Util.waitThread(2)
        validatePassword()
        Util.waitThread(2)
        clickOnNewAccount()
        //clickOnForgotPassword()
    }

    private fun validateUsername() {
        onView(withId(R.id.tietUsername))
            .perform(typeText("username"), closeSoftKeyboard())

        onView(withId(R.id.mbtnLogin))
            .perform(click())

        onView(withId(R.id.tietUsername))
            .check(matches(hasErrorText("Please enter valid email")))

        onView(withId(R.id.tietUsername))
            .perform(replaceText("akshaykondekar81194@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.mbtnLogin))
            .perform(click())
    }

    private fun validatePassword() {
        onView(withId(R.id.tietUsername))
            .perform(replaceText("akshaykondekar81194@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.tietPassword))
            .perform(typeText("password"), closeSoftKeyboard())

        onView(withId(R.id.mbtnLogin))
            .perform(click())

        onView(withId(R.id.tietPassword))
            .check(matches(hasErrorText("Password must contain at least 6 to 15 characters, including lower case, upper case and at least one number.")))

        onView(withId(R.id.tietPassword))
            .perform(replaceText("Pass@123"), closeSoftKeyboard())

        onView(withId(R.id.mbtnLogin))
            .perform(click())
    }

    private fun clickOnForgotPassword() {
        Intents.init()
        onView(withId(R.id.tvForgotPassword))
            .perform(click())

        intended(hasComponent(ForgotPasswordWithEmailActivity::class.java.name))
        Intents.release()
    }

    private fun clickOnNewAccount() {
        Intents.init()
        onView(withId(R.id.tvCreateNewAccount))
            .perform(click())

        intended(hasComponent(SignUpWithPhoneNumberActivity::class.java.name))
        Intents.release()
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}