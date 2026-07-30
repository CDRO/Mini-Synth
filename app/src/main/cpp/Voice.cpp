#include "Voice.h"
#include <cmath>

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

    switch (mLfoTarget) {
        case LfoTarget::Pitch:
            // Vibrato: +/- 1 semitone max depth
            modPitch = lfoVal * 1.0f;
            break;
        case LfoTarget::Volume:
            // Tremolo: Modulate gain
            modVolume = 1.0f + lfoVal; // lfoVal is [-depth, depth]
            break;
        case LfoTarget::Filter:
            // Placeholder
            break;
    }

    if (modPitch != 0.0f) {
        // frequency * 2^(semitones/12)
        mOscillator.setFrequency(midiToFreq(mNote) * pow(2.0, modPitch / 12.0));
    }

    float sample = mOscillator.nextSample() * mVelocity * mEnvelope.nextLevel() * modVolume;
    return sample;
}
