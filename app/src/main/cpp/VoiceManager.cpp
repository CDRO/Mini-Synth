#include "VoiceManager.h"

VoiceManager::VoiceManager() {
    setSampleRate(48000);
}

void VoiceManager::setSampleRate(int32_t sampleRate) {
    if (sampleRate <= 0) return;
    mSampleRate = sampleRate;
    for (int i = 0; i < MAX_VOICES; ++i) {
        mVoices[i].setSampleRate(sampleRate);
    }
}

void VoiceManager::setWaveform(Waveform waveform) {
    mCurrentWaveform = waveform;
    for (int i = 0; i < MAX_VOICES; ++i) {
        mVoices[i].setWaveform(waveform);
    }
}

void VoiceManager::setPolyphonic(bool isPolyphonic) {
    mIsPolyphonic = isPolyphonic;
    if (!mIsPolyphonic) {
        // Kill all but the last voice if switching to mono?
        // Or just release all.
        for (int i = 0; i < MAX_VOICES; ++i) {
            mVoices[i].release();
        }
    }
}

void VoiceManager::noteOn(int midiNote, float velocity) {
    if (mIsPolyphonic) {
        // Check if note is already playing
        int index = findVoiceByNote(midiNote);
        if (index != -1) {
            mVoices[index].trigger(midiNote, velocity);
            return;
        }

        index = findFreeVoice();
        if (index != -1) {
            mVoices[index].trigger(midiNote, velocity);
        }
    } else {
        // Monophonic: always use first voice
        mVoices[0].trigger(midiNote, velocity);
    }
}

void VoiceManager::noteOff(int midiNote) {
    if (mIsPolyphonic) {
        int index = findVoiceByNote(midiNote);
        if (index != -1) {
            mVoices[index].release();
        }
    } else {
        if (mVoices[0].getNote() == midiNote) {
            mVoices[0].release();
        }
    }
}

int VoiceManager::findFreeVoice() {
    for (int i = 0; i < MAX_VOICES; ++i) {
        if (!mVoices[i].isActive()) return i;
    }
    // Round-robin stealing
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

float VoiceManager::nextSample() {
    float mixedSample = 0.0f;
    int activeCount = 0;

    for (int i = 0; i < MAX_VOICES; ++i) {
        if (mVoices[i].isActive()) {
            mixedSample += mVoices[i].nextSample();
            activeCount++;
        }
    }

    // Normalization to avoid clipping
    if (activeCount > 0) {
        mixedSample /= static_cast<float>(activeCount);
    }

    return mixedSample;
}
