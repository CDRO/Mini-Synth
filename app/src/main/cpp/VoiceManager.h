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

struct EngineParams {
    Waveform waveform;
    float attack, decay, sustain, release;
    float masterVolume;
    float lfoRate, lfoDepth;
    Waveform lfoWaveform;
    LfoTarget lfoTarget;
    float filterCutoff, filterResonance;
    bool isPolyphonic;
    float pitchBend;
    float modulation;
    float panning;
};

class VoiceManager {
public:
    VoiceManager();

    void setSampleRate(int32_t sampleRate);
    void setWaveform(Waveform waveform);
    void setPolyphonic(bool isPolyphonic);

    void noteOn(int midiNote, float velocity, const std::vector<float>* sampleBuffer = nullptr, float initialPan = 0.0f);
    void noteOff(int midiNote);
    void setPadLooping(int midiNote, bool looping);

    void setAttack(float seconds) { mParams.attack = seconds; }
    void setDecay(float seconds) { mParams.decay = seconds; }
    void setSustain(float level) { mParams.sustain = level; }
    void setRelease(float seconds) { mParams.release = seconds; }

    void setMasterVolume(float volume) { mMasterVolume = volume; }
    void setPanning(float panning) { mPanning = panning; mParamsChanged = true; }

    void setLfoRate(float frequency) { mLfoRate = frequency; mParamsChanged = true; }
    void setLfoDepth(float depth) { mLfoDepth = depth; mParamsChanged = true; }
    void setLfoWaveform(Waveform waveform) { mLfoWaveform = waveform; mParamsChanged = true; }
    void setLfoTarget(LfoTarget target) { mLfoTarget = target; mParamsChanged = true; }
    void setAftertouchTarget(LfoTarget target) { mAftertouchTarget = target; mParamsChanged = true; }

    void setFilterCutoff(float frequency) { mFilterCutoff = frequency; mParamsChanged = true; }
    void setFilterResonance(float resonance) { mFilterResonance = resonance; mParamsChanged = true; }

    void setPitchBend(float semitones) { mPitchBend = semitones; mParamsChanged = true; }
    void setModulation(float amount) { mModulation = amount; mParamsChanged = true; }
    void setVoiceAftertouch(int midiNote, float amount);

    EngineParams getParams() const;
    void setParams(const EngineParams& params);

    void nextSample(float& left, float& right);

private:
    static const int MAX_VOICES = 16;
    Voice mVoices[MAX_VOICES];
    bool mIsPolyphonic = true;
    Waveform mCurrentWaveform = Waveform::Sine;
    int32_t mSampleRate = 48000;
    int mLastStealIndex = 0;

    AdsrParams mParams;
    std::atomic<float> mMasterVolume{0.8f};

    std::atomic<float> mLfoRate{1.0f};
    std::atomic<float> mLfoDepth{0.0f};
    std::atomic<Waveform> mLfoWaveform{Waveform::Sine};
    std::atomic<LfoTarget> mLfoTarget{LfoTarget::Pitch};
    std::atomic<LfoTarget> mAftertouchTarget{LfoTarget::Filter};
    std::atomic<float> mFilterCutoff{1000.0f};
    std::atomic<float> mFilterResonance{0.5f};
    std::atomic<float> mPitchBend{0.0f};
    std::atomic<float> mModulation{0.0f};
    std::atomic<float> mPanning{0.0f}; // -1.0 to 1.0
    std::atomic<bool> mParamsChanged{false};

    int findFreeVoice();
    int findVoiceByNote(int midiNote);
};

#endif //MINI_SYNTH_VOICEMANAGER_H
