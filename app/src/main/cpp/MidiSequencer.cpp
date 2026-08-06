#include "MidiSequencer.h"
#include <cstring>

MidiSequencer::MidiSequencer() {
    clear();
    mActiveNoteTracking[0].store(0);
    mActiveNoteTracking[1].store(0);
}

void MidiSequencer::setNote(int step, int note, bool active) {
    if (step >= 0 && step < MAX_STEPS && note >= 0 && note < NUM_NOTES) {
        int word = note / 64;
        int bit = note % 64;
        uint64_t mask = 1ULL << bit;
        if (active) {
            mGrid[step][word].fetch_or(mask);
        } else {
            mGrid[step][word].fetch_and(~mask);
        }
    }
}

bool MidiSequencer::getNote(int step, int note) const {
    if (step >= 0 && step < MAX_STEPS && note >= 0 && note < NUM_NOTES) {
        int word = note / 64;
        int bit = note % 64;
        return (mGrid[step][word].load() & (1ULL << bit)) != 0;
    }
    return false;
}

void MidiSequencer::getActiveNotes(int step, std::vector<int>& outNotes) const {
    outNotes.clear();
    if (step >= 0 && step < MAX_STEPS) {
        for (int word = 0; word < 2; ++word) {
            uint64_t val = mGrid[step][word].load();
            for (int bit = 0; bit < 64; ++bit) {
                if (val & (1ULL << bit)) outNotes.push_back(word * 64 + bit);
            }
        }
    }
}

void MidiSequencer::clear() {
    for (int i = 0; i < MAX_STEPS; ++i) {
        mGrid[i][0].store(0);
        mGrid[i][1].store(0);
    }
    reset();
}

void MidiSequencer::setStepDuration(float division) {
    if (division < 0.01f) division = 0.01f;
    if (division > 100.0f) division = 100.0f;
    mStepDivision.store(division);
}

int MidiSequencer::recordNote(int note) {
    if (note < 0 || note >= NUM_NOTES) return mCurrentStep.load();
    int step = mCurrentStep.load();
    int totalSteps = mNumSteps.load();

    mGrid[step][0].store(0);
    mGrid[step][1].store(0);
    setNote(step, note, true);

    int nextStep = step;
    if (!mIsPlaying.load()) {
        nextStep = (step + 1) % totalSteps;
        mCurrentStep.store(nextStep);
    }
    return nextStep;
}

void MidiSequencer::handleRealTimeNoteOn(int note) {
    if (note < 0 || note >= NUM_NOTES) return;
    if (!mIsPlaying.load() || !mIsRecording.load()) return;

    int current = mCurrentStep.load();
    int samples = mSamplesProcessed.load();
    int total = mLastStepDuration.load();
    int totalSteps = mNumSteps.load();

    int targetStep = current;
    if (mInputQuantize.load() && total > 0) {
        if (samples > (total / 2)) {
            targetStep = (current + 1) % totalSteps;
        }
    }

    if (!mIsOverdub.load()) {
        mGrid[targetStep][0].store(0);
        mGrid[targetStep][1].store(0);
    }
    setNote(targetStep, note, true);

    int word = note / 64;
    int bit = note % 64;
    mActiveNoteTracking[word].fetch_or(1ULL << bit);
}

void MidiSequencer::handleRealTimeNoteOff(int note) {
    if (note < 0 || note >= NUM_NOTES) return;
    int word = note / 64;
    int bit = note % 64;
    mActiveNoteTracking[word].fetch_and(~(1ULL << bit));
}

void MidiSequencer::stop(VoiceManager& voiceManager) {
    releaseStep(mCurrentStep, voiceManager);
    reset();
    mActiveNoteTracking[0].store(0);
    mActiveNoteTracking[1].store(0);
}

void MidiSequencer::reset() {
    mSamplesProcessed.store(0);
    mCurrentStep.store(0);
}

void MidiSequencer::process(int32_t numFrames, int32_t samplesPerBeat, VoiceManager& voiceManager) {
    if (!mIsPlaying.load()) return;

    float currentDivision = mStepDivision.load();
    int32_t stepDurationSamples = static_cast<int32_t>(static_cast<float>(samplesPerBeat) * currentDivision);
    mLastStepDuration.store(stepDurationSamples);

    int32_t gateSamples = static_cast<int32_t>(static_cast<float>(stepDurationSamples) * 0.9f);
    int32_t samplesCounter = mSamplesProcessed.load();
    int totalSteps = mNumSteps.load();

    for (int i = 0; i < numFrames; ++i) {
        if (samplesCounter == 0) {
            triggerStep(mCurrentStep, voiceManager);

            if (mIsRecording.load()) {
                uint64_t track0 = mActiveNoteTracking[0].load();
                uint64_t track1 = mActiveNoteTracking[1].load();
                if (track0 != 0) mGrid[mCurrentStep][0].fetch_or(track0);
                if (track1 != 0) mGrid[mCurrentStep][1].fetch_or(track1);
            }
        }

        samplesCounter++;

        if (samplesCounter == gateSamples) {
            releaseStep(mCurrentStep, voiceManager);
        }

        if (samplesCounter >= stepDurationSamples) {
            samplesCounter = 0;
            mCurrentStep = (mCurrentStep + 1) % totalSteps;
        }
    }
    mSamplesProcessed.store(samplesCounter);
}

void MidiSequencer::triggerStep(int step, VoiceManager& voiceManager) {
    float vel = mVelocity.load();
    for (int word = 0; word < 2; ++word) {
        uint64_t val = mGrid[step][word].load();
        if (val == 0) continue;
        for (int bit = 0; bit < 64; ++bit) {
            if (val & (1ULL << bit)) {
                voiceManager.noteOn(word * 64 + bit, vel);
            }
        }
    }
}

void MidiSequencer::releaseStep(int step, VoiceManager& voiceManager) {
    for (int word = 0; word < 2; ++word) {
        uint64_t val = mGrid[step][word].load();
        if (val == 0) continue;
        for (int bit = 0; bit < 64; ++bit) {
            if (val & (1ULL << bit)) {
                voiceManager.noteOff(word * 64 + bit);
            }
        }
    }
}
