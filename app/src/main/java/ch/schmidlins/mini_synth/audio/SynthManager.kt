package ch.schmidlins.mini_synth.audio

class SynthManager {
    companion object {
        init {
            try {
                System.loadLibrary("mini_synth")
            } catch (e: UnsatisfiedLinkError) {
                // Ignore in unit tests
            }
        }
    }

    external fun startEngine()
    external fun stopEngine()
    external fun isEngineRunning(): Boolean

    external fun noteOn(midiNote: Int, velocity: Float)
    external fun noteOff(midiNote: Int)
    
    external fun padNoteOn(padIndex: Int, velocity: Float)
    external fun padNoteOff(padIndex: Int)
    external fun startPadSampling(padIndex: Int)
    external fun stopPadSampling()
    external fun loadFactorySample(padIndex: Int, sampleId: Int)
    external fun savePadSample(padIndex: Int, path: String)
    external fun loadPadSample(padIndex: Int, path: String)

    external fun setPolyphonic(isPolyphonic: Boolean)
    external fun setWaveform(waveformIndex: Int)
    external fun setOctaveShift(shift: Int)
    external fun setAttack(seconds: Float)
    external fun setDecay(seconds: Float)
    external fun setSustain(level: Float)
    external fun setRelease(seconds: Float)
    external fun setMasterVolume(volume: Float)

    external fun setLfoRate(frequency: Float)
    external fun setLfoDepth(depth: Float)
    external fun setLfoWaveform(waveformIndex: Int)
    external fun setLfoTarget(targetIndex: Int)

    external fun setFilterCutoff(frequency: Float)
    external fun setFilterResonance(resonance: Float)
    external fun setPitchBend(semitones: Float)
    external fun setModulation(amount: Float)

    external fun getVisualizerData(buffer: FloatArray): Int
    external fun startRecording(path: String)
    external fun stopRecording()
    external fun renderPatternToFile(path: String)
    external fun setBpm(bpm: Float)
    external fun setMetronomeEnabled(enabled: Boolean)
    external fun isBeatStarted(): Boolean

    // Sequencer
    external fun setSequencerPlaying(playing: Boolean)
    external fun isSequencerPlaying(): Boolean
    external fun setSequencerNote(step: Int, note: Int, active: Boolean)
    external fun isSequencerNoteActive(step: Int, note: Int): Boolean
    external fun getSequencerActiveNotes(step: Int): IntArray?
    external fun isSequencerStepActive(step: Int): Boolean
    external fun recordSequencerNote(note: Int): Int
    external fun clearSequencer()
    external fun setSequencerStepDuration(division: Float)
    external fun getSequencerCurrentStep(): Int

    external fun renderSampleForTest(): Float
}
