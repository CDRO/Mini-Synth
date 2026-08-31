#include "Voice.h"
#include <cmath>
#include <algorithm>

#ifndef PI_F
#define PI_F 3.1415926535f
#endif

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

void Voice::setWavetable(const float* data, int32_t size) {
    for (auto& osc : mOscillators) {
        osc.setWavetable(data, size);
    }
}

void Voice::nextSample(float& left, float& right) {
    if (!isActive()) {
        left = 0.0f;
        right = 0.0f;
        return;
    }

    mCurrentPitchBend = mCurrentPitchBend * 0.995f + mTargetPitchBend * 0.005f;
    mCurrentModulation = mCurrentModulation * 0.995f + mTargetModulation * 0.005f;
    mCurrentAftertouch = mCurrentAftertouch * 0.99f + mTargetAftertouch * 0.01f;

    float monoSample = 0.0f;
    if (mIsSampleMode) {
        float rate = powf(2.0f, mCurrentPitchBend / 12.0f);
        mSamplePlayer.setPlaybackRate(rate);
        monoSample = mSamplePlayer.nextSample() * mVelocity;
    } else {
        float effectiveLfoDepth = mLfo.getDepth() + (mCurrentModulation * 0.5f);
        float lfoVal = mLfo.nextValue();
        float lfoMod = lfoVal * effectiveLfoDepth;

        float atVolume = 0.0f, atPitch = 0.0f, atFilter = 0.0f, atPD = 0.0f;
        switch (mAftertouchTarget) {
            case LfoTarget::Pitch: atPitch = mCurrentAftertouch * 2.0f; break;
            case LfoTarget::Volume: atVolume = mCurrentAftertouch * 0.5f; break;
            case LfoTarget::Filter: atFilter = mCurrentAftertouch * 4.0f; break;
            case LfoTarget::PhaseDistortion: atPD = mCurrentAftertouch * 0.8f; break;
        }

        float modPitch = lfoMod * mLfoMatrix[0];
        float modVolume = 1.0f + (lfoMod * mLfoMatrix[1]);
        float modFilter = lfoMod * 5.0f * mLfoMatrix[2];
        float modPD = lfoMod * mLfoMatrix[3];

        float totalPitchShift = mCurrentPitchBend + modPitch + atPitch;
        float filterShift = modFilter + (mCurrentModulation * 2.0f) + atFilter;
        mFilter.setCutoff(mBaseCutoff * powf(2.0f, filterShift));

        float effectivePD = std::max(0.0f, std::min(mBasePhaseDistortion + modPD + atPD, 0.99f));
        float env = mEnvelope.nextLevel();
        float baseFreq = midiToFreq(mNote);

        float combinedL = 0.0f;
        float combinedR = 0.0f;
        int count = std::max(1, std::min(mUnisonCount, MAX_UNISON));

        for (int i = 0; i < count; ++i) {
            float detune = 0.0f, panOffset = 0.0f;
            if (count > 1) {
                float ratio = static_cast<float>(i) / (count - 1);
                detune = (ratio * 2.0f - 1.0f) * mUnisonDetune;
                panOffset = (ratio * 2.0f - 1.0f) * mUnisonSpread;
            }

            double freq = baseFreq * pow(2.0, (totalPitchShift + (detune / 100.0)) / 12.0);
            mOscillators[i].setFrequency(freq);
            mOscillators[i].setMorph(mMorph);
            mOscillators[i].setPhaseDistortion(effectivePD);

            float s = mOscillators[i].nextSample();
            float angle = (std::max(-1.0f, std::min(mPanning + panOffset, 1.0f)) + 1.0f) * (PI_F / 4.0f);
            combinedL += s * cosf(angle);
            combinedR += s * sinf(angle);
        }

        left = mFilter.process(combinedL * mVelocity * env * (modVolume + atVolume) * mUnisonNorm);
        right = mFilter.process(combinedR * mVelocity * env * (modVolume + atVolume) * mUnisonNorm);
        return;
    }

    float angle = (mPanning + 1.0f) * (PI_F / 4.0f);
    left = monoSample * cosf(angle);
    right = monoSample * sinf(angle);
}
