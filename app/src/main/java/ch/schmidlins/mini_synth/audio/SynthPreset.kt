package ch.schmidlins.mini_synth.audio

import kotlinx.serialization.Serializable

@Serializable
data class SynthPreset(
    val name: String,
    val version: Int = 1,
    val waveformIndex: Int = 0,
    val attack: Float = 0.1f,
    val decay: Float = 0.1f,
    val sustain: Float = 0.8f,
    val release: Float = 0.1f,
    val lfoRate: Float = 1.0f,
    val lfoDepth: Float = 0.0f,
    val lfoWaveformIndex: Int = 0,
    val lfoTargetIndex: Int = 0,
    val filterCutoff: Float = 1000.0f,
    val filterResonance: Float = 0.2f,
    val panning: Float = 0.0f,
    val unisonCount: Int = 1,
    val unisonDetune: Float = 0.0f,
    val sequencerStepDivision: Float = 0.25f,
    val padSamplePaths: Map<Int, String> = emptyMap(),
    val padPannings: Map<Int, Float> = emptyMap()
)
