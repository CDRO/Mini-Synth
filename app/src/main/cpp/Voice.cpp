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

float Voice::nextSample() {
    if (!isActive()) return 0.0f;

    // Simple Parameter Smoothing (approx 5-10ms ramp)
    mCurrentPitchBend = mCurrentPitchBend * 0.995f + mTargetPitchBend * 0.005f;
    mCurrentModulation = mCurrentModulation * 0.995f + mTargetModulation * 0.005f;
    mCurrentAftertouch = mCurrentAftertouch * 0.99f + mTargetAftertouch * 0.01f;

    if (mIsSampleMode) {
        // Pitch bend for samples (change playback rate)
        float rate = powf(2.0f, mCurrentPitchBend / 12.0f);
        mSamplePlayer.setPlaybackRate(rate);
        return mSamplePlayer.nextSample() * mVelocity;
    }

    float lfoVal = mLfo.nextValue();
    float modPitch = 0.0f;
    float modVolume = 1.0f;
    float modFilter = 0.0f;

    // Modulation Wheel (Vertical Gesture) can add to LFO depth or change Cutoff directly
    // For now, let's map it to adding to LFO depth and direct Filter Cutoff offset
    float effectiveLfoDepth = mLfo.getDepth() + (mCurrentModulation * 0.5f);

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

    // Apply Pitch Bend (Horizontal Gesture)
    float totalPitchShift = mCurrentPitchBend + modPitch;
    mOscillator.setFrequency(midiToFreq(mNote) * pow(2.0, totalPitchShift / 12.0));

    // Apply Filter Modulation + direct Modulation influence + Aftertouch
    // Aftertouch can sweep up to 4 octaves
    float filterShift = modFilter + (mCurrentModulation * 2.0f) + (mCurrentAftertouch * 4.0f);
    mFilter.setCutoff(mBaseCutoff * powf(2.0f, filterShift));

    float sample = mOscillator.nextSample() * mVelocity * mEnvelope.nextLevel() * modVolume;
    return mFilter.process(sample);
}
