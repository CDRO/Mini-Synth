package ch.schmidlins.mini_synth.ui

import android.view.View
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
class WorkspaceRefinementTest {

    @Test
    fun testPadModeWorkspaceTransitions() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                
                val btnToggle = activity.findViewById<View>(R.id.btn_mode_toggle)
                val paramContainer = activity.findViewById<View>(R.id.parameter_container)
                val fullToggle = activity.findViewById<View>(R.id.toggle_pads_fullscreen)
                
                // Switch to Pads
                btnToggle.performClick()
                ShadowLooper.idleMainLooper()
                
                assertEquals("Parameter container should be GONE", View.GONE, paramContainer.visibility)
                assertEquals("Fullscreen toggle should be VISIBLE", View.VISIBLE, fullToggle.visibility)
                
                // Toggle Fullscreen ON
                fullToggle.performClick()
                ShadowLooper.idleMainLooper()
                assertEquals("Header should be GONE in fullscreen", View.GONE, activity.findViewById<View>(R.id.top_header).visibility)
            }
        }
    }

    @Test
    fun testHelpModeActivation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                val btnHelp = activity.findViewById<View>(R.id.btn_help_mode)
                val keyboard = activity.findViewById<View>(R.id.keyboard_pad_view)
                
                btnHelp.performClick()
                ShadowLooper.idleMainLooper()
                assertEquals("Keyboard should be GONE in Help Mode", View.GONE, keyboard.visibility)
            }
        }
    }
}
