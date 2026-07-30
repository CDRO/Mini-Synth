#ifndef MINI_SYNTH_VOICEMANAGER_H
#define MINI_SYNTH_VOICEMANAGER_H

#include "Voice.h"
#include <vector>
#include <atomic>

struct AdsrParams {
    std::atomic<float> attack{0.1f};
    std::atomic<float> decay{0.1f};
    std::atomic<float> sustain{0.8f};
    std::atomic<float> release{0.1f};
};

class VoiceManager {
public:
    VoiceManager();

    void setSampleRate(int32_t sampleRate);
    void setWaveform(Waveform waveform);
    void setPolyphonic(bool isPolyphonic);

    void noteOn(int midiNote, float velocity);
    void noteOff(int midiNote);

    void setAttack(float seconds) { mParams.attack = seconds; }
    void setDecay(float seconds) { mParams.decay = seconds; }
    void setSustain(float level) { mParams.sustain = level; }
    void setRelease(float seconds) { mParams.release = seconds; }

    float nextSample();

private:
    static const int MAX_VOICES = 16;
    Voice mVoices[MAX_VOICES];
    bool mIsPolyphonic = true;
    Waveform mCurrentWaveform = Waveform::Sine;
    int32_t mSampleRate = 48000;
    int mLastStealIndex = 0;

    AdsrParams mParams;

    int findFreeVoice();
    int findVoiceByNote(int midiNote);
};

#endif //MINI_SYNTH_VOICEMANAGER_H
