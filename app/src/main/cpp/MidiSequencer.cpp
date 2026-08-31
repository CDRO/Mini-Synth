#include "MidiSequencer.h"
#include <cstring>
#include <algorithm>

MidiSequencer::MidiSequencer() {
    clear();
    for (int t = 0; t < MAX_TRACKS; ++t) {
        mActiveNoteTracking[t][0].store(0);
        mActiveNoteTracking[t][1].store(0);
        for (int s = 0; s < MAX_STEPS; ++s) mTieGrid[t][s].store(false);
    }
}

void MidiSequencer::setNote(int track, int step, int note, bool active) {
    if (track >= 0 && track < MAX_TRACKS && step >= 0 && step < MAX_STEPS && note >= 0 && note < NUM_NOTES) {
        int word = note / 64;
        int bit = note % 64;
        uint64_t mask = 1ULL << bit;
        if (active) {
            mGrid[track][step][word].fetch_or(mask);
        } else {
            mGrid[track][step][word].fetch_and(~mask);
        }
    }
}

bool MidiSequencer::getNote(int track, int step, int note) const {
    if (track >= 0 && track < MAX_TRACKS && step >= 0 && step < MAX_STEPS && note >= 0 && note < NUM_NOTES) {
        int word = note / 64;
        int bit = note % 64;
        return (mGrid[track][step][word].load() & (1ULL << bit)) != 0;
    }
    return false;
}

void MidiSequencer::getActiveNotes(int track, int step, std::vector<int>& outNotes) const {
    outNotes.clear();
    if (track >= 0 && track < MAX_TRACKS && step >= 0 && step < MAX_STEPS) {
        for (int word = 0; word < 2; ++word) {
            uint64_t val = mGrid[track][step][word].load();
            for (int bit = 0; bit < 64; ++bit) {
                if (val & (1ULL << bit)) outNotes.push_back(word * 64 + bit);
            }
        }
    }
}

void MidiSequencer::clear() {
    for (int t = 0; t < MAX_TRACKS; ++t) {
        clearTrack(t);
    }
    reset();
}

void MidiSequencer::clearTrack(int track) {
    if (track < 0 || track >= MAX_TRACKS) return;
    for (int i = 0; i < MAX_STEPS; ++i) {
        mGrid[track][i][0].store(0);
        mGrid[track][i][1].store(0);
        mTieGrid[track][i].store(false);
    }
}

void MidiSequencer::setRecording(bool recording) {
    mIsRecording.store(recording);
    if (!recording) {
        for (int t = 0; t < MAX_TRACKS; ++t) {
            mActiveNoteTracking[t][0].store(0);
            mActiveNoteTracking[t][1].store(0);
        }
    }
}

void MidiSequencer::setStepDuration(float division) {
    mStepDivision.store(std::max(0.01f, std::min(division, 100.0f)));
}

int MidiSequencer::recordNote(int track, int note) {
    if (note < 0 || note >= NUM_NOTES || track < 0 || track >= MAX_TRACKS) return mCurrentStep.load();
    stepRecordNote(track, note);
    return mCurrentStep.load();
}

void MidiSequencer::stepRecordNote(int track, int note) {
    if (note < 0 || note >= NUM_NOTES || track < 0 || track >= MAX_TRACKS) return;
    int step = mCurrentStep.load();
    int totalSteps = mNumSteps.load();

    mGrid[track][step][0].store(0);
    mGrid[track][step][1].store(0);
    mTieGrid[track][step].store(false); // New note clears tie
    setNote(track, step, note, true);

    mCurrentStep.store((step + 1) % totalSteps);
}

void MidiSequencer::stepRecordRest(int track) {
    if (track < 0 || track >= MAX_TRACKS) return;
    int step = mCurrentStep.load();
    int totalSteps = mNumSteps.load();

    // A rest clears both notes and ties for the track at this step
    mGrid[track][step][0].store(0);
    mGrid[track][step][1].store(0);
    mTieGrid[track][step].store(false);

    mCurrentStep.store((step + 1) % totalSteps);
}

void MidiSequencer::stepRecordHold(int track) {
    if (track < 0 || track >= MAX_TRACKS) return;
    int step = mCurrentStep.load();
    int totalSteps = mNumSteps.load();
    int prevStep = (step - 1 + totalSteps) % totalSteps;

    mTieGrid[track][step].store(true);
    mGrid[track][step][0].store(mGrid[track][prevStep][0].load());
    mGrid[track][step][1].store(mGrid[track][prevStep][1].load());

    mCurrentStep.store((step + 1) % totalSteps);
}

void MidiSequencer::handleRealTimeNoteOn(int track, int note) {
    if (note < 0 || note >= NUM_NOTES || track < 0 || track >= MAX_TRACKS) return;
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
        mGrid[track][targetStep][0].store(0);
        mGrid[track][targetStep][1].store(0);
    }
    setNote(track, targetStep, note, true);

    int word = note / 64;
    int bit = note % 64;
    mActiveNoteTracking[track][word].fetch_or(1ULL << bit);
}

void MidiSequencer::handleRealTimeNoteOff(int track, int note) {
    if (note < 0 || note >= NUM_NOTES || track < 0 || track >= MAX_TRACKS) return;
    int word = note / 64;
    int bit = note % 64;
    mActiveNoteTracking[track][word].fetch_and(~(1ULL << bit));
}

void MidiSequencer::stop(VoiceManager& voiceManager) {
    releaseStep(mCurrentStep, voiceManager);
    reset();
    for (int t = 0; t < MAX_TRACKS; ++t) {
        mActiveNoteTracking[t][0].store(0);
        mActiveNoteTracking[t][1].store(0);
    }
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
                for (int t = 0; t < MAX_TRACKS; ++t) {
                    uint64_t trackW0 = mActiveNoteTracking[t][0].load();
                    uint64_t trackW1 = mActiveNoteTracking[t][1].load();
                    if (trackW0 != 0) mGrid[t][mCurrentStep][0].fetch_or(trackW0);
                    if (trackW1 != 0) mGrid[t][mCurrentStep][1].fetch_or(trackW1);
                }
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
    for (int t = 0; t < MAX_TRACKS; ++t) {
        if (mTieGrid[t][step].load()) continue; // Skip trigger if this step is a tie from prev

        for (int word = 0; word < 2; ++word) {
            uint64_t val = mGrid[t][step][word].load();
            if (val == 0) continue;
            for (int bit = 0; bit < 64; ++bit) {
                if (val & (1ULL << bit)) {
                    voiceManager.noteOn(word * 64 + bit, vel, nullptr, 0.0f, t);
                }
            }
        }
    }
}

void MidiSequencer::releaseStep(int step, VoiceManager& voiceManager) {
    int totalSteps = mNumSteps.load();
    int nextStep = (step + 1) % totalSteps;

    for (int t = 0; t < MAX_TRACKS; ++t) {
        if (mTieGrid[t][nextStep].load()) continue; // Skip release if next step is tied to this one

        for (int word = 0; word < 2; ++word) {
            uint64_t val = mGrid[t][step][word].load();
            if (val == 0) continue;
            for (int bit = 0; bit < 64; ++bit) {
                if (val & (1ULL << bit)) {
                    voiceManager.noteOff(word * 64 + bit);
                }
            }
        }
    }
}

void MidiSequencer::stepRecordBack() {
    int step = mCurrentStep.load();
    int totalSteps = mNumSteps.load();
    int prevStep = (step - 1 + totalSteps) % totalSteps;

    for (int t = 0; t < MAX_TRACKS; ++t) {
        mGrid[t][prevStep][0].store(0);
        mGrid[t][prevStep][1].store(0);
        mTieGrid[t][prevStep].store(false);
    }
    mCurrentStep.store(prevStep);
}
