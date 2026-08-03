#include "SamplePlayer.h"

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
        mCurrentBuffer->resize(mRecordIndex); // Trim to actual length on stop (safe outside audio loop)
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
        mPlaybackIndex = 0;
        mIsPlaying = true;
    }
}

void SamplePlayer::stop() {
    mIsPlaying = false;
}

float SamplePlayer::nextSample() {
    if (!mIsPlaying || !mPlaybackBuffer) return 0.0f;

    if (mPlaybackIndex >= mPlaybackBuffer->size()) {
        mIsPlaying = false;
        return 0.0f;
    }

    return (*mPlaybackBuffer)[mPlaybackIndex++];
}

void SamplePlayer::reset() {
    mPlaybackIndex = 0;
    mIsRecording = false;
    mIsPlaying = false;
    mCurrentBuffer = nullptr;
    mPlaybackBuffer = nullptr;
}
