package ch.schmidlins.mini_synth.ui

import android.widget.Button
import android.widget.ToggleButton
import androidx.test.core.app.ActivityScenario
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp-land-mdpi", shadows = [ShadowSynthManager::class])
class SequencerUiTest {

    @Test
    fun testStepToggling() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                val step0 = activity.findViewById<ToggleButton>(R.id.step_0)
                step0.performClick()
                ShadowLooper.idleMainLooper()
                assertTrue(step0.isChecked)
            }
        }
    }

    @Test
    fun testSequencerPlayStopToggle() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                val btnPlay = activity.findViewById<Button>(R.id.btn_sequencer_play)
                
                assertEquals("▶", btnPlay.text.toString())
                btnPlay.performClick()
                ShadowLooper.idleMainLooper()
                assertEquals("■", btnPlay.text.toString())
            }
        }
    }
}
