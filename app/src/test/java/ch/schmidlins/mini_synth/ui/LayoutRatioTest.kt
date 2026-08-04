package ch.schmidlins.mini_synth.ui

import android.view.View
import android.view.ViewGroup
import ch.schmidlins.mini_synth.MainActivity
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp-land-mdpi", shadows = [ShadowSynthManager::class])
class LayoutRatioTest {

    @Test
    fun testLayoutHeightRatios() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        
        val root = activity.findViewById<ViewGroup>(R.id.activity_container)
        val header = activity.findViewById<View>(R.id.top_header)
        val keyboard = activity.findViewById<View>(R.id.keyboard_pad_view)
        
        // Ensure layouts are measured in Robolectric
        root.measure(View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
                     View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY))
        root.layout(0, 0, 1920, 1080)

        val rootHeight = root.height.toFloat()
        val headerHeight = header.height.toFloat()
        val keyboardHeight = keyboard.height.toFloat()
        
        val headerRatio = headerHeight / rootHeight
        val keyboardRatio = keyboardHeight / rootHeight
        
        // In the previous run, keyboard was 0.244. 
        // 0.3 * 1080 = 324. 
        // 0.2 * 1080 = 216.
        
        assertEquals("Header height ratio", 0.2f, headerRatio, 0.05f)
        assertEquals("Keyboard height ratio", 0.3f, keyboardRatio, 0.05f)
    }
}
