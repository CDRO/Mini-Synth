package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class LfoTest {

    @Test
    fun testLfoModulation() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setWaveform(0) // Sine
        manager.noteOn(60, 1.0f)
        
        // No modulation
        manager.setLfoDepth(0.0f)
        val sampleBase = manager.renderSampleForTest()
        
        // High depth, fast rate tremolo (Volume modulation)
        manager.setLfoTarget(1) // Volume
        manager.setLfoRate(20.0f)
        manager.setLfoDepth(1.0f)
        
        // Render many samples and check for variance
        val samples = (0 until 1000).map { manager.renderSampleForTest() }
        val maxVal = samples.maxOf { Math.abs(it) }
        val minVal = samples.minOf { Math.abs(it) }
        
        assertTrue("LFO should modulate volume (amplitude variance expected)", (maxVal - minVal) > 0.1f)
        
        manager.stopEngine()
    }
}
