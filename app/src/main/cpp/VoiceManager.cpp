#include "VoiceManager.h"
#include <cmath>
#include <algorithm>

VoiceManager::VoiceManager() {
    for (int i = 0; i < MAX_VOICES; ++i) {
        mVoiceTrackIds[i] = -1;
    }
}

void VoiceManager::setSampleRate(int32_t sampleRate) {
    mSampleRate = sampleRate;
    for (int i = 0; i < MAX_VOICES; ++i) {
        mVoices[i].setSampleRate(sampleRate);
    }
}

void VoiceManager::setPolyphonic(bool isPolyphonic) {
    mIsPolyphonic = isPolyphonic;
}

void VoiceManager::updateTrackParams(int trackId, const EngineParams& params) {
    if (trackId >= 0 && trackId < MAX_TRACKS) {
        mTrackParams[trackId] = params;

        // Update any voices currently playing this track
        for (int i = 0; i < MAX_VOICES; ++i) {
            if (mVoiceTrackIds[i] == trackId) {
                // We could apply some params instantly (like filter/lfo),
                // but noteOn handles the initial setup.
                // For performance, we'll let nextSample pull current track state if needed,
                // or just wait for the next note trigger.
            }
        }
    }
}

void VoiceManager::noteOn(int midiNote, float velocity, const std::vector<float>* sampleBuffer, float initialPan, int trackId, const SampleMetadata* metadata) {
    if (trackId < 0 || trackId >= MAX_TRACKS) trackId = 0;

    int index = findFreeVoice();
    if (index != -1) {
        mVoiceTrackIds[index] = trackId;
        const auto& p = mTrackParams[trackId];

        mVoices[index].setWaveform(p.waveform);
        mVoices[index].setAttack(p.attack);
        mVoices[index].setDecay(p.decay);
        mVoices[index].setSustain(p.sustain);
        mVoices[index].setRelease(p.release);
        mVoices[index].setLfoRate(p.lfoRate);
        mVoices[index].setLfoDepth(p.lfoDepth);
        mVoices[index].setLfoWaveform(p.lfoWaveform);
        mVoices[index].setLfoSync(p.lfoSync, p.lfoSyncDivision);
        for (int i = 0; i < 4; ++i) mVoices[index].setLfoMatrixAmount(i, p.lfoMatrix[i]);

        mVoices[index].setFilterCutoff(p.filterCutoff);
        mVoices[index].setFilterResonance(p.filterResonance);
        mVoices[index].setUnison(p.unisonCount, p.unisonDetune, p.unisonSpread);
        mVoices[index].setMorph(p.morph);
        mVoices[index].setPhaseDistortion(p.phaseDistortion);
        mVoices[index].setPanning(initialPan != 0.0f ? initialPan : p.panning);

        mVoices[index].trigger(midiNote, velocity, sampleBuffer, metadata);
    }
}

void VoiceManager::noteOff(int midiNote) {
    int index = findVoiceByNote(midiNote);
    if (index != -1) {
        mVoices[index].release();
    }
}

void VoiceManager::setPadLooping(int midiNote, bool looping) {
    int index = findVoiceByNote(midiNote);
    if (index != -1) {
        mVoices[index].setSampleLooping(looping);
    }
}

void VoiceManager::setVoiceAftertouch(int midiNote, float amount) {
    int index = findVoiceByNote(midiNote);
    if (index != -1) {
        mVoices[index].setAftertouch(amount);
    }
}

void VoiceManager::setBpm(float bpm) {
    for (int i = 0; i < MAX_VOICES; ++i) {
        mVoices[i].setBpm(bpm);
    }
}

void VoiceManager::nextSample(float& left, float& right) {
    float masterVol = mMasterVolume.load();
    left = 0.0f;
    right = 0.0f;

    for (int i = 0; i < MAX_VOICES; ++i) {
        if (mVoices[i].isActive()) {
            float vL, vR;
            mVoices[i].nextSample(vL, vR);

            // Apply track-specific volume if we had one (for now master only)
            left += vL * masterVol;
            right += vR * masterVol;
        } else {
            mVoiceTrackIds[i] = -1;
        }
    }
}

int VoiceManager::findFreeVoice() {
    for (int i = 0; i < MAX_VOICES; ++i) {
        if (!mVoices[i].isActive()) return i;
    }
    // Steal oldest
    int index = mLastStealIndex;
    mLastStealIndex = (mLastStealIndex + 1) % MAX_VOICES;
    return index;
}

int VoiceManager::findVoiceByNote(int midiNote) {
    for (int i = 0; i < MAX_VOICES; ++i) {
        if (mVoices[i].isActive() && mVoices[i].getNote() == midiNote) return i;
    }
    return -1;
}
