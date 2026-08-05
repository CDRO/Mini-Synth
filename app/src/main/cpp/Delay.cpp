#include "Delay.h"
#include <algorithm>

Delay::Delay() {
    mBuffer.assign(48000 * 2, 0.0f); // 2 second buffer at 48kHz
}

void Delay::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<float>(sampleRate);
    if (mBuffer.size() < static_cast<size_t>(mSampleRate * 2.0f)) {
        mBuffer.assign(static_cast<size_t>(mSampleRate * 2.0f), 0.0f);
    }
}

void Delay::setTime(float seconds) {
    mDelaySamples = std::max(0.0f, std::min(seconds, 2.0f)) * mSampleRate;
}

void Delay::setFeedback(float feedback) {
    mFeedback = std::max(0.0f, std::min(feedback, 0.95f)); // Cap feedback to prevent explosion
}

void Delay::setMix(float mix) {
    mMix = std::max(0.0f, std::min(mix, 1.0f));
}

float Delay::process(float input) {
    if (mDelaySamples < 1.0f) return input;

    // Linear Interpolation for delay read
    float readIdx = static_cast<float>(mWriteIndex) - mDelaySamples;
    while (readIdx < 0) readIdx += static_cast<float>(mBuffer.size());

    size_t idx1 = static_cast<size_t>(readIdx);
    size_t idx2 = (idx1 + 1) % mBuffer.size();
    float frac = readIdx - static_cast<float>(idx1);

    float delayedSample = mBuffer[idx1] * (1.0f - frac) + mBuffer[idx2] * frac;

    // Write to buffer (input + feedback)
    mBuffer[mWriteIndex] = input + (delayedSample * mFeedback);
    mWriteIndex = (mWriteIndex + 1) % mBuffer.size();

    return input * (1.0f - mMix) + delayedSample * mMix;
}
