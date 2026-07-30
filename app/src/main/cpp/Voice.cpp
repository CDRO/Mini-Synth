#include "Voice.h"
#include <cmath>
#include <algorithm>

static double midiToFreq(int midiNote) {
    return 440.0 * pow(2.0, (midiNote - 69) / 12.0);
}

void Voice::trigger(int note, float velocity) {
    mNote = note;
    mVelocity = velocity;
    mOscillator.setFrequency(midiToFreq(note));
    mActive = true;
    mEnvelope.trigger();
}

void Voice::release() {
    mActive = false;
    mEnvelope.release();
}

float Voice::nextSample() {
    if (!mEnvelope.isActive()) return 0.0f;

    float lfoVal = mLfo.nextValue();
    float modPitch = 0.0f;
    float modVolume = 1.0f;
    float modFilter = 0.0f;

    switch (mLfoTarget) {
        case LfoTarget::Pitch:
            modPitch = lfoVal * 1.0f;
            break;
        case LfoTarget::Volume:
            modVolume = 1.0f + lfoVal;
            break;
        case LfoTarget::Filter:
            // Modulate cutoff by +/- 5 octaves
            modFilter = lfoVal * 5.0f;
            break;
    }

    if (modPitch != 0.0f) {
        mOscillator.setFrequency(midiToFreq(mNote) * pow(2.0, modPitch / 12.0));
    } else {
        mOscillator.setFrequency(midiToFreq(mNote));
    }

    if (modFilter != 0.0f) {
        mFilter.setCutoff(mBaseCutoff * powf(2.0f, modFilter));
    } else {
        mFilter.setCutoff(mBaseCutoff);
    }

    float sample = mOscillator.nextSample() * mVelocity * mEnvelope.nextLevel() * modVolume;
    return mFilter.process(sample);
}
