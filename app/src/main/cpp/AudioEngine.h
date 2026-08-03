#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "VoiceManager.h"
#include "MidiSequencer.h"
#include "LockFreeQueue.h"
#include <thread>
#include <atomic>
#include <chrono>

class AudioEngine : public oboe::AudioStreamDataCallback, public oboe::AudioStreamErrorCallback {
public:
    AudioEngine();
    ~AudioEngine();

    void start();
    void stop();
    bool isRunning() { return mStream != nullptr; }

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

    void padNoteOn(int padIndex, float velocity);
    void padNoteOff(int padIndex);

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

    float renderSampleForTest();

    int32_t getVisualizerData(float* buffer, int32_t size);
    void startRecording(const std::string& path);
    void stopRecording();

    void setBpm(float bpm);
    void setMetronomeEnabled(bool enabled);
    bool isBeatStarted();

    // Pad Sampling
    void startPadSampling(int padIndex);
    void stopPadSampling();
    bool isPadSampling() const { return mSamplingPadIndex != -1; }
    void loadFactorySample(int padIndex, int sampleId);

    // Sequencer
    void setSequencerPlaying(bool playing) { mMidiSequencer.setPlaying(playing, mVoiceManager); }
    bool isSequencerPlaying() const { return mMidiSequencer.isPlaying(); }
    void setSequencerNote(int step, int note, bool active) { mMidiSequencer.setNote(step, note, active); }
    bool isSequencerNoteActive(int step, int note) const { return mMidiSequencer.getNote(step, note); }
    bool isSequencerStepActive(int step) const { return mMidiSequencer.isStepActive(step); }
    int recordSequencerNote(int note) { return mMidiSequencer.recordNote(note); }
    void clearSequencer() { mMidiSequencer.clear(); }
    void setSequencerStepDuration(float division) { mMidiSequencer.setStepDuration(division); }
    int getSequencerCurrentStep() const { return mMidiSequencer.getCurrentStep(); }

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

private:
    static constexpr float PI_F = 3.1415926535f;
    static constexpr int MAX_RESTART_RETRIES = 5;
    static constexpr auto MIN_RESTART_INTERVAL = std::chrono::seconds(2);

    std::shared_ptr<oboe::AudioStream> mStream;
    VoiceManager mVoiceManager;
    MidiSequencer mMidiSequencer;
    int mOctaveShift = 0;

    static const int MAX_PADS = 256;
    std::vector<float> mPadBuffers[MAX_PADS];
    int mSamplingPadIndex = -1;
    SamplePlayer mSampleRecorder;

    LockFreeQueue<float> mVizQueue{4096};
    LockFreeQueue<float> mRecordQueue{262144}; // Increased to 256k for better safety margin
    std::atomic<bool> mIsRecording{false};
    std::string mRecordPath;
    std::thread mRecordingThread;

    float mBpm = 120.0f;
    bool mMetronomeEnabled = false;
    int32_t mSamplesPerBeat = 0;
    int32_t mSampleCounter = 0;
    int32_t mBeatCounter = 0;
    std::atomic<bool> mBeatFlag{false};

    int mRestartRetryCount = 0;
    std::chrono::steady_clock::time_point mLastRestartTime;

    void recordingLoop(const std::string& path);
    void updateMetronomeParams();
    float getMetronomeSample();
};

#endif //MINI_SYNTH_AUDIOENGINE_H
