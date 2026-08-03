package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class MidiSequencerTest {

    @Test
    fun testSequencerGridLogic() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Clear initially
        manager.clearSequencer()
        for (i in 0 until 16) {
            assertFalse("Step $i should be empty", manager.isSequencerNoteActive(i, 60))
        }

        // Set some notes
        manager.setSequencerNote(0, 60, true)
        manager.setSequencerNote(5, 60, true)
        manager.setSequencerNote(15, 60, true)

        assertTrue("Step 0 should be active", manager.isSequencerNoteActive(0, 60))
        assertTrue("Step 5 should be active", manager.isSequencerNoteActive(5, 60))
        assertTrue("Step 15 should be active", manager.isSequencerNoteActive(15, 60))
        assertFalse("Step 1 should be empty", manager.isSequencerNoteActive(1, 60))

        // Clear and verify
        manager.clearSequencer()
        assertFalse("Step 0 should be empty after clear", manager.isSequencerNoteActive(0, 60))
        
        manager.stopEngine()
    }

    @Test
    fun testSequencerPlaybackState() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setSequencerPlaying(true)
        assertTrue(manager.isSequencerPlaying())
        
        manager.setSequencerPlaying(false)
        assertFalse(manager.isSequencerPlaying())
        
        manager.stopEngine()
    }
}
