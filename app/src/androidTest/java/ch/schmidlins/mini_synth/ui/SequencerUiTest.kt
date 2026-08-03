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
class SequencerUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSequencerVisibility() {
        // Try to click the CLR button to ensure it's in view
        onView(withId(R.id.btn_sequencer_clear)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun testStepToggling() {
        // Toggle step 0
        onView(withId(R.id.step_0)).perform(scrollTo(), click())
    }

    @Test
    fun testSequencerPlayStopToggle() {
        onView(withId(R.id.btn_sequencer_play)).perform(scrollTo())
        onView(withId(R.id.btn_sequencer_play)).check(matches(withText("PLAY")))
        onView(withId(R.id.btn_sequencer_play)).perform(click())
        onView(withId(R.id.btn_sequencer_play)).check(matches(withText("STOP")))
    }
}
