package ch.schmidlins.mini_synth.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkspaceUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testZenModeToggle() {
        // Initially visible
        onView(withId(R.id.parameter_container)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        
        // Toggle Zen Mode ON
        onView(withId(R.id.toggle_zen_mode)).perform(scrollTo(), click())
        
        // Should be GONE
        onView(withId(R.id.parameter_container)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        
        // Toggle Zen Mode OFF
        onView(withId(R.id.toggle_zen_mode)).perform(click())
        onView(withId(R.id.parameter_container)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun testBrowserToggle() {
        // Initially hidden
        onView(withId(R.id.sidebar_browser)).check(matches(not(isDisplayed())))
        
        // Toggle Browser ON
        onView(withId(R.id.toggle_browser)).perform(scrollTo(), click())
        onView(withId(R.id.sidebar_browser)).check(matches(isDisplayed()))
        
        // Toggle Browser OFF
        onView(withId(R.id.toggle_browser)).perform(click())
        onView(withId(R.id.sidebar_browser)).check(matches(not(isDisplayed())))
    }
}
