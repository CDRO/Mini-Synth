package ch.schmidlins.mini_synth.audio

class SynthManager {
    companion object {
        init {
            System.loadLibrary("mini_synth")
        }
    }

    external fun startEngine()
    external fun stopEngine()

    external fun noteOn(midiNote: Int, velocity: Float)
    external fun noteOff(midiNote: Int)
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

    external fun getVisualizerData(buffer: FloatArray): Int
    external fun startRecording(path: String)
    external fun stopRecording()
    external fun setBpm(bpm: Float)
    external fun setMetronomeEnabled(enabled: Boolean)
    external fun isBeatStarted(): Boolean

    external fun renderSampleForTest(): Float
}
