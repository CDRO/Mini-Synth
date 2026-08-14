#ifndef MINI_SYNTH_VOICE_H
#define MINI_SYNTH_VOICE_H

#include "Oscillator.h"
#include "SamplePlayer.h"
#include "Envelope.h"
#include "Lfo.h"
#include "Filter.h"

class Voice {
public:
    Voice() : mActive(false), mNote(0), mIsSampleMode(false) {}

    void setSampleRate(int32_t sampleRate) {
        for (int i = 0; i < MAX_UNISON; ++i) mOscillators[i].setSampleRate(sampleRate);
        mEnvelope.setSampleRate(sampleRate);
        mLfo.setSampleRate(sampleRate);
        mFilter.setSampleRate(sampleRate);
    }
    void setWaveform(Waveform waveform) {
        for (int i = 0; i < MAX_UNISON; ++i) mOscillators[i].setWaveform(waveform);
    }

    void trigger(int note, float velocity, const std::vector<float>* sampleBuffer = nullptr);
    void release();

    void setSampleMode(bool enabled) { mIsSampleMode = enabled; }
    void setSampleLooping(bool looping) { mSamplePlayer.setLooping(looping); }
    SamplePlayer& getSamplePlayer() { return mSamplePlayer; }

    bool isActive() const { return mEnvelope.isActive() || mSamplePlayer.isActive(); }
    int getNote() const { return mNote; }

    void setAttack(float seconds) { mEnvelope.setAttack(seconds); }
    void setDecay(float seconds) { mEnvelope.setDecay(seconds); }
    void setSustain(float level) { mEnvelope.setSustain(level); }
    void setRelease(float seconds) { mEnvelope.setRelease(seconds); }

    void setLfoRate(float frequency) { mLfo.setFrequency(frequency); }
    void setLfoDepth(float depth) { mLfo.setDepth(depth); }
    void setLfoWaveform(Waveform waveform) { mLfo.setWaveform(waveform); }
    void setLfoTarget(LfoTarget target) { mLfoTarget = target; }
    void setAftertouchTarget(LfoTarget target) { mAftertouchTarget = target; }

    void setFilterCutoff(float frequency) { mBaseCutoff = frequency; mFilter.setCutoff(frequency); }
    void setFilterResonance(float resonance) { mFilter.setResonance(resonance); }

    void setPitchBend(float semitones) { mTargetPitchBend = semitones; }
    void setModulation(float amount) { mTargetModulation = amount; }
    void setAftertouch(float amount) { mTargetAftertouch = amount; }
    void setPanning(float panning) { mPanning = panning; }
    void setPhaseDistortion(float pd) { mBasePhaseDistortion = pd; }
    void setUnison(int count, float detune, float spread) {
        mUnisonCount = count;
        mUnisonDetune = detune;
        mUnisonSpread = spread;
        mUnisonNorm = 1.0f / sqrtf(static_cast<float>(std::max(1, count)));
    }
    void setMorph(float morph) { mMorph = morph; }
    void setWavetable(const float* data, int32_t size);

    void nextSample(float& left, float& right);

private:
    static constexpr int MAX_UNISON = 8;
    Oscillator mOscillators[MAX_UNISON];
    SamplePlayer mSamplePlayer;
    Envelope mEnvelope;
    Lfo mLfo;
    Filter mFilter;
    LfoTarget mLfoTarget = LfoTarget::Pitch;
    LfoTarget mAftertouchTarget = LfoTarget::Filter;
    float mBaseCutoff = 1000.0f;
    float mTargetPitchBend = 0.0f;
    float mCurrentPitchBend = 0.0f;
    float mTargetModulation = 0.0f;
    float mCurrentModulation = 0.0f;
    float mTargetAftertouch = 0.0f;
    float mCurrentAftertouch = 0.0f;
    float mPanning = 0.0f;
    float mBasePhaseDistortion = 0.0f;
    int mUnisonCount = 1;
    float mUnisonDetune = 0.0f;
    float mUnisonSpread = 0.0f;
    float mUnisonNorm = 1.0f;
    float mMorph = 0.0f;
    bool mActive;
    bool mIsSampleMode;
    int mNote;
    float mVelocity;
};

#endif //MINI_SYNTH_VOICE_H
