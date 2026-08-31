package ch.schmidlins.mini_synth.audio

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class MidiDeviceManagerTest {

    private lateinit var synthManager: SynthManager
    private lateinit var midiDeviceManager: MidiDeviceManager

    @Before
    fun setUp() {
        ShadowSynthManager.reset()
        synthManager = SynthManager()
        midiDeviceManager = MidiDeviceManager(RuntimeEnvironment.getApplication(), synthManager)
    }

    @Test
    fun testStartStop() {
        midiDeviceManager.start()
        midiDeviceManager.stop()
        // Simple smoke test for now
        assertTrue(true)
    }
}
