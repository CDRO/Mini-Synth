#ifndef MINI_SYNTH_ENVELOPE_H
#define MINI_SYNTH_ENVELOPE_H

#include <stdint.h>

class Envelope {
public:
    enum class State {
        Idle,
        Attack,
        Decay,
        Sustain,
        Release
    };

    Envelope();

    void setSampleRate(int32_t sampleRate);
    void setAttack(float seconds);
    void setDecay(float seconds);
    void setSustain(float level);
    void setRelease(float seconds);

    void trigger();
    void release();

    float nextLevel();
    bool isActive() const { return mState != State::Idle; }

private:
    State mState = State::Idle;
    float mCurrentLevel = 0.0f;
    int32_t mSampleRate = 48000;

    float mAttackRate = 0.001f;
    float mDecayRate = 0.001f;
    float mSustainLevel = 0.8f;
    float mReleaseRate = 0.001f;

    void updateRates();
};

#endif //MINI_SYNTH_ENVELOPE_H
