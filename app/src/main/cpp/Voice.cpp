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
}

void Voice::release() {
    mActive = false;
}

float Voice::nextSample() {
    if (!mActive) return 0.0f;
    return mOscillator.nextSample() * mVelocity;
}
