package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class PadSamplingTest {

    @Test
    fun testPadSamplingLogic() {
        val manager = SynthManager()
        manager.startEngine()
        
        // Start sampling on Pad 0
        manager.startPadSampling(0)
        
        // Feed some "simulated" sound (note 60)
        manager.noteOn(60, 0.8f)
        Thread.sleep(500)
        manager.noteOff(60)
        
        manager.stopPadSampling()
        
        // Trigger Pad 0 and verify sound output
        // We can't easily verify the buffer content from Kotlin, 
        // but we verify the engine doesn't crash and we can trigger it.
        manager.padNoteOn(0, 0.8f)
        Thread.sleep(100)
        manager.padNoteOff(0)
        
        manager.stopEngine()
    }
}
