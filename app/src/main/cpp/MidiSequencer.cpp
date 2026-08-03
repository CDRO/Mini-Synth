#include "MidiSequencer.h"

MidiSequencer::MidiSequencer() {
    clear();
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

void MidiSequencer::stop(VoiceManager& voiceManager) {
    releaseStep(mCurrentStep, voiceManager);
    reset();
}

void MidiSequencer::reset() {
    mSamplesProcessed = 0;
    mCurrentStep.store(0);
}

void MidiSequencer::process(int32_t numFrames, int32_t samplesPerBeat, VoiceManager& voiceManager) {
    if (!mIsPlaying.load()) return;

    int32_t stepDurationSamples = static_cast<int32_t>(static_cast<float>(samplesPerBeat) * mStepDivision.load());
    int32_t gateSamples = static_cast<int32_t>(static_cast<float>(stepDurationSamples) * 0.9f); // 90% Gate

    for (int i = 0; i < numFrames; ++i) {
        if (mSamplesProcessed == 0) {
            triggerStep(mCurrentStep, voiceManager);
        }

        mSamplesProcessed++;

        if (mSamplesProcessed == gateSamples) {
            releaseStep(mCurrentStep, voiceManager);
        }

        if (mSamplesProcessed >= stepDurationSamples) {
            mSamplesProcessed = 0;
            mCurrentStep = (mCurrentStep + 1) % NUM_STEPS;
        }
    }
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
