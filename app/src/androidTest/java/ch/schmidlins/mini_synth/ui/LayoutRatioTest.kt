package ch.schmidlins.mini_synth.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class LayoutRatioTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testEssentialControlsVisibility() {
        onView(withId(R.id.top_header)).check(matches(isDisplayed()))
        onView(withId(R.id.workspace_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.toggle_keyboard)).check(matches(isDisplayed()))
    }
}
