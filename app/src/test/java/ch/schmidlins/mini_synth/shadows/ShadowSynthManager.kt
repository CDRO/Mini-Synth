package ch.schmidlins.mini_synth.shadows

import ch.schmidlins.mini_synth.audio.SynthManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(SynthManager::class)
class ShadowSynthManager {

    @Implementation fun startEngine() {}
    @Implementation fun stopEngine() {}
    @Implementation fun isEngineRunning(): Boolean = true
    @Implementation fun noteOn(midiNote: Int, velocity: Float) {}
    @Implementation fun noteOff(midiNote: Int) {}
    @Implementation fun padNoteOn(padIndex: Int, velocity: Float) {}
    @Implementation fun padNoteOff(padIndex: Int) {}
    @Implementation fun startPadSampling(padIndex: Int) {}
    @Implementation fun stopPadSampling() {}
    @Implementation fun loadFactorySample(padIndex: Int, sampleId: Int) {}
    @Implementation fun savePadSample(padIndex: Int, path: String) {}
    @Implementation fun loadPadSample(padIndex: Int, path: String) {}
    @Implementation fun setPolyphonic(isPolyphonic: Boolean) {}
    @Implementation fun setWaveform(waveformIndex: Int) {}
    @Implementation fun setOctaveShift(shift: Int) {}
    @Implementation fun setAttack(seconds: Float) {}
    @Implementation fun setDecay(seconds: Float) {}
    @Implementation fun setSustain(level: Float) {}
    @Implementation fun setRelease(seconds: Float) {}
    @Implementation fun setMasterVolume(volume: Float) {}
    @Implementation fun setLfoRate(frequency: Float) {}
    @Implementation fun setLfoDepth(depth: Float) {}
    @Implementation fun setLfoWaveform(waveformIndex: Int) {}
    @Implementation fun setLfoTarget(targetIndex: Int) {}
    @Implementation fun setFilterCutoff(frequency: Float) {}
    @Implementation fun setFilterResonance(resonance: Float) {}
    @Implementation fun getVisualizerData(buffer: FloatArray): Int = 0
    @Implementation fun startRecording(path: String) {}
    @Implementation fun stopRecording() {}
    @Implementation fun renderPatternToFile(path: String) {}
    @Implementation fun setBpm(bpm: Float) {}
    @Implementation fun setMetronomeEnabled(enabled: Boolean) {}
    @Implementation fun isBeatStarted(): Boolean = false
    @Implementation fun setSequencerPlaying(playing: Boolean) {}
    @Implementation fun isSequencerPlaying(): Boolean = false
    @Implementation fun setSequencerNote(step: Int, note: Int, active: Boolean) {}
    @Implementation fun isSequencerNoteActive(step: Int, note: Int): Boolean = false
    @Implementation fun getSequencerActiveNotes(step: Int): IntArray? = intArrayOf()
    @Implementation fun isSequencerStepActive(step: Int): Boolean = false
    @Implementation fun recordSequencerNote(note: Int): Int = 0
    @Implementation fun clearSequencer() {}
    @Implementation fun setSequencerStepDuration(division: Float) {}
    @Implementation fun getSequencerCurrentStep(): Int = 0
    @Implementation fun renderSampleForTest(): Float = 0.0f
}
