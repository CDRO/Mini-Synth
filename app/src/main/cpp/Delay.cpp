#include "Delay.h"
#include <algorithm>

Delay::Delay() {
    mBufferL.assign(48000 * 2, 0.0f); // 2 second buffer at 48kHz
    mBufferR.assign(48000 * 2, 0.0f);
}

void Delay::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<float>(sampleRate);
    size_t size = static_cast<size_t>(mSampleRate * 2.0f);
    if (mBufferL.size() < size) {
        mBufferL.assign(size, 0.0f);
        mBufferR.assign(size, 0.0f);
    }
}

void Delay::setTime(float seconds) {
    mTargetDelaySamples = std::max(0.0f, std::min(seconds, 2.0f)) * mSampleRate;
    if (mCurrentDelaySamples == 0.0f) mCurrentDelaySamples = mTargetDelaySamples;
}

void Delay::setFeedback(float feedback) {
    mFeedback = std::max(0.0f, std::min(feedback, 0.95f)); // Cap feedback to prevent explosion
}

void Delay::setMix(float mix) {
    mMix = std::max(0.0f, std::min(mix, 1.0f));
}

void Delay::process(float inputL, float inputR, float& outputL, float& outputR) {
    if (mTargetDelaySamples < 1.0f && mCurrentDelaySamples < 1.0f) {
        outputL = inputL;
        outputR = inputR;
        return;
    }

    // Smooth delay time transition (approx 20ms ramp)
    mCurrentDelaySamples = mCurrentDelaySamples * 0.999f + mTargetDelaySamples * 0.001f;

    // Linear Interpolation for delay read
    float readIdx = static_cast<float>(mWriteIndex) - mCurrentDelaySamples;
    while (readIdx < 0) readIdx += static_cast<float>(mBufferL.size());

    auto idx1 = static_cast<size_t>(readIdx);
    size_t idx2 = (idx1 + 1) % mBufferL.size();
    float frac = readIdx - static_cast<float>(idx1);

    float delayedL = mBufferL[idx1] * (1.0f - frac) + mBufferL[idx2] * frac;
    float delayedR = mBufferR[idx1] * (1.0f - frac) + mBufferR[idx2] * frac;

    // Ping-Pong Logic (Cross-feedback)
    // L input goes to L buffer + R feedback
    // R input goes to R buffer + L feedback
    mBufferL[mWriteIndex] = inputL + (delayedR * mFeedback);
    mBufferR[mWriteIndex] = inputR + (delayedL * mFeedback);

    mWriteIndex = (mWriteIndex + 1) % mBufferL.size();

    outputL = inputL * (1.0f - mMix) + delayedL * mMix;
    outputR = inputR * (1.0f - mMix) + delayedR * mMix;
}
