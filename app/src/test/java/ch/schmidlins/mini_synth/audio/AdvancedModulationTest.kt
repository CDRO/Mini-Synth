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
}
