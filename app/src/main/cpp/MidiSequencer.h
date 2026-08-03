#ifndef MINI_SYNTH_MIDISEQUENCER_H
#define MINI_SYNTH_MIDISEQUENCER_H

#include <stdint.h>
#include <vector>
#include <bitset>
#include <atomic>
#include "VoiceManager.h"

class MidiSequencer {
public:
    static const int NUM_STEPS = 16;
    static const int NUM_NOTES = 128;

    MidiSequencer();

    void setNote(int step, int note, bool active);
    bool getNote(int step, int note) const;
    void clear();

    void setStepDuration(float division); // e.g., 0.25 for 1/16th notes if beat is 1/4
    void setVelocity(float velocity) { mVelocity.store(velocity); }
    void process(int32_t numFrames, int32_t samplesPerBeat, VoiceManager& voiceManager);

    int getCurrentStep() const { return mCurrentStep.load(); }
    void setPlaying(bool playing, VoiceManager& voiceManager) {
        mIsPlaying.store(playing);
        if (!playing) stop(voiceManager);
    }
    bool isPlaying() const { return mIsPlaying.load(); }

private:
    std::bitset<NUM_NOTES> mGrid[NUM_STEPS];
    std::atomic<int> mCurrentStep{0};
    int32_t mSamplesProcessed = 0;
    std::atomic<float> mStepDivision{0.25f}; // Default to 1/16th notes (4 steps per beat)
    std::atomic<bool> mIsPlaying{false};
    std::atomic<float> mVelocity{0.8f};

    void stop(VoiceManager& voiceManager);
    void reset();
    void triggerStep(int step, VoiceManager& voiceManager);
    void releaseStep(int step, VoiceManager& voiceManager);
};

#endif //MINI_SYNTH_MIDISEQUENCER_H
