#ifndef MINI_SYNTH_LFO_H
#define MINI_SYNTH_LFO_H

#include <stdint.h>
#include <cmath>
#include "Oscillator.h"

enum class LfoTarget {
    Pitch,
    Volume,
    Filter
};

class Lfo {
public:
    Lfo();

    void setSampleRate(int32_t sampleRate);
    void setWaveform(Waveform waveform) { mWaveform = waveform; }
    void setFrequency(float frequency);
    void setDepth(float depth) { mDepth = depth; }
    float getDepth() const { return mDepth; }

    float nextValue(); // Returns value in range [-depth, depth]

private:
    Waveform mWaveform = Waveform::Sine;
    float mDepth = 0.0f;
    double mFrequency = 1.0;
    double mSampleRate = 48000.0;
    double mPhase = 0.0;
    double mPhaseIncrement = 0.0;

    void updatePhaseIncrement();
};

#endif //MINI_SYNTH_LFO_H
