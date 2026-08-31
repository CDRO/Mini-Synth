package ch.schmidlins.mini_synth.shadows

import ch.schmidlins.mini_synth.audio.SynthManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(SynthManager::class)
class ShadowSynthManager {

    companion object {
        val noteOnCalls = mutableListOf<Int>()
        val noteOffCalls = mutableListOf<Int>()
        val midiMessages = mutableListOf<ByteArray>()
        var lastOctaveShift = 0
        var lastPolyphonic = true
        var lastBpm = 120f
        var engineRunning = false
        var sequencerPlaying = false
        var sequencerRecording = false
        var lastPhaseDistortion = 0f
        var lastMorph = 0f
        var lastLfoSyncEnabled = false
        var lastLfoSyncDivision = 1.0f
        val lfoMatrixAmounts = FloatArray(4)

        fun reset() {
            noteOnCalls.clear()
            noteOffCalls.clear()
            midiMessages.clear()
            lastOctaveShift = 0
            lastPolyphonic = true
            lastBpm = 120f
            engineRunning = false
            sequencerPlaying = false
            sequencerRecording = false
            lastPhaseDistortion = 0f
            lastMorph = 0f
            lastLfoSyncEnabled = false
            lastLfoSyncDivision = 1.0f
            for (i in 0..3) lfoMatrixAmounts[i] = 0f
        }
    }

    @Implementation fun startEngine() { engineRunning = true }
    @Implementation fun stopEngine() { engineRunning = false }
    @Implementation fun isEngineRunning(): Boolean = engineRunning
    @Implementation fun noteOn(midiNote: Int, velocity: Float, trackId: Int) {
        noteOnCalls.add(midiNote)
    }
    @Implementation fun noteOff(midiNote: Int) {
        noteOffCalls.add(midiNote)
    }
    @Implementation fun setPolyphonic(isPolyphonic: Boolean) {
        lastPolyphonic = isPolyphonic
    }
    @Implementation fun setOctaveShift(shift: Int) {
        lastOctaveShift = shift
    }
    @Implementation fun setBpm(bpm: Float) {
        lastBpm = bpm
    }
    @Implementation fun setSequencerPlaying(playing: Boolean) {
        sequencerPlaying = playing
    }
    @Implementation fun isSequencerPlaying(): Boolean = sequencerPlaying
    @Implementation fun setSequencerRecording(recording: Boolean) {
        sequencerRecording = recording
    }
    @Implementation fun isSequencerRecording(): Boolean = sequencerRecording
    
    @Implementation fun setTrackMorph(track: Int, morph: Float) {
        lastMorph = morph
    }
    @Implementation fun setTrackPhaseDistortion(track: Int, pd: Float) {
        lastPhaseDistortion = pd
    }
    @Implementation fun setTrackLfoSync(track: Int, enabled: Boolean, beatsPerCycle: Float) {
        lastLfoSyncEnabled = enabled
        lastLfoSyncDivision = beatsPerCycle
    }
    @Implementation fun setTrackLfoMatrixAmount(track: Int, targetIndex: Int, amount: Float) {
        if (targetIndex in 0..3) lfoMatrixAmounts[targetIndex] = amount
    }
    @Implementation fun processMidi(data: ByteArray, length: Int) {
        midiMessages.add(data.copyOf())
    }

    // Stubs
    @Implementation fun padNoteOn(padIndex: Int, velocity: Float) {}
    @Implementation fun padNoteOff(padIndex: Int) {}
    @Implementation fun setPadLooping(padIndex: Int, looping: Boolean) {}
    @Implementation fun startPadSampling(padIndex: Int) {}
    @Implementation fun stopPadSampling() {}
    @Implementation fun startAutomatedSampling(padIndex: Int, durationSeconds: Float) {}
    @Implementation fun loadFactorySample(padIndex: Int, sampleId: Int) {}
    @Implementation fun savePadSample(padIndex: Int, path: String) {}
    @Implementation fun loadPadSample(padIndex: Int, path: String) {}
    @Implementation fun setTrackWaveform(track: Int, waveformIndex: Int) {}
    @Implementation fun setTrackAttack(track: Int, seconds: Float) {}
    @Implementation fun setTrackDecay(track: Int, seconds: Float) {}
    @Implementation fun setTrackSustain(track: Int, level: Float) {}
    @Implementation fun setTrackRelease(track: Int, seconds: Float) {}
    @Implementation fun setTrackVolume(track: Int, volume: Float) {}
    @Implementation fun setTrackPanning(track: Int, panning: Float) {}
    @Implementation fun setTrackUnison(track: Int, count: Int, detune: Float, spread: Float) {}
    @Implementation fun setTrackLfoRate(track: Int, frequency: Float) {}
    @Implementation fun setTrackLfoDepth(track: Int, depth: Float) {}
    @Implementation fun setTrackLfoWaveform(track: Int, waveformIndex: Int) {}
    @Implementation fun setTrackLfoTarget(track: Int, targetIndex: Int) {}
    @Implementation fun setTrackFilterCutoff(track: Int, frequency: Float) {}
    @Implementation fun setTrackFilterResonance(track: Int, resonance: Float) {}
    @Implementation fun setMasterVolume(volume: Float) {}
    @Implementation fun setPadPanning(padIndex: Int, panning: Float) {}
    @Implementation fun setAftertouchTarget(targetIndex: Int) {}
    @Implementation fun setPitchBend(semitones: Float) {}
    @Implementation fun setModulation(amount: Float) {}
    @Implementation fun setAftertouch(midiNote: Int, amount: Float) {}
    @Implementation fun getVisualizerData(buffer: FloatArray): Int = 0
    @Implementation fun getFftData(buffer: FloatArray): Int = 0
    @Implementation fun startRecording(path: String) {}
    @Implementation fun stopRecording() {}
    @Implementation fun renderPatternToFile(path: String) {}
    @Implementation fun setMetronomeEnabled(enabled: Boolean) {}
    @Implementation fun isBeatStarted(): Boolean = false
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
    @Implementation fun setSequencerNote(track: Int, step: Int, note: Int, active: Boolean) {}
    @Implementation fun isSequencerNoteActive(track: Int, step: Int, note: Int): Boolean = false
    @Implementation fun getSequencerActiveNotes(track: Int, step: Int): IntArray? = intArrayOf()
    @Implementation fun isSequencerStepActive(track: Int, step: Int): Boolean = false
    @Implementation fun recordSequencerNote(track: Int, note: Int): Int = 0
    @Implementation fun setSequencerNumSteps(steps: Int) {}
    @Implementation fun handleRealTimeNoteOn(track: Int, note: Int) {}
    @Implementation fun handleRealTimeNoteOff(track: Int, note: Int) {}
    @Implementation fun clearSequencer() {}
    @Implementation fun clearSequencerTrack(track: Int) {}
    @Implementation fun stepRecordNote(track: Int, note: Int) {}
    @Implementation fun stepRecordRest() {}
    @Implementation fun stepRecordBack() {}
    @Implementation fun setSequencerStepDuration(division: Float) {}
    @Implementation fun setInputQuantize(enabled: Boolean) {}
    @Implementation fun setOverdub(enabled: Boolean) {}
    @Implementation fun getSequencerCurrentStep(): Int = 0
    @Implementation fun saveProject(directory: String) {}
    @Implementation fun loadProject(directory: String) {}
    @Implementation fun renderStereoSampleForTest(buffer: FloatArray): Int {
        buffer[0] = 0.0f
        buffer[1] = 0.0f
        return 2
    }
}
