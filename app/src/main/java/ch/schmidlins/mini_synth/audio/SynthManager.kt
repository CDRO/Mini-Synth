package ch.schmidlins.mini_synth.audio

class SynthManager {
    companion object {
        init {
            System.loadLibrary("mini_synth")
        }
    }

    external fun startEngine()
    external fun stopEngine()
}
