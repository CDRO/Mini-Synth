package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class StepRecordingTest {

    @Test
    fun testStepAdvanceOnRecord() {
        val manager = SynthManager()
        manager.startEngine()
        manager.clearSequencer()
        
        // Initial step is 0
        assertEquals(0, manager.getSequencerCurrentStep())
        
        // Record note 60
        val nextStep1 = manager.recordSequencerNote(60)
        
        // Should be at step 1 now
        assertEquals(1, nextStep1)
        assertEquals(1, manager.getSequencerCurrentStep())
        assertTrue(manager.isSequencerNoteActive(0, 60))
        
        // Record another
        val nextStep2 = manager.recordSequencerNote(64)
        assertEquals(2, nextStep2)
        assertEquals(2, manager.getSequencerCurrentStep())
        assertTrue(manager.isSequencerNoteActive(1, 64))
        
        manager.stopEngine()
    }
}
