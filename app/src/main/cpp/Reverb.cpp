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
        mCombsL[i].feedback = 0.7f + (mSize * 0.28f);
        mCombsR[i].feedback = 0.7f + (mSize * 0.28f);
    }
}

void Reverb::setDamping(float damping) {
    mDamping = std::max(0.0f, std::min(damping, 1.0f));
    for (int i = 0; i < NUM_COMBS; ++i) {
        mCombsL[i].damping = mDamping * 0.4f;
        mCombsR[i].damping = mDamping * 0.4f;
    }
}

void Reverb::setMix(float mix) {
    mMix = std::max(0.0f, std::min(mix, 1.0f));
}

void Reverb::initFilters() {
    int combLengths[] = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
    int apLengths[] = {556, 441, 341, 225};
    int stereoSpread = 23; // Offset for right channel width

    float scale = mSampleRate / 44100.0f;

    for (int i = 0; i < NUM_COMBS; ++i) {
        mCombsL[i].buffer.assign(static_cast<size_t>(combLengths[i] * scale), 0.0f);
        mCombsL[i].index = 0;
        mCombsL[i].filterState = 0.0f;
        mCombsL[i].feedback = 0.7f + (mSize * 0.28f);
        mCombsL[i].damping = mDamping * 0.4f;

        mCombsR[i].buffer.assign(static_cast<size_t>((combLengths[i] + stereoSpread) * scale), 0.0f);
        mCombsR[i].index = 0;
        mCombsR[i].filterState = 0.0f;
        mCombsR[i].feedback = 0.7f + (mSize * 0.28f);
        mCombsR[i].damping = mDamping * 0.4f;
    }

    for (int i = 0; i < NUM_ALLPASS; ++i) {
        mAllPassL[i].buffer.assign(static_cast<size_t>(apLengths[i] * scale), 0.0f);
        mAllPassL[i].index = 0;
        mAllPassL[i].feedback = 0.5f;

        mAllPassR[i].buffer.assign(static_cast<size_t>((apLengths[i] + stereoSpread) * scale), 0.0f);
        mAllPassR[i].index = 0;
        mAllPassR[i].feedback = 0.5f;
    }
}

void Reverb::process(float inputL, float inputR, float& outputL, float& outputR) {
    if (mMix <= 0.001f) {
        outputL = inputL;
        outputR = inputR;
        return;
    }

    float wetL = 0.0f;
    float wetR = 0.0f;

    // Mono input summed to stereo combs for diffusion
    float monoIn = (inputL + inputR) * 0.5f;

    for (int i = 0; i < NUM_COMBS; ++i) {
        wetL += mCombsL[i].process(monoIn);
        wetR += mCombsR[i].process(monoIn);
    }

    wetL *= 0.125f;
    wetR *= 0.125f;

    for (int i = 0; i < NUM_ALLPASS; ++i) {
        wetL = mAllPassL[i].process(wetL);
        wetR = mAllPassR[i].process(wetR);
    }

    outputL = (inputL * (1.0f - mMix)) + (wetL * mMix);
    outputR = (inputR * (1.0f - mMix)) + (wetR * mMix);
}
