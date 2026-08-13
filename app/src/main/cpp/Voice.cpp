#include "Voice.h"
#include <cmath>
#include <algorithm>

static double midiToFreq(int midiNote) {
    return 440.0 * pow(2.0, (midiNote - 69) / 12.0);
}

void Voice::trigger(int note, float velocity, const std::vector<float>* sampleBuffer) {
    mNote = note;
    mVelocity = velocity;
    if (sampleBuffer) {
        mIsSampleMode = true;
        mSamplePlayer.trigger(*sampleBuffer);
    } else {
        mIsSampleMode = false;
        mOscillator.setFrequency(midiToFreq(note));
        mTargetAftertouch = 0.0f;
        mCurrentAftertouch = 0.0f;
        mEnvelope.trigger();
    }
    mActive = true;
}

void Voice::release() {
    mActive = false;
    if (mIsSampleMode) {
        mSamplePlayer.stop();
    } else {
        mEnvelope.release();
    }
}

void Voice::nextSample(float& left, float& right) {
    if (!isActive()) {
        left = 0.0f;
        right = 0.0f;
        return;
    }

    // Simple Parameter Smoothing (approx 5-10ms ramp)
    mCurrentPitchBend = mCurrentPitchBend * 0.995f + mTargetPitchBend * 0.005f;
    mCurrentModulation = mCurrentModulation * 0.995f + mTargetModulation * 0.005f;
    mCurrentAftertouch = mCurrentAftertouch * 0.99f + mTargetAftertouch * 0.01f;

    float monoSample = 0.0f;
    if (mIsSampleMode) {
        float rate = powf(2.0f, mCurrentPitchBend / 12.0f);
        mSamplePlayer.setPlaybackRate(rate);
        monoSample = mSamplePlayer.nextSample() * mVelocity;
    } else {
        float lfoVal = mLfo.nextValue();
        float modPitch = 0.0f;
        float modVolume = 1.0f;
        float modFilter = 0.0f;

        // Base modulation (Mod Wheel)
        float effectiveLfoDepth = mLfo.getDepth() + (mCurrentModulation * 0.5f);

        // Apply Aftertouch to specific target
        float atVolume = 0.0f;
        float atPitch = 0.0f;
        float atFilter = 0.0f;

        switch (mAftertouchTarget) {
            case LfoTarget::Pitch:
                atPitch = mCurrentAftertouch * 2.0f; // +/- 2 semitones
                break;
            case LfoTarget::Volume:
                atVolume = mCurrentAftertouch * 0.5f; // +50% gain
                break;
            case LfoTarget::Filter:
                atFilter = mCurrentAftertouch * 4.0f; // +4 octaves
                break;
        }

        switch (mLfoTarget) {
            case LfoTarget::Pitch:
                modPitch = lfoVal * effectiveLfoDepth;
                break;
            case LfoTarget::Volume:
                modVolume = 1.0f + (lfoVal * effectiveLfoDepth);
                break;
            case LfoTarget::Filter:
                modFilter = lfoVal * 5.0f * effectiveLfoDepth;
                break;
        }

        // Combine all modulations
        float totalPitchShift = mCurrentPitchBend + modPitch + atPitch;
        mOscillator.setFrequency(midiToFreq(mNote) * pow(2.0, totalPitchShift / 12.0));

        float filterShift = modFilter + (mCurrentModulation * 2.0f) + atFilter;
        mFilter.setCutoff(mBaseCutoff * powf(2.0f, filterShift));

        monoSample = mOscillator.nextSample() * mVelocity * mEnvelope.nextLevel() * (modVolume + atVolume);
        monoSample = mFilter.process(monoSample);
    }

    // Equal Power Panning for individual Voice
    float angle = (mPanning + 1.0f) * (PI_F / 4.0f);
    left = monoSample * cosf(angle);
    right = monoSample * sinf(angle);
}
