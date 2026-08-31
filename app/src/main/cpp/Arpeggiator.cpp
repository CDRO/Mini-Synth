#include "Arpeggiator.h"

Arpeggiator::Arpeggiator() {
    std::random_device rd;
    mRng = std::mt19937(rd());
}

void Arpeggiator::noteOn(int note) {
    std::lock_guard<std::mutex> lock(mNoteMutex);
    if (std::find(mHeldNotes.begin(), mHeldNotes.end(), note) == mHeldNotes.end()) {
        mHeldNotes.push_back(note);
        std::sort(mHeldNotes.begin(), mHeldNotes.end());
        updateArpNotes();
    }
}

void Arpeggiator::noteOff(int note) {
    std::lock_guard<std::mutex> lock(mNoteMutex);
    auto it = std::find(mHeldNotes.begin(), mHeldNotes.end(), note);
    if (it != mHeldNotes.end()) {
        mHeldNotes.erase(it);
        updateArpNotes();
    }
}

void Arpeggiator::setParams(const EngineParams& params) {
    std::lock_guard<std::mutex> lock(mNoteMutex);
    mMode = params.arpMode;
    mDivision = params.arpDivision;
    mOctaveRange = params.arpOctaves;
    updateArpNotes();
}

void Arpeggiator::updateArpNotes() {
    mArpNotes.clear();
    if (mHeldNotes.empty()) return;

    for (int oct = 0; oct < mOctaveRange; ++oct) {
        for (int note : mHeldNotes) {
            int n = note + (oct * 12);
            if (n <= 127) mArpNotes.push_back(n);
        }
    }

    if (mMode == ArpMode::Down) {
        std::reverse(mArpNotes.begin(), mArpNotes.end());
    }

    if (mCurrentStep >= mArpNotes.size()) {
        mCurrentStep = 0;
    }
}

void Arpeggiator::process(int32_t numFrames, int32_t samplesPerBeat, int trackId, VoiceManager& vm) {
    if (mMode == ArpMode::Off) {
        if (mPlayingNote != -1) stop(vm);
        return;
    }

    int32_t stepDuration = static_cast<int32_t>(static_cast<float>(samplesPerBeat) * mDivision);
    if (stepDuration <= 0) return;

    for (int i = 0; i < numFrames; ++i) {
        if (mSamplesProcessed >= stepDuration) {
            mSamplesProcessed = 0;
            advance(trackId, vm);
        }
        mSamplesProcessed++;
    }
}

void Arpeggiator::advance(int trackId, VoiceManager& vm) {
    std::lock_guard<std::mutex> lock(mNoteMutex);

    if (mPlayingNote != -1) {
        vm.noteOff(mPlayingNote);
        mPlayingNote = -1;
    }

    if (mArpNotes.empty()) return;

    if (mMode == ArpMode::Random) {
        std::uniform_int_distribution<int> dist(0, mArpNotes.size() - 1);
        mCurrentStep = dist(mRng);
    } else if (mMode == ArpMode::UpDown) {
        if (mDirectionUp) {
            mCurrentStep++;
            if (mCurrentStep >= mArpNotes.size()) {
                mCurrentStep = std::max(0, static_cast<int>(mArpNotes.size()) - 2);
                mDirectionUp = false;
            }
        } else {
            mCurrentStep--;
            if (mCurrentStep < 0) {
                mCurrentStep = std::min(1, static_cast<int>(mArpNotes.size()) - 1);
                mDirectionUp = true;
            }
        }
    } else {
        mCurrentStep = (mCurrentStep + 1) % mArpNotes.size();
    }

    mPlayingNote = mArpNotes[mCurrentStep];
    vm.noteOn(mPlayingNote, 0.8f, nullptr, 0.0f, trackId);
}

void Arpeggiator::stop(VoiceManager& vm) {
    std::lock_guard<std::mutex> lock(mNoteMutex);
    if (mPlayingNote != -1) {
        vm.noteOff(mPlayingNote);
        mPlayingNote = -1;
    }
    mSamplesProcessed = 0;
    mCurrentStep = 0;
}
