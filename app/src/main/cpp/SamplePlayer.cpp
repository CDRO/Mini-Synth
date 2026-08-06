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
        mPlaybackIndex = 0.0f;
        mIsPlaying = true;
    }
}

void SamplePlayer::stop() {
    mIsPlaying = false;
}

float SamplePlayer::nextSample() {
    if (!mIsPlaying || !mPlaybackBuffer || mPlaybackBuffer->empty()) return 0.0f;

    float index = mPlaybackIndex;
    size_t i0 = static_cast<size_t>(index);
    size_t i1 = i0 + 1;

    size_t size = mPlaybackBuffer->size();

    if (mIsLooping) {
        if (i0 >= size) {
            mPlaybackIndex = fmodf(mPlaybackIndex, static_cast<float>(size));
            i0 = static_cast<size_t>(mPlaybackIndex);
            i1 = i0 + 1;
        }
        if (i1 >= size) i1 = 0;
    } else {
        if (i0 >= size) {
            mIsPlaying = false;
            return 0.0f;
        }
        if (i1 >= size) i1 = i0; // Clamp
    }

    // Linear Interpolation
    float frac = index - static_cast<float>(i0);
    float s0 = (*mPlaybackBuffer)[i0];
    float s1 = (*mPlaybackBuffer)[i1];
    float sample = s0 + frac * (s1 - s0);

    mPlaybackIndex += mPlaybackRate;
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
