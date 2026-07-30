#ifndef MINI_SYNTH_OSCILLATOR_H
#define MINI_SYNTH_OSCILLATOR_H

#include <cmath>
#include <stdint.h>

enum class Waveform {
    Sine,
    Square,
    Saw,
    Triangle
};

class Oscillator {
public:
    void setWaveform(Waveform waveform) { mWaveform = waveform; }
    void setFrequency(double frequency);
    void setSampleRate(int32_t sampleRate);
    float nextSample();

private:
    Waveform mWaveform = Waveform::Sine;
    double mFrequency = 440.0;
    double mSampleRate = 48000.0;
    double mPhase = 0.0;
    double mPhaseIncrement = 0.0;

    void updatePhaseIncrement();
};

#endif //MINI_SYNTH_OSCILLATOR_H
