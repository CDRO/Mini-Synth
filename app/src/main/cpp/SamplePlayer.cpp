#include "SamplePlayer.h"
#include <cmath>
#include <algorithm>

SamplePlayer::SamplePlayer() {
}

void SamplePlayer::startRecording(std::vector<float>& buffer) {
    mCurrentBuffer = &buffer;
    mRecordIndex = 0;
    mIsRecording = true;
    mIsPlaying = false;
}

void SamplePlayer::stopRecording() {
    if (mIsRecording && mCurrentBuffer) {
        mCurrentBuffer->resize(mRecordIndex);
    }
    mIsRecording = false;
    mCurrentBuffer = nullptr;
}

void SamplePlayer::recordSample(float sample) {
    if (mIsRecording && mCurrentBuffer && mRecordIndex < mCurrentBuffer->size()) {
        (*mCurrentBuffer)[mRecordIndex++] = sample;
    }
}

void SamplePlayer::trigger(const std::vector<float>& buffer) {
    mPlaybackBuffer = &buffer;
    if (mPlaybackBuffer && !mPlaybackBuffer->empty()) {
        if (mEndSample == 0 || mEndSample > mPlaybackBuffer->size()) {
            mEndSample = static_cast<uint32_t>(mPlaybackBuffer->size());
        }
        mPlaybackIndex = mIsReversed ? static_cast<float>(mEndSample - 1) : static_cast<float>(mStartSample);
        mIsPlaying = true;
    }
}

void SamplePlayer::stop() {
    mIsPlaying = false;
}

uint32_t SamplePlayer::findZeroCrossing(const std::vector<float>& buffer, uint32_t index) {
    if (buffer.empty()) return 0;
    uint32_t size = static_cast<uint32_t>(buffer.size());
    if (index >= size) index = size - 1;

    // Search forward/backward for a zero crossing (sign change)
    // We'll search up to 512 samples in both directions
    uint32_t bestIdx = index;
    float minVal = std::abs(buffer[index]);

    for (int offset = 1; offset < 512; ++offset) {
        // Forward
        if (index + offset < size) {
            float val = std::abs(buffer[index + offset]);
            if (val < minVal) {
                minVal = val;
                bestIdx = index + offset;
            }
            if (buffer[index + offset] * buffer[index + offset - 1] <= 0) return index + offset;
        }
        // Backward
        if (index >= static_cast<uint32_t>(offset)) {
            float val = std::abs(buffer[index - offset]);
            if (val < minVal) {
                minVal = val;
                bestIdx = index - offset;
            }
            if (buffer[index - offset] * buffer[index - offset + 1] <= 0) return index - offset;
        }
    }
    return bestIdx;
}

float SamplePlayer::nextSample() {
    if (!mIsPlaying || !mPlaybackBuffer || mPlaybackBuffer->empty()) return 0.0f;

    float index = mPlaybackIndex;
    size_t i0 = static_cast<size_t>(index);
    size_t i1;

    size_t size = mPlaybackBuffer->size();
    uint32_t start = mStartSample;
    uint32_t end = std::min(static_cast<uint32_t>(size), mEndSample);
    if (start >= end) return 0.0f;

    float rate = mIsReversed ? -mPlaybackRate : mPlaybackRate;

    if (mIsLooping) {
        if (mIsReversed) {
            if (mPlaybackIndex < static_cast<float>(start)) {
                mPlaybackIndex = static_cast<float>(end - 1);
            }
        } else {
            if (mPlaybackIndex >= static_cast<float>(end)) {
                mPlaybackIndex = static_cast<float>(start);
            }
        }
        i0 = static_cast<size_t>(mPlaybackIndex);
        i1 = mIsReversed ? (i0 == start ? end - 1 : i0 - 1) : (i0 + 1 >= end ? start : i0 + 1);
    } else {
        if (mIsReversed) {
            if (i0 < start || mPlaybackIndex < static_cast<float>(start)) {
                mIsPlaying = false;
                return 0.0f;
            }
            i1 = (i0 == start) ? i0 : i0 - 1;
        } else {
            if (i0 >= end) {
                mIsPlaying = false;
                return 0.0f;
            }
            i1 = (i0 + 1 >= end) ? i0 : i0 + 1;
        }
    }

    // Linear Interpolation
    float frac = std::abs(index - static_cast<float>(i0));
    float s0 = (*mPlaybackBuffer)[i0];
    float s1 = (*mPlaybackBuffer)[i1];
    float sample = s0 + frac * (s1 - s0);

    mPlaybackIndex += rate;
    return sample;
}

void SamplePlayer::reset() {
    mPlaybackIndex = 0.0f;
    mIsRecording = false;
    mIsPlaying = false;
    mIsLooping = false;
    mCurrentBuffer = nullptr;
    mPlaybackBuffer = nullptr;
}
