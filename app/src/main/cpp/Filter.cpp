#include "Filter.h"
#include <cmath>
#include <algorithm>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

Filter::Filter() {
    resetState();
    updateCoefficients();
}

void Filter::setSampleRate(int32_t sampleRate) {
    if (sampleRate <= 0) return;
    mSampleRate = static_cast<float>(sampleRate);
    updateCoefficients();
}

void Filter::setCutoff(float frequency) {
    // SVF is stable roughly up to SR/6. Clamp to SR/7 for safety.
    float maxCutoff = mSampleRate / 7.0f;
    mCutoff = std::max(20.0f, std::min(frequency, maxCutoff));
    updateCoefficients();
}

void Filter::setResonance(float resonance) {
    mResonance = std::max(0.0f, std::min(resonance, 0.99f));
    updateCoefficients();
}

void Filter::resetState() {
    low = 0.0f;
    band = 0.0f;
}

void Filter::updateCoefficients() {
    // Chamberlin SVF
    f = 2.0f * sinf(static_cast<float>(M_PI) * mCutoff / mSampleRate);
    // d = 1/Q. Map resonance [0, 1] to Q [0.5, 20]
    float q = 0.5f + (mResonance * 19.5f);
    d = 1.0f / q;
}

float Filter::process(float input) {
    // Basic SVF iteration
    low = low + f * band;
    float high = input - low - d * band;
    band = f * high + band;

    // Sanity check for instability
    if (std::isnan(low) || std::isinf(low)) {
        resetState();
        return 0.0f;
    }

    return low; // Low-pass output
}
