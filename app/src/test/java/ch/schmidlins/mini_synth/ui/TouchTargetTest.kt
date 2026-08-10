package ch.schmidlins.mini_synth.ui

import android.view.View
import androidx.test.core.app.ActivityScenario
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class TouchTargetTest {

    @Test
    fun testSequencerButtonTouchTargets() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val density = activity.resources.displayMetrics.density
                val minSizePx = 44 * density

                val buttons = listOf(
                    R.id.btn_sequencer_options,
                    R.id.btn_sequencer_play,
                    R.id.toggle_sequencer_rec,
                    R.id.btn_sequencer_clear,
                    R.id.toggle_pad_sampling,
                    R.id.toggle_step_rec
                )

                for (id in buttons) {
                    val view = activity.findViewById<View>(id)
                    assertTrue("Button ${activity.resources.getResourceEntryName(id)} width should be >= 44dp", view.layoutParams.width >= minSizePx || view.layoutParams.width == -1 || view.layoutParams.width == -2)
                    // In Robolectric, we usually check layoutParams if layout hasn't run fully or use measuredWidth
                    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    assertTrue("Button ${activity.resources.getResourceEntryName(id)} measured width should be >= 44dp", view.measuredWidth >= minSizePx)
                }
            }
        }
    }
}
