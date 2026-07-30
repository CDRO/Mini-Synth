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
        for (int i = 0; i < MAX_VOICES; ++i) {
            mVoices[i].release();
        }
    }
}

void VoiceManager::noteOn(int midiNote, float velocity) {
    if (mIsPolyphonic) {
        int index = findVoiceByNote(midiNote);
        if (index != -1) {
            mVoices[index].trigger(midiNote, velocity);
            return;
        }

        index = findFreeVoice();
        if (index != -1) {
            mVoices[index].setAttack(mParams.attack);
            mVoices[index].setDecay(mParams.decay);
            mVoices[index].setSustain(mParams.sustain);
            mVoices[index].setRelease(mParams.release);

            mVoices[index].setLfoRate(mLfoRate);
            mVoices[index].setLfoDepth(mLfoDepth);
            mVoices[index].setLfoWaveform(mLfoWaveform);
            mVoices[index].setLfoTarget(mLfoTarget);

            mVoices[index].trigger(midiNote, velocity);
        }
    } else {
        mVoices[0].setAttack(mParams.attack);
        mVoices[0].setDecay(mParams.decay);
        mVoices[0].setSustain(mParams.sustain);
        mVoices[0].setRelease(mParams.release);

        mVoices[0].setLfoRate(mLfoRate);
        mVoices[0].setLfoDepth(mLfoDepth);
        mVoices[0].setLfoWaveform(mLfoWaveform);
        mVoices[0].setLfoTarget(mLfoTarget);

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
            // Update real-time LFO params from manager to active voices
            mVoices[i].setLfoRate(mLfoRate);
            mVoices[i].setLfoDepth(mLfoDepth);
            mVoices[i].setLfoWaveform(mLfoWaveform);
            mVoices[i].setLfoTarget(mLfoTarget);

            mixedSample += mVoices[i].nextSample();
            activeCount++;
        }
    }

    if (activeCount > 0) {
        mixedSample /= static_cast<float>(activeCount);
    }

    return mixedSample;
}
