#ifndef MINI_SYNTH_FILTER_H
#define MINI_SYNTH_FILTER_H

#include <stdint.h>

class Filter {
public:
    Filter();

    void setSampleRate(int32_t sampleRate);
    void setCutoff(float frequency);
    void setResonance(float resonance); // 0.0 to 1.0

    float process(float input);

private:
    float mSampleRate = 48000.0f;
    float mCutoff = 1000.0f;
    float mResonance = 0.5f;
    bool mParamsDirty = true;

    // Filter coefficients
    float f = 0.0f;
    float d = 0.0f;

    // State variables
    float low = 0.0f;
    float band = 0.0f;

    void updateCoefficients();
    void resetState();
};

#endif //MINI_SYNTH_FILTER_H
