@file:Suppress("DEPRECATION")

package com.app.signme.view


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.app.signme.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        val appCompatImageView = onView(
            allOf(
                withId(R.id.search_button), withContentDescription("Search"),
                childAtPosition(
                    allOf(
                        withId(R.id.search_bar),
                        childAtPosition(
                            withId(R.id.etSearchCity),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageView.perform(click())

        val searchAutoComplete = onView(
            allOf(
                withId(R.id.search_src_text),
                childAtPosition(
                    allOf(
                        withId(R.id.search_plate),
                        childAtPosition(
                            withId(R.id.search_edit_frame),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        searchAutoComplete.perform(replaceText("Delhi"), closeSoftKeyboard())

        val searchAutoComplete2 = onView(
            allOf(
                withId(R.id.search_src_text), withText("Delhi"),
                childAtPosition(
                    allOf(
                        withId(R.id.search_plate),
                        childAtPosition(
                            withId(R.id.search_edit_frame),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        searchAutoComplete2.perform(pressImeActionButton())

        val textView = onView(
            allOf(
                withId(R.id.tvTemperature), withText("28o"),
                childAtPosition(
                    allOf(
                        withId(R.id.ll_body),
                        childAtPosition(
                            withId(R.id.body_scroll_layout),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("28o")))

        val textView2 = onView(
            allOf(
                withId(R.id.tvCityName), withText("Delhi"),
                childAtPosition(
                    allOf(
                        withId(R.id.ll_body),
                        childAtPosition(
                            withId(R.id.body_scroll_layout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("Delhi")))

        val textView3 = onView(
            allOf(
                withId(R.id.tvSunRise), withText("Sunrise 18:36"),
                childAtPosition(
                    allOf(
                        withId(R.id.group_divider),
                        childAtPosition(
                            withId(R.id.ll_body),
                            4
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textView3.check(matches(withText("Sunrise 18:36")))

        val textView4 = onView(
            allOf(
                withId(R.id.tvSunSet), withText("Sunset 18:36"),
                childAtPosition(
                    allOf(
                        withId(R.id.group_divider),
                        childAtPosition(
                            withId(R.id.ll_body),
                            4
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        textView4.check(matches(withText("Sunset 18:36")))

        val textView5 = onView(
            allOf(
                withId(R.id.tvWindSpeed), withText("Wind Speed 0.95 km/h"),
                childAtPosition(
                    allOf(
                        withId(R.id.group_divider),
                        childAtPosition(
                            withId(R.id.ll_body),
                            4
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        textView5.check(matches(withText("Wind Speed 0.95 km/h")))

        val textView6 = onView(
            allOf(
                withId(R.id.tvHumidity), withText("Humidity 57%"),
                childAtPosition(
                    allOf(
                        withId(R.id.group_divider),
                        childAtPosition(
                            withId(R.id.ll_body),
                            4
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        textView6.check(matches(withText("Humidity 57%")))

        val textView7 = onView(
            allOf(
                withId(R.id.tvHumidity), withText("Humidity 57%"),
                childAtPosition(
                    allOf(
                        withId(R.id.group_divider),
                        childAtPosition(
                            withId(R.id.ll_body),
                            4
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        textView7.check(matches(withText("Humidity 57%")))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
