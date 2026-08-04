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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp-land-mdpi", shadows = [ShadowSynthManager::class])
class WorkspaceUiTest {

    @Test
    fun testZenModeToggle() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.isPollingEnabled = false
                val zenToggle = activity.findViewById<View>(R.id.toggle_zen_mode)
                val paramContainer = activity.findViewById<View>(R.id.parameter_container)
                
                assertEquals(View.VISIBLE, paramContainer.visibility)
                
                zenToggle.performClick()
                assertEquals("Parameter container should be GONE in Zen Mode", View.GONE, paramContainer.visibility)
                
                zenToggle.performClick()
                assertEquals(View.VISIBLE, paramContainer.visibility)
            }
        }
    }
}
