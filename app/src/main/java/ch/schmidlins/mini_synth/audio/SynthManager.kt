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

    /** Trigger a synth voice for the given MIDI note on a specific track. */
    external fun noteOn(midiNote: Int, velocity: Float, trackId: Int = 0)
    
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
    
    /** Set the oscillator waveform for a specific track. */
    external fun setTrackWaveform(track: Int, waveformIndex: Int)
    
    /** Shift the keyboard range by octaves (+/- 4). */
    external fun setOctaveShift(shift: Int)
    
    external fun setTrackAttack(track: Int, seconds: Float)
    external fun setTrackDecay(track: Int, seconds: Float)
    external fun setTrackSustain(track: Int, level: Float)
    external fun setTrackRelease(track: Int, seconds: Float)
    
    /** Set overall engine output volume. */
    external fun setMasterVolume(volume: Float)

    /** Set per-track volume. */
    external fun setTrackVolume(track: Int, volume: Float)

    /** Set per-track panning (-1.0 to 1.0). */
    external fun setTrackPanning(track: Int, panning: Float)

    /** Set Unison parameters for a track. */
    external fun setTrackUnison(track: Int, count: Int, detune: Float, spread: Float)

    /** Set the waveform morph parameter for a track. */
    external fun setTrackMorph(track: Int, morph: Float)

    /** Set the Phase Distortion amount for a track. */
    external fun setTrackPhaseDistortion(track: Int, pd: Float)

    /** Load a custom wavetable into the oscillator. */
    external fun setWavetable(data: FloatArray)

    /** Set per-pad panning (-1.0 to 1.0). */
    external fun setPadPanning(padIndex: Int, panning: Float)

    external fun setTrackLfoRate(track: Int, frequency: Float)
    external fun setTrackLfoDepth(track: Int, depth: Float)
    external fun setTrackLfoWaveform(track: Int, waveformIndex: Int)
    external fun setTrackLfoTarget(track: Int, targetIndex: Int)
    external fun setTrackLfoSync(track: Int, enabled: Boolean, beatsPerCycle: Float)
    external fun setTrackLfoMatrixAmount(track: Int, targetIndex: Int, amount: Float)
    external fun setAftertouchTarget(targetIndex: Int)

    external fun setTrackFilterCutoff(track: Int, frequency: Float)
    external fun setTrackFilterResonance(track: Int, resonance: Float)
    
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
    
    /** Start recording engine output to a WAV file. */
    external fun startRecording(path: String)
    
    /** Stop recording and flush encoder. */
    external fun stopRecording()
    
    /** Render current sequencer pattern to a WAV file (Faster than real-time). */
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
    external fun setSequencerNote(track: Int, step: Int, note: Int, active: Boolean)
    external fun isSequencerNoteActive(track: Int, step: Int, note: Int): Boolean
    external fun getSequencerActiveNotes(track: Int, step: Int): IntArray?
    external fun isSequencerStepActive(track: Int, step: Int): Boolean
    external fun recordSequencerNote(track: Int, note: Int): Int
    external fun setSequencerNumSteps(steps: Int)
    external fun handleRealTimeNoteOn(track: Int, note: Int)
    external fun handleRealTimeNoteOff(track: Int, note: Int)
    external fun clearSequencer()
    external fun clearSequencerTrack(track: Int)
    external fun stepRecordNote(track: Int, note: Int)
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
    
    // Backwards compatibility or convenience setters for active track
    fun setWaveform(waveformIndex: Int) = setTrackWaveform(0, waveformIndex)
    fun setAttack(seconds: Float) = setTrackAttack(0, seconds)
    fun setDecay(seconds: Float) = setTrackDecay(0, seconds)
    fun setSustain(level: Float) = setTrackSustain(0, level)
    fun setRelease(seconds: Float) = setTrackRelease(0, seconds)
    fun setFilterCutoff(frequency: Float) = setTrackFilterCutoff(0, frequency)
    fun setFilterResonance(resonance: Float) = setTrackFilterResonance(0, resonance)
    fun setLfoRate(frequency: Float) = setTrackLfoRate(0, frequency)
    fun setLfoDepth(depth: Float) = setTrackLfoDepth(0, depth)
    fun setLfoWaveform(waveformIndex: Int) = setTrackLfoWaveform(0, waveformIndex)
    fun setLfoTarget(targetIndex: Int) = setTrackLfoTarget(0, targetIndex)
    fun setUnison(count: Int, detune: Float, spread: Float) = setTrackUnison(0, count, detune, spread)
    fun setMorph(morph: Float) = setTrackMorph(0, morph)
    fun setPhaseDistortion(pd: Float) = setTrackPhaseDistortion(0, pd)
    fun setPanning(panning: Float) = setTrackPanning(0, panning)
    fun setSequencerNote(step: Int, note: Int, active: Boolean) = setSequencerNote(0, step, note, active)
    fun isSequencerNoteActive(step: Int, note: Int) = isSequencerNoteActive(0, step, note)
    fun getSequencerActiveNotes(step: Int) = getSequencerActiveNotes(0, step)
    fun recordSequencerNote(note: Int) = recordSequencerNote(0, note)
    fun handleRealTimeNoteOn(note: Int) = handleRealTimeNoteOn(0, note)
    fun handleRealTimeNoteOff(note: Int) = handleRealTimeNoteOff(0, note)
    fun stepRecordNote(note: Int) = stepRecordNote(0, note)
}
