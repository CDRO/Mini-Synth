package ch.schmidlins.mini_synth.audio

import android.media.midi.MidiReceiver
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class MidiDeviceManagerTest {

    @Test
    fun testMidiManagerInitialization() {
        val context = RuntimeEnvironment.getApplication()
        val synthManager = SynthManager()
        val midiDeviceManager = MidiDeviceManager(context, synthManager)
        
        midiDeviceManager.start()
        midiDeviceManager.stop()
    }

    @Test
    fun testMidiProcessingRegression() {
        val synthManager = SynthManager()
        val shadow = Shadow.extract<ShadowSynthManager>(synthManager)
        
        // Mock a MIDI Note On message (0x90, note, velocity)
        val noteOn = byteArrayOf(0x90.toByte(), 60.toByte(), 100.toByte())
        synthManager.processMidi(noteOn, 3)
        
        assertEquals(1, shadow.midiMessages.size)
        assertArrayEquals(noteOn, shadow.midiMessages[0])
    }
}
