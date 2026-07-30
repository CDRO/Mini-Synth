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
        
        // Render more samples to ensure we get past any initial zeros
        val samples = (0 until 500).map { manager.renderSampleForTest() }
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
        // Advance
        repeat(50) { manager.renderSampleForTest() }
        val sample1 = manager.renderSampleForTest()
        
        manager.noteOn(64, 1.0f)
        // Advance
        repeat(50) { manager.renderSampleForTest() }
        val sample2 = manager.renderSampleForTest()
        
        assertNotEquals("Mixing two notes should produce different values than one", sample1, sample2)
        
        manager.stopEngine()
    }

    @Test
    fun testMasterVolume() {
        val manager = SynthManager()
        manager.startEngine()
        manager.setWaveform(0)
        manager.noteOn(60, 1.0f)
        
        repeat(50) { manager.renderSampleForTest() }

        manager.setMasterVolume(1.0f)
        val loudSample = (0 until 100).maxOf { Math.abs(manager.renderSampleForTest()) }

        manager.setMasterVolume(0.1f)
        val quietSample = (0 until 100).maxOf { Math.abs(manager.renderSampleForTest()) }

        assertTrue("Lower master volume should decrease max sample amplitude", quietSample < loudSample)

        manager.setMasterVolume(0.0f)
        val silentSample = (0 until 100).maxOf { Math.abs(manager.renderSampleForTest()) }
        assertEquals("Master volume 0 should result in silence", 0.0f, silentSample, 0.001f)

        manager.stopEngine()
    }
}
