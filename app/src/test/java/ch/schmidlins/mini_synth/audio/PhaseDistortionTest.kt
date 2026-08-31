package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class PhaseDistortionTest {

    @Test
    fun testPhaseDistortionJniRouting() {
        ShadowSynthManager.reset()
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackPhaseDistortion(0, 0.75f)
        
        assertEquals(0.75f, ShadowSynthManager.lastPhaseDistortion, 0.001f)
    }

    @Test
    fun testMorphJniRouting() {
        ShadowSynthManager.reset()
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setTrackMorph(0, 0.5f)
        
        assertEquals(0.5f, ShadowSynthManager.lastMorph, 0.001f)
    }
}
