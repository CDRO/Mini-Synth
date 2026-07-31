package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class SoundOutputTest {

    @Test
    fun testAudioEngineStatusAndOutput() {
        val manager = SynthManager()
        
        // Start engine and verify status
        manager.startEngine()
        // Wait a moment for Oboe to initialize
        Thread.sleep(200) 
        
        assertTrue("Audio engine should be running after startEngine()", manager.isEngineRunning())
        
        // Trigger a note and verify oscillator output
        manager.noteOn(60, 0.8f)
        
        // Wait for ADSR attack phase
        Thread.sleep(100) 
        
        // Check multiple samples to ensure non-silence
        var foundSound = false
        for (i in 0 until 100) {
            val sample = manager.renderSampleForTest()
            if (Math.abs(sample) > 0.0001f) {
                foundSound = true
                break
            }
        }
        
        assertTrue("Oscillator should produce non-zero samples when note is ON", foundSound)
        
        // Test master volume 0 leads to silence
        manager.setMasterVolume(0.0f)
        val silentSample = manager.renderSampleForTest()
        assertEquals("Master volume 0 should result in silence", 0.0f, silentSample, 0.00001f)
        
        manager.stopEngine()
        assertFalse("Audio engine should NOT be running after stopEngine()", manager.isEngineRunning())
    }

    @Test
    fun testMetronomeHighBpmStability() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Stress test at 240 BPM
        manager.setBpm(240f)
        manager.setMetronomeEnabled(true)
        
        // Run for 3 seconds
        Thread.sleep(3000)
        
        assertTrue("Engine should remain running during high BPM metronome", manager.isEngineRunning())
        
        // Verify we still get sound data (metronome tick is 500 samples, period at 240bpm is ~12k)
        var soundDetected = false
        for (i in 0 until 15000) {
            if (Math.abs(manager.renderSampleForTest()) > 0.0001f) {
                soundDetected = true
                break
            }
        }
        assertTrue("Engine should still produce sound (metronome tick) after high BPM stress", soundDetected)
        
        manager.stopEngine()
    }

    @Test
    fun testPolyphonyStress() {
        val manager = SynthManager()
        manager.startEngine()
        manager.setPolyphonic(true)
        
        // Trigger many notes rapidly (more than MAX_VOICES=16)
        for (note in 40..80) {
            manager.noteOn(note, 0.5f)
        }
        
        Thread.sleep(500)
        
        assertTrue("Engine should survive polyphony overload", manager.isEngineRunning())
        
        val sample = manager.renderSampleForTest()
        assertTrue("Engine should still output sound after voice overload", Math.abs(sample) > 0.0001f)
        
        manager.stopEngine()
    }
}
