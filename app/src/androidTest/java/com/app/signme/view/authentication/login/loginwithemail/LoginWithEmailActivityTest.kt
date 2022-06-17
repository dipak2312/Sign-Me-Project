package com.app.signme.view.authentication.login.loginwithemail

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.app.signme.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.app.signme.util.Util
import com.app.signme.view.authentication.forgotpassword.email.ForgotPasswordWithEmailActivity

class LoginWithEmailActivityTest {

    @get:Rule
    var activityRule : ActivityTestRule<LoginWithEmailActivity> = ActivityTestRule(LoginWithEmailActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
    }

    @Test
    fun initialTest(){
        validateEmailTest()
        Util.waitThread(2)
        validatePasswordTest()
        Util.waitThread(2)
        clickOnNewAccountTest()
        //clickOnForgotPasswordTest()
    }

    private fun validateEmailTest(){
        onView(withId(R.id.tietEmail))
            .perform(typeText("akshay"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())

        onView(withId(R.id.tietEmail))
            .check(matches(hasErrorText("Please enter valid email")))

        onView(withId(R.id.tietEmail))
            .perform(replaceText("akshaykondekar81194@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())
    }

    private fun validatePasswordTest(){

        onView(withId(R.id.tietEmail))
            .perform(replaceText("akshaykondekar81194@gmail.com"), closeSoftKeyboard())

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

    private fun clickOnForgotPasswordTest() {
        Intents.init()
        onView(withId(R.id.tvForgotPassword))
            .perform(click())

        intended(hasComponent(ForgotPasswordWithEmailActivity::class.java.name))
        Intents.release()
    }

    private fun clickOnNewAccountTest() {
        Intents.init()
        onView(withId(R.id.tvCreateNewAccount))
            .perform(click())

        intended(hasComponent(SignUpWithPhoneNumberActivity::class.java.name))
        Intents.release()
    }

    @After
    fun tearDown(){
        activityRule.activity.finish()
    }
}