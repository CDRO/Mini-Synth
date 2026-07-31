package ch.schmidlins.mini_synth.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardViewTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testModeToggle() {
        // Initial state is "Pads" text on button, meaning currently in Keyboard mode
        onView(withId(R.id.btn_mode_toggle)).perform(scrollTo()).check(matches(withText("Pads")))
        
        // Click to switch to Pads mode
        onView(withId(R.id.btn_mode_toggle)).perform(scrollTo(), click())
        onView(withId(R.id.btn_mode_toggle)).check(matches(withText("Keys")))
        
        // Click back to Keyboard mode
        onView(withId(R.id.btn_mode_toggle)).perform(scrollTo(), click())
        onView(withId(R.id.btn_mode_toggle)).check(matches(withText("Pads")))
    }

    @Test
    fun testOctaveControls() {
        onView(withId(R.id.tv_octave_value)).perform(scrollTo()).check(matches(withText("0")))
        
        // Up
        onView(withId(R.id.btn_octave_up)).perform(scrollTo(), click())
        onView(withId(R.id.tv_octave_value)).check(matches(withText("1")))
        
        // Down
        onView(withId(R.id.btn_octave_down)).perform(scrollTo(), click())
        onView(withId(R.id.tv_octave_value)).check(matches(withText("0")))
    }
}
