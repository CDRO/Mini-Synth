#ifndef MINI_SYNTH_VOICEMANAGER_H
#define MINI_SYNTH_VOICEMANAGER_H

#include "Voice.h"
#include "AudioCommon.h"
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

    // Global polyphonic mode (for now)
    void setPolyphonic(bool isPolyphonic);

    // noteOn now takes a trackId to apply track-specific parameters
    void noteOn(int midiNote, float velocity, const std::vector<float>* sampleBuffer = nullptr, float initialPan = 0.0f, int trackId = 0);
    void noteOff(int midiNote);
    void setPadLooping(int midiNote, bool looping);

    // Track parameter sync
    void updateTrackParams(int trackId, const EngineParams& params);

    void setMasterVolume(float volume) { mMasterVolume = volume; }
    void setVoiceAftertouch(int midiNote, float amount);
    void setBpm(float bpm);

    void nextSample(float& left, float& right);

private:
    static const int MAX_VOICES = 16;
    static const int MAX_TRACKS = 4;
    Voice mVoices[MAX_VOICES];
    int mVoiceTrackIds[MAX_VOICES]; // Which track is each voice playing?

    EngineParams mTrackParams[MAX_TRACKS];
    bool mIsPolyphonic = true;
    int32_t mSampleRate = 48000;
    int mLastStealIndex = 0;

    std::atomic<float> mMasterVolume{0.8f};

    int findFreeVoice();
    int findVoiceByNote(int midiNote);
};

#endif //MINI_SYNTH_VOICEMANAGER_H
