package ch.schmidlins.mini_synth.audio

/**
 * JNI Bridge for the C++ Audio Engine.
 * Handles all real-time audio parameters, MIDI processing, and project management.
 */
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

    /** Start the Oboe audio stream. */
    external fun startEngine()
    
    /** Stop and close the Oboe audio stream. */
    external fun stopEngine()
    
    /** Check if the audio stream is active. */
    external fun isEngineRunning(): Boolean

    /** Trigger a synth voice for the given MIDI note. */
    external fun noteOn(midiNote: Int, velocity: Float)
    
    /** Release a synth voice for the given MIDI note. */
    external fun noteOff(midiNote: Int)
    
    /** Trigger playback of a sampled pad. */
    external fun padNoteOn(padIndex: Int, velocity: Float)
    
    /** Stop playback of a sampled pad. */
    external fun padNoteOff(padIndex: Int)
    
    /** Toggle looping for a sampled pad. */
    external fun setPadLooping(padIndex: Int, looping: Boolean)
    
    /** Start recording engine output into a specific pad buffer. */
    external fun startPadSampling(padIndex: Int)
    
    /** Manually stop pad sampling. */
    external fun stopPadSampling()
    
    /** 
     * Start automated sampling of engine output into a pad. 
     * Stops automatically after [durationSeconds].
     */
    external fun startAutomatedSampling(padIndex: Int, durationSeconds: Float)
    
    /** Load a built-in factory sample into a pad. */
    external fun loadFactorySample(padIndex: Int, sampleId: Int)
    
    /** Save a pad's PCM buffer to a binary file. */
    external fun savePadSample(padIndex: Int, path: String)
    
    /** Load a binary PCM file into a pad's buffer. */
    external fun loadPadSample(padIndex: Int, path: String)

    /** Toggle between polyphonic (16-voice) and monophonic modes. */
    external fun setPolyphonic(isPolyphonic: Boolean)
    
    /** Set the oscillator waveform (0: Sine, 1: Square, 2: Saw, 3: Triangle). */
    external fun setWaveform(waveformIndex: Int)
    
    /** Shift the keyboard range by octaves (+/- 4). */
    external fun setOctaveShift(shift: Int)
    
    external fun setAttack(seconds: Float)
    external fun setDecay(seconds: Float)
    external fun setSustain(level: Float)
    external fun setRelease(seconds: Float)
    
    /** Set overall engine output volume. */
    external fun setMasterVolume(volume: Float)

    /** Set master panning (-1.0 to 1.0). */
    external fun setPanning(panning: Float)

    /** Set Unison parameters. */
    external fun setUnison(count: Int, detune: Float, spread: Float)

    /** Set the waveform morph parameter (0.0 to 3.0). */
    external fun setMorph(morph: Float)

    /** Set the Phase Distortion amount (0.0 to 1.0). */
    external fun setPhaseDistortion(pd: Float)

    /** Load a custom wavetable into the oscillator. */
    external fun setWavetable(data: FloatArray)

    /** Set per-pad panning (-1.0 to 1.0). */
    external fun setPadPanning(padIndex: Int, panning: Float)

    external fun setLfoRate(frequency: Float)
    external fun setLfoDepth(depth: Float)
    external fun setLfoWaveform(waveformIndex: Int)
    external fun setLfoTarget(targetIndex: Int)
    external fun setAftertouchTarget(targetIndex: Int)

    external fun setFilterCutoff(frequency: Float)
    external fun setFilterResonance(resonance: Float)
    
    /** Shift pitch of all active voices in semitones. */
    external fun setPitchBend(semitones: Float)
    
    /** Adjust modulation depth (Mod Wheel equivalent). */
    external fun setModulation(amount: Float)

    /** Apply per-voice pressure modulation. */
    external fun setAftertouch(midiNote: Int, amount: Float)

    /** Retrieve real-time PCM data for UI visualization. */
    external fun getVisualizerData(buffer: FloatArray): Int
    
    /** Retrieve FFT magnitude data. */
    external fun getFftData(buffer: FloatArray): Int
    
    /** Start recording engine output to an MP3 file. */
    external fun startRecording(path: String)
    
    /** Stop MP3 recording and flush encoder. */
    external fun stopRecording()
    
    /** Render current sequencer pattern to an MP3 file (Faster than real-time). */
    external fun renderPatternToFile(path: String)
    
    /** Set engine tempo in beats per minute. */
    external fun setBpm(bpm: Float)
    
    /** Enable/disable metronome tick. */
    external fun setMetronomeEnabled(enabled: Boolean)
    
    /** Check if a beat boundary was just crossed (used for UI flash). */
    external fun isBeatStarted(): Boolean

    // Buffer Status
    external fun getXRunCount(): Int
    external fun getBufferSize(): Int
    external fun getFramesPerBurst(): Int
    external fun setAutoLatencyEnabled(enabled: Boolean)
    external fun checkAndApplyBufferSize()

    // Effects
    external fun setDelayTime(seconds: Float)
    external fun setDelayFeedback(feedback: Float)
    external fun setDelayMix(mix: Float)
    external fun setReverbSize(size: Float)
    external fun setReverbDamping(damping: Float)
    external fun setReverbMix(mix: Float)

    // Sequencer
    external fun setSequencerPlaying(playing: Boolean)
    external fun isSequencerPlaying(): Boolean
    external fun setSequencerRecording(recording: Boolean)
    external fun isSequencerRecording(): Boolean
    external fun setSequencerNote(step: Int, note: Int, active: Boolean)
    external fun isSequencerNoteActive(step: Int, note: Int): Boolean
    external fun getSequencerActiveNotes(step: Int): IntArray?
    external fun isSequencerStepActive(step: Int): Boolean
    external fun recordSequencerNote(note: Int): Int
    external fun setSequencerNumSteps(steps: Int)
    external fun handleRealTimeNoteOn(note: Int)
    external fun handleRealTimeNoteOff(note: Int)
    external fun clearSequencer()
    external fun stepRecordNote(note: Int)
    external fun stepRecordRest()
    external fun stepRecordBack()
    external fun setSequencerStepDuration(division: Float)
    external fun setInputQuantize(enabled: Boolean)
    external fun setOverdub(enabled: Boolean)
    external fun getSequencerCurrentStep(): Int

    /** Process a 3-byte MIDI message. Thread-safe. */
    external fun processMidi(data: ByteArray, length: Int)

    /** Save all pads and parameters to a project directory. */
    external fun saveProject(directory: String)
    
    /** Load all pads and parameters from a project directory. */
    external fun loadProject(directory: String)

    /** Render a stereo sample (L, R) for host-side unit tests. */
    external fun renderStereoSampleForTest(buffer: FloatArray): Int
}
