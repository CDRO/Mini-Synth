#ifndef MINI_SYNTH_VOICE_H
#define MINI_SYNTH_VOICE_H

#include "Oscillator.h"

class Voice {
public:
    Voice() : mActive(false), mNote(0) {}

    void setSampleRate(int32_t sampleRate) { mOscillator.setSampleRate(sampleRate); }
    void setWaveform(Waveform waveform) { mOscillator.setWaveform(waveform); }

    void trigger(int note, float velocity);
    void release();

    bool isActive() const { return mActive; }
    int getNote() const { return mNote; }

    float nextSample();

private:
    Oscillator mOscillator;
    bool mActive;
    int mNote;
    float mVelocity;
};

#endif //MINI_SYNTH_VOICE_H
