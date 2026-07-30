#ifndef MINI_SYNTH_VOICE_H
#define MINI_SYNTH_VOICE_H

#include "Oscillator.h"
#include "Envelope.h"

class Voice {
public:
    Voice() : mActive(false), mNote(0) {}

    void setSampleRate(int32_t sampleRate) {
        mOscillator.setSampleRate(sampleRate);
        mEnvelope.setSampleRate(sampleRate);
    }
    void setWaveform(Waveform waveform) { mOscillator.setWaveform(waveform); }

    void trigger(int note, float velocity);
    void release();

    bool isActive() const { return mEnvelope.isActive(); }
    int getNote() const { return mNote; }

    void setAttack(float seconds) { mEnvelope.setAttack(seconds); }
    void setDecay(float seconds) { mEnvelope.setDecay(seconds); }
    void setSustain(float level) { mEnvelope.setSustain(level); }
    void setRelease(float seconds) { mEnvelope.setRelease(seconds); }

    float nextSample();

private:
    Oscillator mOscillator;
    Envelope mEnvelope;
    bool mActive;
    int mNote;
    float mVelocity;
};

#endif //MINI_SYNTH_VOICE_H
