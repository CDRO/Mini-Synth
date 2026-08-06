#include "MidiSequencer.h"
#include <cstring>

MidiSequencer::MidiSequencer() {
    clear();
    std::memset(mActiveNoteTracking, 0, sizeof(mActiveNoteTracking));
}

void MidiSequencer::setNote(int step, int note, bool active) {
    if (step >= 0 && step < NUM_STEPS && note >= 0 && note < NUM_NOTES) {
        mGrid[step].set(note, active);
    }
}

bool MidiSequencer::getNote(int step, int note) const {
    if (step >= 0 && step < NUM_STEPS && note >= 0 && note < NUM_NOTES) {
        return mGrid[step].test(note);
    }
    return false;
}

void MidiSequencer::getActiveNotes(int step, std::vector<int>& outNotes) const {
    outNotes.clear();
    if (step >= 0 && step < NUM_STEPS) {
        for (int i = 0; i < NUM_NOTES; ++i) {
            if (mGrid[step].test(i)) outNotes.push_back(i);
        }
    }
}

void MidiSequencer::clear() {
    for (int i = 0; i < NUM_STEPS; ++i) {
        mGrid[i].reset();
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
    mGrid[step].reset();
    mGrid[step].set(note, true);

    int nextStep = step;
    if (!mIsPlaying.load()) {
        nextStep = (step + 1) % NUM_STEPS;
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

    int targetStep = current;
    if (mInputQuantize.load() && total > 0) {
        if (samples > (total / 2)) {
            targetStep = (current + 1) % NUM_STEPS;
        }
    }

    mGrid[targetStep].set(note, true);
    mActiveNoteTracking[note] = true;
}

void MidiSequencer::handleRealTimeNoteOff(int note) {
    if (note < 0 || note >= NUM_NOTES) return;
    mActiveNoteTracking[note] = false;
}

void MidiSequencer::stop(VoiceManager& voiceManager) {
    releaseStep(mCurrentStep, voiceManager);
    reset();
    std::memset(mActiveNoteTracking, 0, sizeof(mActiveNoteTracking));
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

    int32_t gateSamples = static_cast<int32_t>(static_cast<float>(stepDurationSamples) * 0.9f); // 90% Gate

    int32_t samplesCounter = mSamplesProcessed.load();

    for (int i = 0; i < numFrames; ++i) {
        if (samplesCounter == 0) {
            triggerStep(mCurrentStep, voiceManager);

            if (mIsRecording.load()) {
                // Record active notes into the grid for the current step
                for (int note = 0; note < NUM_NOTES; ++note) {
                    if (mActiveNoteTracking[note]) {
                        mGrid[mCurrentStep].set(note, true);
                    }
                }
            }
        }

        samplesCounter++;

        if (samplesCounter == gateSamples) {
            releaseStep(mCurrentStep, voiceManager);
        }

        if (samplesCounter >= stepDurationSamples) {
            samplesCounter = 0;
            mCurrentStep = (mCurrentStep + 1) % NUM_STEPS;
        }
    }
    mSamplesProcessed.store(samplesCounter);
}

void MidiSequencer::triggerStep(int step, VoiceManager& voiceManager) {
    float vel = mVelocity.load();
    for (int i = 0; i < NUM_NOTES; ++i) {
        if (mGrid[step].test(i)) {
            voiceManager.noteOn(i, vel);
        }
    }
}

void MidiSequencer::releaseStep(int step, VoiceManager& voiceManager) {
    for (int i = 0; i < NUM_NOTES; ++i) {
        if (mGrid[step].test(i)) {
            voiceManager.noteOff(i);
        }
    }
}
