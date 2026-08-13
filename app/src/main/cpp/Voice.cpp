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
        double freq = midiToFreq(note);
        for (int i = 0; i < MAX_UNISON; ++i) {
            mOscillators[i].setFrequency(freq);
        }
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
        float filterShift = modFilter + (mCurrentModulation * 2.0f) + atFilter;
        mFilter.setCutoff(mBaseCutoff * powf(2.0f, filterShift));

        float env = mEnvelope.nextLevel();
        float baseFreq = midiToFreq(mNote);

        // Sum unison oscillators
        float combinedL = 0.0f;
        float combinedR = 0.0f;
        int count = std::max(1, std::min(mUnisonCount, MAX_UNISON));

        for (int i = 0; i < count; ++i) {
            float detune = 0.0f;
            float panOffset = 0.0f;

            if (count > 1) {
                // Calculate detune and pan offset for this unison voice
                float ratio = static_cast<float>(i) / (count - 1); // 0.0 to 1.0
                detune = (ratio * 2.0f - 1.0f) * mUnisonDetune; // -Detune to +Detune cents
                panOffset = (ratio * 2.0f - 1.0f) * mUnisonSpread; // -Spread to +Spread
            }

            double freq = baseFreq * pow(2.0, (totalPitchShift + (detune / 100.0)) / 12.0);
            mOscillators[i].setFrequency(freq);

            float s = mOscillators[i].nextSample();

            // Individual Panning for this unison voice (Spread)
            // Combine with master panning
            float voicePan = std::max(-1.0f, std::min(mPanning + panOffset, 1.0f));
            float angle = (voicePan + 1.0f) * (PI_F / 4.0f);

            combinedL += s * cosf(angle);
            combinedR += s * sinf(angle);
        }

        // Normalize sum to prevent volume explosion
        float norm = 1.0f / sqrtf(static_cast<float>(count));
        left = mFilter.process(combinedL * mVelocity * env * (modVolume + atVolume) * norm);
        right = mFilter.process(combinedR * mVelocity * env * (modVolume + atVolume) * norm);
        return;
    }

    // Equal Power Panning for individual Voice
    float angle = (mPanning + 1.0f) * (PI_F / 4.0f);
    left = monoSample * cosf(angle);
    right = monoSample * sinf(angle);
}
