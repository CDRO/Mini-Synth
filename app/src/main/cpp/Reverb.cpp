#include "Reverb.h"
#include <algorithm>

Reverb::Reverb() {
    initFilters();
}

void Reverb::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<float>(sampleRate);
    initFilters();
}

void Reverb::setSize(float size) {
    mSize = std::max(0.1f, std::min(size, 0.99f));
    for (int i = 0; i < NUM_COMBS; ++i) {
        mCombs[i].feedback = 0.7f + (mSize * 0.28f);
    }
}

void Reverb::setDamping(float damping) {
    mDamping = std::max(0.0f, std::min(damping, 1.0f));
    for (int i = 0; i < NUM_COMBS; ++i) {
        mCombs[i].damping = mDamping * 0.4f;
    }
}

void Reverb::setMix(float mix) {
    mMix = std::max(0.0f, std::min(mix, 1.0f));
}

void Reverb::initFilters() {
    int combLengths[] = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
    int apLengths[] = {556, 441, 341, 225};

    float scale = mSampleRate / 44100.0f;

    for (int i = 0; i < NUM_COMBS; ++i) {
        mCombs[i].buffer.assign(static_cast<size_t>(combLengths[i] * scale), 0.0f);
        mCombs[i].index = 0;
        mCombs[i].filterState = 0.0f;
        mCombs[i].feedback = 0.7f + (mSize * 0.28f);
        mCombs[i].damping = mDamping * 0.4f;
    }

    for (int i = 0; i < NUM_ALLPASS; ++i) {
        mAllPass[i].buffer.assign(static_cast<size_t>(apLengths[i] * scale), 0.0f);
        mAllPass[i].index = 0;
        mAllPass[i].feedback = 0.5f;
    }
}

float Reverb::process(float input) {
    if (mMix <= 0.001f) return input;

    float dry = input;
    float wet = 0.0f;

    // Parallel Combs
    for (int i = 0; i < NUM_COMBS; ++i) {
        wet += mCombs[i].process(input);
    }

    // Gain normalization
    wet *= 0.125f;

    // Serial All-pass
    for (int i = 0; i < NUM_ALLPASS; ++i) {
        wet = mAllPass[i].process(wet);
    }

    return (dry * (1.0f - mMix)) + (wet * mMix);
}
