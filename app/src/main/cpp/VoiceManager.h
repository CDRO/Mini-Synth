#ifndef MINI_SYNTH_VOICEMANAGER_H
#define MINI_SYNTH_VOICEMANAGER_H

#include "Voice.h"
#include <vector>

class VoiceManager {
public:
    VoiceManager();

    void setSampleRate(int32_t sampleRate);
    void setWaveform(Waveform waveform);
    void setPolyphonic(bool isPolyphonic);

    void noteOn(int midiNote, float velocity);
    void noteOff(int midiNote);

    float nextSample();

private:
    static const int MAX_VOICES = 16;
    Voice mVoices[MAX_VOICES];
    bool mIsPolyphonic = true;
    Waveform mCurrentWaveform = Waveform::Sine;
    int32_t mSampleRate = 48000;
    int mLastStealIndex = 0;

    int findFreeVoice();
    int findVoiceByNote(int midiNote);
};

#endif //MINI_SYNTH_VOICEMANAGER_H
