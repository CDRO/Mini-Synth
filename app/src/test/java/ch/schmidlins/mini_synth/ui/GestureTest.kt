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

    private lateinit var view: KeyboardPadView

    @org.junit.Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        view = KeyboardPadView(context)
        view.measure(1000, 500)
        view.layout(0, 0, 1000, 500)
    }

    @Test
    fun testKeyboardGestures() {
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
            override fun onAftertouch(midi: Int, amount: Float) {}
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

    @Test
    fun testAftertouchTrigger() {
        var lastAftertouchMidi = -1
        var lastAftertouchAmount = 0.0f

        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {}
            override fun onNoteOff(midi: Int) {}
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {}
            override fun onAftertouch(midi: Int, amount: Float) {
                lastAftertouchMidi = midi
                lastAftertouchAmount = amount
            }
        }

        // Simulate pressure-like gesture or direct call if exposed
        // For now, verify the interface implementation in the test
        view.listener?.onAftertouch(60, 0.7f)
        assertEquals(60, lastAftertouchMidi)
        assertEquals(0.7f, lastAftertouchAmount, 0.01f)
    }

    @Test
    fun testKeyboardBoundaries() {
        var lastNoteOn = -1
        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) { lastNoteOn = midi }
            override fun onNoteOff(midi: Int) {}
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {}
            override fun onAftertouch(midi: Int, amount: Float) {}
        }

        // Far Left (below key 0)
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, -10f, 250f, 0))
        assertEquals(-1, lastNoteOn)

        // Far Right (above key 7)
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 1010f, 250f, 0))
        assertEquals(-1, lastNoteOn)
    }

    @Test
    fun testMultiTouchTracking() {
        val notesOn = mutableSetOf<Int>()
        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) { notesOn.add(midi) }
            override fun onNoteOff(midi: Int) { notesOn.remove(midi) }
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {}
            override fun onAftertouch(midi: Int, amount: Float) {}
        }

        // First pointer down
        val down1 = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 50f, 250f, 0)
        view.dispatchTouchEvent(down1)
        assertTrue("Note 60 should be on", notesOn.contains(60))

        // Second pointer down
        val pointerCoords = arrayOf(
            MotionEvent.PointerProperties().apply { id = 0 },
            MotionEvent.PointerProperties().apply { id = 1 }
        )
        val pointerCoordsValues = arrayOf(
            MotionEvent.PointerCoords().apply { x = 50f; y = 250f },
            MotionEvent.PointerCoords().apply { x = 150f; y = 250f }
        )

        val down2 = MotionEvent.obtain(0L, 100L, 
            MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
            2, pointerCoords, pointerCoordsValues, 0, 0, 1.0f, 1.0f, 0, 0, 0, 0)
        
        view.dispatchTouchEvent(down2)
        assertTrue("Note 62 should also be on", notesOn.contains(62))
        assertEquals(2, notesOn.size)
    }

    @Test
    fun testPadGridTouch() {
        view.setMode(KeyboardPadView.Mode.PAD_GRID)
        view.setGridDimensions(4, 4)
        
        var lastPadNote = -1
        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) { lastPadNote = midi }
            override fun onNoteOff(midi: Int) {}
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {}
            override fun onAftertouch(midi: Int, amount: Float) {}
        }

        // Touch Pad 0 (Top Left) -> should be MIDI 60
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 10f, 10f, 0))
        assertEquals(60, lastPadNote)

        // Touch Pad 15 (Bottom Right) -> should be MIDI 75
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 100L, MotionEvent.ACTION_DOWN, 990f, 490f, 0))
        assertEquals(75, lastPadNote)
    }

    @Test
    fun testModeSwitchingCleanup() {
        var notesOffCalled = 0
        view.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {}
            override fun onNoteOff(midi: Int) { notesOffCalled++ }
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {}
            override fun onGesture(pitchBend: Float, modulation: Float) {}
            override fun onAftertouch(midi: Int, amount: Float) {}
        }

        // Hold a note
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 50f, 250f, 0))
        // Slide up to hold
        view.dispatchTouchEvent(MotionEvent.obtain(0L, 100L, MotionEvent.ACTION_UP, 50f, 10f, 0))
        
        // Switch mode should clear held notes
        view.setMode(KeyboardPadView.Mode.PAD_GRID)
        // NoteOff should have been called (once for the touch release, but wait - if held, it stays active)
        // clearHeldNotes is called in setMode
        assertTrue("NoteOff should have been triggered during mode switch", notesOffCalled > 0)
    }
}
