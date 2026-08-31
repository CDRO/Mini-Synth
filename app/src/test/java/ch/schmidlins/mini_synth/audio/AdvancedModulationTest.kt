package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class AdvancedModulationTest {

    @Test
    fun testLfoSyncJniRouting() {
        ShadowSynthManager.reset()
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackLfoSync(0, true, 0.5f)
        
        assertTrue(ShadowSynthManager.lastLfoSyncEnabled)
        assertEquals(0.5f, ShadowSynthManager.lastLfoSyncDivision, 0.001f)
    }

    @Test
    fun testLfoMatrixJniRouting() {
        ShadowSynthManager.reset()
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackLfoMatrixAmount(0, 2, 0.85f) // Filter target
        
        assertEquals(0.85f, ShadowSynthManager.lfoMatrixAmounts[2], 0.001f)
    }

    @Test
    fun testModulationStressStability() {
        ShadowSynthManager.reset()
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setPolyphonic(true)
        for (t in 0..3) {
            manager.setTrackUnison(t, 8, 50f, 1.0f)
            manager.setTrackLfoSync(t, true, 0.25f)
            for (target in 0..3) {
                manager.setTrackLfoMatrixAmount(t, target, 1.0f)
            }
        }
        
        for (i in 0..15) {
            manager.noteOn(60 + i, 0.8f, i % 4)
        }
        
        assertTrue(ShadowSynthManager.engineRunning)
    }
}
