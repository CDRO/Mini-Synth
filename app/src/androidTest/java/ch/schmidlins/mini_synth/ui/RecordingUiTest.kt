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
class RecordingUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testRecModeToggle() {
        onView(withId(R.id.toggle_sequencer_rec)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.toggle_sequencer_rec)).check(matches(withText("REC OFF")))
        onView(withId(R.id.toggle_sequencer_rec)).perform(click())
        onView(withId(R.id.toggle_sequencer_rec)).check(matches(withText("REC ON")))
    }
}
