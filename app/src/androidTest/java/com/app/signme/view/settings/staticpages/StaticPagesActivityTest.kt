package com.app.signme.view.settings.staticpages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.app.signme.R
import com.app.signme.util.Util
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StaticPagesActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<StaticPagesActivity>
            = ActivityTestRule(StaticPagesActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
        initialTest()
    }

    @Test
    fun initialTest(){
        scrollToBottomTest()
        Util.waitThread(2)
        scrollToTopTest()
    }

    private fun scrollToTopTest() {
        onView(withId(R.id.nestedScrollView))
            .perform(swipeDown(), click())
    }

    private fun scrollToBottomTest() {
        onView(withId(R.id.nestedScrollView))
            .perform(swipeUp(), click())
    }

    @After
    fun tearDown() {
    }
}