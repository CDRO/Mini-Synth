#ifndef MINI_SYNTH_ARPEGGIATOR_H
#define MINI_SYNTH_ARPEGGIATOR_H

#include <vector>
#include <atomic>
#include <mutex>
#include <algorithm>
#include <random>
#include "AudioCommon.h"
#include "VoiceManager.h"

class Arpeggiator {
public:
    Arpeggiator();

    void noteOn(int note);
    void noteOff(int note);
    void setParams(const EngineParams& params);

    void process(int32_t numFrames, int32_t samplesPerBeat, int trackId, VoiceManager& vm);
    void stop(VoiceManager& vm);

private:
    std::mutex mNoteMutex;
    std::vector<int> mHeldNotes;
    std::vector<int> mArpNotes;

    ArpMode mMode = ArpMode::Off;
    float mDivision = 0.25f;
    int mOctaveRange = 1;

    int mCurrentStep = 0;
    int mPlayingNote = -1;
    bool mDirectionUp = true;
    int32_t mSamplesProcessed = 0;

    std::mt19937 mRng;

    void updateArpNotes();
    void advance(int trackId, VoiceManager& vm);
};

#endif //MINI_SYNTH_ARPEGGIATOR_H
