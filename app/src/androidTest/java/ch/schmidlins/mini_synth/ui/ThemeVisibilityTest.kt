package ch.schmidlins.mini_synth.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.scrollTo
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeVisibilityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testUIVisibility() {
        // Verify top header elements (non-scrolling)
        onView(withId(R.id.visualizer_view)).check(matches(isDisplayed()))
        onView(withId(R.id.metronome_container)).check(matches(isDisplayed()))

        // Verify control bar and elements (inside ScrollView)
        onView(withId(R.id.btn_mode_toggle)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.btn_poly_toggle)).check(matches(isDisplayed()))
        
        // For elements in ScrollView, perform scrollTo() before checking isDisplayed()
        onView(withId(R.id.toggle_waveform)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Verify custom view is present and taking up space
        // keyboard_pad_view is outside ScrollView, so no scrollTo needed
        onView(withId(R.id.keyboard_pad_view)).check(matches(isDisplayed()))
    }
}
