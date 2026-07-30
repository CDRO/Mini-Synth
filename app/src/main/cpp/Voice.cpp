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
    return mOscillator.nextSample() * mVelocity * mEnvelope.nextLevel();
}
