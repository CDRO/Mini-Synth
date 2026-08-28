#ifndef MINI_SYNTH_OSCILLATOR_H
#define MINI_SYNTH_OSCILLATOR_H

#include <cmath>
#include <stdint.h>
#include <vector>

enum class Waveform {
    Sine,
    Square,
    Saw,
    Triangle,
    Morph,
    Wavetable,
    Random
};

class Oscillator {
public:
    void setWaveform(Waveform waveform) { mWaveform = waveform; }
    void setFrequency(double frequency);
    void setSampleRate(int32_t sampleRate);
    void setMorph(float morph) { mMorph = morph; }
    void setWavetable(const float* data, int32_t size);
    void setPhaseDistortion(float pd) { mPhaseDistortion = pd; }
    void resetPhase() { mPhase = 0.0; }
    float nextSample();

private:
    Waveform mWaveform = Waveform::Sine;
    double mFrequency = 440.0;
    double mSampleRate = 48000.0;
    double mPhase = 0.0;
    double mPhaseIncrement = 0.0;
    float mMorph = 0.0f;
    float mPhaseDistortion = 0.0f;
    std::vector<float> mWavetable;

    void updatePhaseIncrement();
    float getWaveformSample(Waveform type, float phase);
    float applyPhaseDistortion(float phase);

    static float fastSin(float phase);

    static constexpr int SINE_LUT_SIZE = 1024;
    static float sSineLut[SINE_LUT_SIZE + 1];
    static bool sSineLutInitialized;
    static void initSineLut();
};

#endif //MINI_SYNTH_OSCILLATOR_H
