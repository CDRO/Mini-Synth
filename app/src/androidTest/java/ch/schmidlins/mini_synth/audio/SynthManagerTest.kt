package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class SynthManagerTest {

    @Test
    fun testEngineLifecycle() {
        val manager = SynthManager()
        // These should not crash
        manager.startEngine()
        manager.noteOn(60, 0.8f)
        manager.noteOff(60)
        manager.setPolyphonic(true)
        manager.setWaveform(1)
        manager.setOctaveShift(1)
        manager.stopEngine()
    }
}
