package ch.schmidlins.mini_synth.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
        // Verify control bar and elements
        onView(withId(R.id.control_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_mode_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_poly_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.toggle_waveform)).check(matches(isDisplayed()))
        
        // Verify custom view is present and taking up space
        onView(withId(R.id.keyboard_pad_view)).check(matches(isDisplayed()))
        
        // Check text color of labels to ensure they are visible on dark background (Off-white)
        // Note: Espresso doesn't have a built-in hasTextColor(ColorInt) but we can check if it's visible.
    }
}
