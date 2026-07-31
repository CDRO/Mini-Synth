#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "VoiceManager.h"
#include "LockFreeQueue.h"
#include <thread>
#include <atomic>

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    ~AudioEngine();

    void start();
    void stop();

    void noteOn(int midiNote, float velocity) {
        int shifted = midiNote + (mOctaveShift * 12);
        if (shifted < 0) shifted = 0;
        if (shifted > 127) shifted = 127;
        mVoiceManager.noteOn(shifted, velocity);
    }
    void noteOff(int midiNote) {
        int shifted = midiNote + (mOctaveShift * 12);
        if (shifted < 0) shifted = 0;
        if (shifted > 127) shifted = 127;
        mVoiceManager.noteOff(shifted);
    }
    void setPolyphonic(bool isPolyphonic) { mVoiceManager.setPolyphonic(isPolyphonic); }
    void setWaveform(Waveform waveform) { mVoiceManager.setWaveform(waveform); }
    void setOctaveShift(int shift) { mOctaveShift = shift; }

    void setAttack(float seconds) { mVoiceManager.setAttack(seconds); }
    void setDecay(float seconds) { mVoiceManager.setDecay(seconds); }
    void setSustain(float level) { mVoiceManager.setSustain(level); }
    void setRelease(float seconds) { mVoiceManager.setRelease(seconds); }
    void setMasterVolume(float volume) { mVoiceManager.setMasterVolume(volume); }

    void setLfoRate(float frequency) { mVoiceManager.setLfoRate(frequency); }
    void setLfoDepth(float depth) { mVoiceManager.setLfoDepth(depth); }
    void setLfoWaveform(Waveform waveform) { mVoiceManager.setLfoWaveform(waveform); }
    void setLfoTarget(LfoTarget target) { mVoiceManager.setLfoTarget(target); }

    void setFilterCutoff(float frequency) { mVoiceManager.setFilterCutoff(frequency); }
    void setFilterResonance(float resonance) { mVoiceManager.setFilterResonance(resonance); }

    float renderSampleForTest() { return mVoiceManager.nextSample(); }

    int32_t getVisualizerData(float* buffer, int32_t size);
    void startRecording(const std::string& path);
    void stopRecording();

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
    VoiceManager mVoiceManager;
    int mOctaveShift = 0;

    LockFreeQueue<float> mVizQueue{4096};
    LockFreeQueue<float> mRecordQueue{65536};
    std::atomic<bool> mIsRecording{false};
    std::string mRecordPath;
    std::thread mRecordingThread;

    void recordingLoop();
};

#endif //MINI_SYNTH_AUDIOENGINE_H
