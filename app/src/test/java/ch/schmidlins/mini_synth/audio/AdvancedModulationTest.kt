package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.robolectric.shadow.api.Shadow

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class AdvancedModulationTest {

    @Test
    fun testLfoSyncJniRouting() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackLfoSync(0, true, 0.5f)
        
        val shadow = Shadow.extract<ShadowSynthManager>(manager)
        assertTrue(shadow.lastLfoSyncEnabled)
        assertEquals(0.5f, shadow.lastLfoSyncDivision, 0.001f)
    }

    @Test
    fun testLfoMatrixJniRouting() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackLfoMatrixAmount(0, 2, 0.85f) // Filter target
        
        val shadow = Shadow.extract<ShadowSynthManager>(manager)
        assertEquals(0.85f, shadow.lfoMatrixAmounts[2], 0.001f)
    }

    @Test
    fun testModulationStressStability() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Max polyphony + Max Unison + Heavy Modulation
        manager.setPolyphonic(true)
        for (t in 0..3) {
            manager.setTrackUnison(t, 8, 50f, 1.0f)
            manager.setTrackLfoSync(t, true, 0.25f) // Very fast LFO
            for (target in 0..3) {
                manager.setTrackLfoMatrixAmount(t, target, 1.0f)
            }
        }
        
        // Trigger notes on all tracks
        for (i in 0..15) {
            manager.noteOn(60 + i, 0.8f, i % 4)
        }
        
        // Verify no crash in shadow (real verification is native)
        assertTrue(manager.isEngineRunning())
    }
}
