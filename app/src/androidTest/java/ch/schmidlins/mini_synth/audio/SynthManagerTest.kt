package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class SynthManagerTest {

    @Test
    fun testEngineLifecycle() {
        val manager = SynthManager()
        manager.startEngine()
        manager.stopEngine()
    }

    @Test
    fun testOscillatorOutput() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Sine wave (0)
        manager.setWaveform(0)
        manager.noteOn(60, 1.0f)
        
        // Initial sample might be 0, but it should change
        val samples = (0 until 100).map { manager.renderSample() }
        assertTrue("Oscillator should produce non-zero samples", samples.any { it != 0.0f })
        assertTrue("Sine samples should be in [-1, 1]", samples.all { it >= -1.0f && it <= 1.0f })
        
        manager.noteOff(60)
        manager.stopEngine()
    }

    @Test
    fun testPolyphony() {
        val manager = SynthManager()
        manager.startEngine()
        manager.setPolyphonic(true)
        
        manager.noteOn(60, 1.0f)
        val sample1 = manager.renderSample()
        
        manager.noteOn(64, 1.0f)
        val sample2 = manager.renderSample()
        
        assertNotEquals("Mixing two notes should produce different values than one", sample1, sample2)
        
        manager.stopEngine()
    }
}
