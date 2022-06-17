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

class StaticPagesMultipleActivityTest {

    @get:Rule
    public var activityRule : ActivityTestRule<StaticPagesMultipleActivity>
        = ActivityTestRule(StaticPagesMultipleActivity::class.java)

    @Before
    fun setUp() {
        activityRule.activity
        initialTest()
    }

    @Test
    fun initialTest() {
        scrollBottomTest();
        Util.waitThread(2)
    }

    private fun scrollBottomTest() {
        onView(withId(R.id.srl))
            .perform(swipeUp(), click())
    }

    private fun scrollToTopTest() {
        onView(withId(R.id.srl))
            .perform(swipeDown(), click())
    }

    @After
    fun tearDown() {
        activityRule.activity.finish()
    }
}