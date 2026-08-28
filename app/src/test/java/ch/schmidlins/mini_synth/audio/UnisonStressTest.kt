package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class UnisonStressTest {

    @Test
    fun testMaximumPolyphonyAndUnison() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Configure for max load
        manager.setPolyphonic(true)
        manager.setUnison(8, 50f, 1.0f) // 8x Unison
        manager.setReverbMix(0.8f)
        manager.setDelayMix(0.8f)
        
        // Trigger 16 notes (Max Voices)
        // Total oscillators = 16 voices * 8 unison = 128 oscillators
        for (i in 0 until 16) {
            manager.noteOn(60 + i, 0.8f)
        }
        
        // In Robolectric/Shadow, we just verify it doesn't crash
        // The real test happens on physical/AVD
        assertTrue(manager.isEngineRunning())
    }
}
