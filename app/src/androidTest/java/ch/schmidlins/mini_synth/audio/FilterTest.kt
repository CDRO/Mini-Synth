package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class FilterTest {

    @Test
    fun testFilterEffect() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setWaveform(1) // Square (rich harmonics)
        manager.noteOn(60, 1.0f)
        
        // Let oscillator/envelope advance
        repeat(50) { manager.renderSampleForTest() }
        
        // Open filter
        manager.setFilterCutoff(20000.0f)
        manager.setFilterResonance(0.0f)
        val openSamples = (0 until 100).map { Math.abs(manager.renderSampleForTest()) }
        val openSum = openSamples.sum()
        
        // Closed filter
        manager.setFilterCutoff(50.0f)
        val closedSamples = (0 until 100).map { Math.abs(manager.renderSampleForTest()) }
        val closedSum = closedSamples.sum()
        
        assertTrue("Filter should change average sample energy", Math.abs(openSum - closedSum) > 0.0001f)
        
        manager.stopEngine()
    }
}
