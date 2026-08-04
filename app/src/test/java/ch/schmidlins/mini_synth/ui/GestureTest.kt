package ch.schmidlins.mini_synth.ui

import android.view.MotionEvent
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class GestureTest {

    @Test
    fun testKeyboardGestures() {
        val context = RuntimeEnvironment.getApplication()
        val view = KeyboardPadView(context)
        view.measure(1000, 500)
        view.layout(0, 0, 1000, 500)
        
        var receivedPb = 0.0f
        var receivedMod = 0.0f
        
        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {}
            override fun onNoteOff(midi: Int) {}
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {
                receivedPb = pitchBend
                receivedMod = modulation
            }
        }

        // Touch Center of key 0
        val keyWidth = 1000 / 8f
        val startX = keyWidth / 2f
        val startY = 400f
        
        val down = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, startX, startY, 0)
        view.dispatchTouchEvent(down)
        
        // Slide Right 50% of key width -> should be +1 semitone
        val moveRight = MotionEvent.obtain(0L, 100L, MotionEvent.ACTION_MOVE, startX + (keyWidth / 2f), startY, 0)
        view.dispatchTouchEvent(moveRight)
        assertEquals("Horizontal slide should trigger Pitch Bend", 1.0f, receivedPb, 0.1f)
        
        // Slide Up 50% of view height -> should be 0.5 modulation
        val moveUp = MotionEvent.obtain(0L, 200L, MotionEvent.ACTION_MOVE, startX, startY - 250f, 0)
        view.dispatchTouchEvent(moveUp)
        assertTrue("Vertical slide should trigger Modulation", receivedMod > 0.4f)
        
        val up = MotionEvent.obtain(0L, 300L, MotionEvent.ACTION_UP, startX, startY - 250f, 0)
        view.dispatchTouchEvent(up)
        
        // Should reset on release
        assertEquals(0.0f, receivedPb, 0.001f)
        assertEquals(0.0f, receivedMod, 0.001f)
    }
}
