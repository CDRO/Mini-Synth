package ch.schmidlins.mini_synth.audio

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetSerializationTest {

    @Test
    fun `test preset serialization and deserialization`() {
        val preset = SynthPreset(
            name = "Test Preset",
            waveformIndex = 2,
            attack = 0.5f,
            filterCutoff = 5000f
        )
        
        val json = Json.encodeToString(preset)
        val decoded = Json.decodeFromString<SynthPreset>(json)
        
        assertEquals(preset, decoded)
    }

    @Test
    fun `test list of presets serialization`() {
        val list = listOf(
            SynthPreset("Bass"),
            SynthPreset("Lead", waveformIndex = 1)
        )
        
        val json = Json.encodeToString(list)
        val decoded = Json.decodeFromString<List<SynthPreset>>(json)
        
        assertEquals(2, decoded.size)
        assertEquals("Bass", decoded[0].name)
        assertEquals("Lead", decoded[1].name)
    }
}
