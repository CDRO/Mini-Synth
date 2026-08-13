package ch.schmidlins.mini_synth.audio

fun SynthManager.renderSampleForTest(): Float {
    val buffer = FloatArray(2)
    this.renderStereoSampleForTest(buffer)
    return (buffer[0] + buffer[1]) * 0.5f // Mono sum for compatibility with existing tests
}
