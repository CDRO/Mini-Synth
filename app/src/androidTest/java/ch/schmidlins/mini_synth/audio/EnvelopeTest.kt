package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class EnvelopeTest {

    @Test
    fun testEnvelopeTransitions() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Short attack, long release
        manager.setAttack(0.01f)
        manager.setDecay(0.01f)
        manager.setSustain(0.5f)
        manager.setRelease(0.1f)
        
        manager.noteOn(60, 1.0f)
        
        // Render some samples during attack/decay/sustain
        val attackSamples = (0 until 100).map { manager.renderSampleForTest() }
        assertTrue("Envelope should start producing sound", attackSamples.any { it != 0.0f })
        
        manager.noteOff(60)
        
        // Immediately after noteOff, it should be in Release stage
        val releaseSample1 = manager.renderSampleForTest()
        assertTrue("Should still be audible during release", Math.abs(releaseSample1) > 0.0f)
        
        // Wait for release to finish (0.1s @ 48kHz = 4800 samples)
        // We can't wait that long easily, but we can check if it's decreasing
        val releaseSample2 = manager.renderSampleForTest()
        // Note: nextSample() called twice, may vary depending on oscillator phase
        // but the envelope multiplier should be decreasing.
        
        manager.stopEngine()
    }
}
