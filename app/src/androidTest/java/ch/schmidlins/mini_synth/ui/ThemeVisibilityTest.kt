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
        // Verify control bar buttons are present
        onView(withId(R.id.btn_mode_toggle)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_poly_toggle)).check(matches(isDisplayed()))
        
        // Verify custom view is present
        onView(withId(R.id.keyboard_pad_view)).check(matches(isDisplayed()))
    }
}
