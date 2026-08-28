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
class PhaseDistortionTest {

    @Test
    fun testPhaseDistortionJniRouting() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setPhaseDistortion(0.75f)
        
        val shadow = Shadow.extract<ShadowSynthManager>(manager)
        assertEquals(0.75f, shadow.lastPhaseDistortion, 0.001f)
    }

    @Test
    fun testMorphJniRouting() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.setMorph(2.5f)
        
        val shadow = Shadow.extract<ShadowSynthManager>(manager)
        assertEquals(2.5f, shadow.lastMorph, 0.001f)
    }
}
