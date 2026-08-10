package ch.schmidlins.mini_synth.ui

import android.view.View
import android.widget.ToggleButton
import androidx.test.core.app.ActivityScenario
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp-land-mdpi", shadows = [ShadowSynthManager::class])
class RecordingUiTest {

    @Test
    fun testRecModeToggle() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                val recToggle = activity.findViewById<ToggleButton>(R.id.toggle_sequencer_rec)
                
                assertEquals("●", recToggle.text.toString())
                recToggle.performClick()
                ShadowLooper.idleMainLooper()
                assertEquals("●", recToggle.text.toString())
            }
        }
    }
}
