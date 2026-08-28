package ch.schmidlins.mini_synth.shadows

import ch.schmidlins.mini_synth.audio.SynthManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(SynthManager::class)
class ShadowSynthManager {

    val noteOnCalls = mutableListOf<Int>()
    val noteOffCalls = mutableListOf<Int>()
    val midiMessages = mutableListOf<ByteArray>()
    val renderPatternCalls = mutableListOf<String>()
    val realTimeNoteOnCalls = mutableListOf<Int>()
    val recordSequencerNoteCalls = mutableListOf<Int>()
    var lastPhaseDistortion = 0f
    var lastMorph = 0f
    var lastPanning = 0f
    var lastUnisonCount = 1

    @Implementation fun startEngine() {}
    @Implementation fun stopEngine() {}
    @Implementation fun isEngineRunning(): Boolean = true
    @Implementation fun noteOn(midiNote: Int, velocity: Float) {
        noteOnCalls.add(midiNote)
    }
    @Implementation fun noteOff(midiNote: Int) {
        noteOffCalls.add(midiNote)
    }
    @Implementation fun padNoteOn(padIndex: Int, velocity: Float) {}
    @Implementation fun padNoteOff(padIndex: Int) {}
    @Implementation fun setPadLooping(padIndex: Int, looping: Boolean) {}
    @Implementation fun startPadSampling(padIndex: Int) {}
    @Implementation fun stopPadSampling() {}
    @Implementation fun startAutomatedSampling(padIndex: Int, durationSeconds: Float) {}
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
    @Implementation fun setPanning(panning: Float) {
        this.lastPanning = panning
    }
    @Implementation fun setUnison(count: Int, detune: Float, spread: Float) {
        this.lastUnisonCount = count
    }
    @Implementation fun setPadPanning(padIndex: Int, panning: Float) {}
    @Implementation fun setLfoRate(frequency: Float) {}
    @Implementation fun setLfoDepth(depth: Float) {}
    @Implementation fun setLfoWaveform(waveformIndex: Int) {}
    @Implementation fun setLfoTarget(targetIndex: Int) {}
    @Implementation fun setAftertouchTarget(targetIndex: Int) {}
    @Implementation fun setFilterCutoff(frequency: Float) {}
    @Implementation fun setFilterResonance(resonance: Float) {}
    @Implementation fun setPitchBend(semitones: Float) {}
    @Implementation fun setModulation(amount: Float) {}
    @Implementation fun setAftertouch(midiNote: Int, amount: Float) {}
    @Implementation fun getVisualizerData(buffer: FloatArray): Int = 0
    @Implementation fun getFftData(buffer: FloatArray): Int = 0
    @Implementation fun startRecording(path: String) {}
    @Implementation fun stopRecording() {}
    @Implementation fun renderPatternToFile(path: String) {
        renderPatternCalls.add(path)
    }
    @Implementation fun setMorph(morph: Float) {
        this.lastMorph = morph
    }
    @Implementation fun setPhaseDistortion(pd: Float) {
        this.lastPhaseDistortion = pd
    }
    @Implementation fun setBpm(bpm: Float) {}
    @Implementation fun setMetronomeEnabled(enabled: Boolean) {}
    @Implementation fun isBeatStarted(): Boolean = false
    
    // Buffer
    @Implementation fun getXRunCount(): Int = 0
    @Implementation fun getBufferSize(): Int = 0
    @Implementation fun getFramesPerBurst(): Int = 0
    @Implementation fun setAutoLatencyEnabled(enabled: Boolean) {}
    @Implementation fun checkAndApplyBufferSize() {}

    @Implementation fun setDelayTime(seconds: Float) {}
    @Implementation fun setDelayFeedback(feedback: Float) {}
    @Implementation fun setDelayMix(mix: Float) {}
    @Implementation fun setReverbSize(size: Float) {}
    @Implementation fun setReverbDamping(damping: Float) {}
    @Implementation fun setReverbMix(mix: Float) {}
    @Implementation fun setSequencerPlaying(playing: Boolean) {}
    @Implementation fun isSequencerPlaying(): Boolean = false
    @Implementation fun setSequencerRecording(recording: Boolean) {}
    @Implementation fun isSequencerRecording(): Boolean = false
    @Implementation fun setSequencerNote(step: Int, note: Int, active: Boolean) {}
    @Implementation fun isSequencerNoteActive(step: Int, note: Int): Boolean = false
    @Implementation fun getSequencerActiveNotes(step: Int): IntArray? = intArrayOf()
    @Implementation fun isSequencerStepActive(step: Int): Boolean = false
    @Implementation fun recordSequencerNote(note: Int): Int {
        recordSequencerNoteCalls.add(note)
        return 0
    }
    @Implementation fun setSequencerNumSteps(steps: Int) {}
    @Implementation fun handleRealTimeNoteOn(note: Int) {
        realTimeNoteOnCalls.add(note)
    }
    @Implementation fun handleRealTimeNoteOff(note: Int) {}
    @Implementation fun clearSequencer() {}
    @Implementation fun stepRecordNote(note: Int) {}
    @Implementation fun stepRecordRest() {}
    @Implementation fun stepRecordBack() {}
    @Implementation fun setSequencerStepDuration(division: Float) {}
    @Implementation fun setInputQuantize(enabled: Boolean) {}
    @Implementation fun setOverdub(enabled: Boolean) {}
    @Implementation fun getSequencerCurrentStep(): Int = 0
    @Implementation fun processMidi(data: ByteArray, length: Int) {
        midiMessages.add(data.copyOf())
    }
    @Implementation fun saveProject(directory: String) {}
    @Implementation fun loadProject(directory: String) {}
    @Implementation fun renderStereoSampleForTest(buffer: FloatArray): Int {
        buffer[0] = 0.0f
        buffer[1] = 0.0f
        return 2
    }
}
