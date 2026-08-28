#ifndef MINI_SYNTH_MIDISEQUENCER_H
#define MINI_SYNTH_MIDISEQUENCER_H

#include <stdint.h>
#include <vector>
#include <bitset>
#include <atomic>
#include "VoiceManager.h"

class MidiSequencer {
public:
    static const int MAX_STEPS = 64;
    static const int NUM_NOTES = 128;
    static const int MAX_TRACKS = 4;

    MidiSequencer();

    void setNote(int track, int step, int note, bool active);
    bool getNote(int track, int step, int note) const;
    void getActiveNotes(int track, int step, std::vector<int>& outNotes) const;
    bool isStepActive(int track, int step) const {
        if (track >= 0 && track < MAX_TRACKS && step >= 0 && step < mNumSteps) {
            return mGrid[track][step][0].load() != 0 || mGrid[track][step][1].load() != 0;
        }
        return false;
    }
    void clear();
    void clearTrack(int track);

    const std::atomic<uint64_t>* getGridData(int track) const { return &mGrid[track][0][0]; }
    float getStepDivision() const { return mStepDivision.load(); }

    void setStepDuration(float division);
    void setVelocity(float velocity) { mVelocity.store(velocity); }
    void setInputQuantize(bool enabled) { mInputQuantize.store(enabled); }
    void setOverdub(bool enabled) { mIsOverdub.store(enabled); }
    void setNumSteps(int steps) { if (steps > 0 && steps <= MAX_STEPS) mNumSteps.store(steps); }
    int getNumSteps() const { return mNumSteps.load(); }
    int recordNote(int track, int note);
    void stepRecordNote(int track, int note);
    void stepRecordRest();
    void stepRecordBack();
    void handleRealTimeNoteOn(int track, int note);
    void handleRealTimeNoteOff(int track, int note);
    void process(int32_t numFrames, int32_t samplesPerBeat, VoiceManager& voiceManager);

    int getCurrentStep() const { return mCurrentStep.load(); }
    void setPlaying(bool playing, VoiceManager& voiceManager) {
        mIsPlaying.store(playing);
        if (!playing) stop(voiceManager);
    }
    bool isPlaying() const { return mIsPlaying.load(); }
    void setRecording(bool recording);
    bool isRecording() const { return mIsRecording.load(); }

private:
    std::atomic<uint64_t> mGrid[MAX_TRACKS][MAX_STEPS][2];
    std::atomic<int> mNumSteps{16};
    std::atomic<int> mCurrentStep{0};
    std::atomic<int32_t> mSamplesProcessed{0};
    std::atomic<int32_t> mLastStepDuration{4800}; // 48kHz / 120bpm * 60 * 0.25
    std::atomic<float> mStepDivision{0.25f}; // Default to 1/16th notes (4 steps per beat)
    std::atomic<bool> mIsPlaying{false};
    std::atomic<bool> mIsRecording{false};
    std::atomic<bool> mIsOverdub{true};
    std::atomic<bool> mInputQuantize{true};
    std::atomic<float> mVelocity{0.8f};
    std::atomic<uint64_t> mActiveNoteTracking[MAX_TRACKS][2];

    void stop(VoiceManager& voiceManager);
    void reset();
    void triggerStep(int step, VoiceManager& voiceManager);
    void releaseStep(int step, VoiceManager& voiceManager);
};

#endif //MINI_SYNTH_MIDISEQUENCER_H
