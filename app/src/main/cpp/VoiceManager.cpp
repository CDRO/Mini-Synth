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

void VoiceManager::noteOn(int midiNote, float velocity, const std::vector<float>* sampleBuffer) {
    if (mIsPolyphonic) {
        int index = findVoiceByNote(midiNote);
        if (index != -1) {
            mVoices[index].trigger(midiNote, velocity, sampleBuffer);
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

            mVoices[index].setFilterCutoff(mFilterCutoff);
            mVoices[index].setFilterResonance(mFilterResonance);

            mVoices[index].trigger(midiNote, velocity, sampleBuffer);
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

        mVoices[0].setFilterCutoff(mFilterCutoff);
        mVoices[0].setFilterResonance(mFilterResonance);

        mVoices[0].trigger(midiNote, velocity, sampleBuffer);
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

    bool updateParams = mParamsChanged.exchange(false);
    float currentVol = mMasterVolume.load(std::memory_order_relaxed);

    for (int i = 0; i < MAX_VOICES; ++i) {
        if (mVoices[i].isActive()) {
            if (updateParams) {
                mVoices[i].setLfoRate(mLfoRate);
                mVoices[i].setLfoDepth(mLfoDepth);
                mVoices[i].setLfoWaveform(mLfoWaveform);
                mVoices[i].setLfoTarget(mLfoTarget);

                mVoices[i].setFilterCutoff(mFilterCutoff);
                mVoices[i].setFilterResonance(mFilterResonance);
            }

            mixedSample += mVoices[i].nextSample();
            activeCount++;
        }
    }

    if (activeCount > 0) {
        mixedSample *= 0.5f; // Headroom for polyphony
    }

    return mixedSample * currentVol;
}
