#include "Envelope.h"
#include <algorithm>

Envelope::Envelope() {
    updateAttackRate();
    updateDecayRate();
    updateReleaseRate();
}

void Envelope::setSampleRate(int32_t sampleRate) {
    mSampleRate = sampleRate;
    updateAttackRate();
    updateDecayRate();
    updateReleaseRate();
}

void Envelope::setAttack(float seconds) {
    mAttackSeconds = seconds;
    updateAttackRate();
}

void Envelope::setDecay(float seconds) {
    mDecaySeconds = seconds;
    updateDecayRate();
}

void Envelope::setSustain(float level) {
    mSustainLevel = level;
    updateDecayRate();
    updateReleaseRate();
}

void Envelope::setRelease(float seconds) {
    mReleaseSeconds = seconds;
    updateReleaseRate();
}

void Envelope::updateAttackRate() {
    float samples = std::max(1.0f, mAttackSeconds * mSampleRate);
    mAttackRate = 1.0f / samples;
}

void Envelope::updateDecayRate() {
    float samples = std::max(1.0f, mDecaySeconds * mSampleRate);
    mDecayRate = (1.0f - mSustainLevel) / samples;
}

void Envelope::updateReleaseRate() {
    float samples = std::max(1.0f, mReleaseSeconds * mSampleRate);
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
            if (mCurrentLevel <= 0.0001f) {
                mCurrentLevel = 0.0f;
                mState = State::Idle;
            }
            break;
    }
    return mCurrentLevel;
}
