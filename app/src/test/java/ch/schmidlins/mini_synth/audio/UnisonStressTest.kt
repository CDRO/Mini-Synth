package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class UnisonStressTest {

    @Test
    fun testHighPolyphonyUnisonLoad() {
        val manager = SynthManager()
        manager.startEngine()
        
        // 8x Unison
        manager.setUnison(8, 20.0f, 1.0f)
        
        // Trigger 16 voices
        for (note in 60 until 76) {
            manager.noteOn(note, 0.5f)
        }
        
        // Render some samples and measure time (relative)
        val time = measureTimeMillis {
            repeat(1000) {
                manager.renderStereoSampleForTest(FloatArray(2))
            }
        }
        
        println("Rendered 1000 stereo samples with 16 polyphony and 8x unison in ${time}ms")
        
        manager.stopEngine()
    }
}
