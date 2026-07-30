#include "Envelope.h"
#include <algorithm>

Envelope::Envelope() {}

void Envelope::setSampleRate(int32_t sampleRate) {
    mSampleRate = sampleRate;
}

void Envelope::setAttack(float seconds) {
    // level increment per sample: 1.0 / (seconds * sampleRate)
    float samples = std::max(1.0f, seconds * mSampleRate);
    mAttackRate = 1.0f / samples;
}

void Envelope::setDecay(float seconds) {
    float samples = std::max(1.0f, seconds * mSampleRate);
    mDecayRate = (1.0f - mSustainLevel) / samples;
}

void Envelope::setSustain(float level) {
    mSustainLevel = level;
}

void Envelope::setRelease(float seconds) {
    float samples = std::max(1.0f, seconds * mSampleRate);
    mReleaseRate = mSustainLevel / samples;
}

void Envelope::trigger() {
    mState = State::Attack;
    mCurrentLevel = 0.0f;
}

void Envelope::release() {
    if (mState != State::Idle) {
        mState = State::Release;
    }
}

float Envelope::nextLevel() {
    switch (mState) {
        case State::Idle:
            mCurrentLevel = 0.0f;
            break;
        case State::Attack:
            mCurrentLevel += mAttackRate;
            if (mCurrentLevel >= 1.0f) {
                mCurrentLevel = 1.0f;
                mState = State::Decay;
            }
            break;
        case State::Decay:
            mCurrentLevel -= mDecayRate;
            if (mCurrentLevel <= mSustainLevel) {
                mCurrentLevel = mSustainLevel;
                mState = State::Sustain;
            }
            break;
        case State::Sustain:
            mCurrentLevel = mSustainLevel;
            break;
        case State::Release:
            mCurrentLevel -= mReleaseRate;
            if (mCurrentLevel <= 0.0f) {
                mCurrentLevel = 0.0f;
                mState = State::Idle;
            }
            break;
    }
    return mCurrentLevel;
}
