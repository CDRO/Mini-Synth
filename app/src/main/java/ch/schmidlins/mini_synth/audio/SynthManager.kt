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
    external fun renderSampleForTest(): Float
}
