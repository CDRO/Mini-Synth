#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "VoiceManager.h"
#include "MidiSequencer.h"
#include "LockFreeQueue.h"
#include "Delay.h"
#include "Reverb.h"
#include "FftProcessor.h"
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

    void noteOn(int midiNote, float velocity);
    void noteOff(int midiNote);

    void padNoteOn(int padIndex, float velocity);
    void padNoteOff(int padIndex);
    void setPadLooping(int padIndex, bool looping);

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
    void setAftertouchTarget(LfoTarget target) { mVoiceManager.setAftertouchTarget(target); }

    void setFilterCutoff(float frequency) { mVoiceManager.setFilterCutoff(frequency); }
    void setFilterResonance(float resonance) { mVoiceManager.setFilterResonance(resonance); }

    void setPitchBend(float semitones) { mVoiceManager.setPitchBend(semitones); }
    void setModulation(float amount) { mVoiceManager.setModulation(amount); }
    void setAftertouch(int midiNote, float amount) { mVoiceManager.setVoiceAftertouch(midiNote, amount); }

    // Effects
    void setDelayTime(float seconds) { mDelay.setTime(seconds); }
    void setDelayFeedback(float feedback) { mDelay.setFeedback(feedback); }
    void setDelayMix(float mix) { mDelay.setMix(mix); }

    void setReverbSize(float size) { mReverb.setSize(size); }
    void setReverbDamping(float damping) { mReverb.setDamping(damping); }
    void setReverbMix(float mix) { mReverb.setMix(mix); }

    float renderSampleForTest();

    int32_t getVisualizerData(float* buffer, int32_t size);
    int32_t getFftData(float* buffer, int32_t size);
    void startRecording(const std::string& path);
    void stopRecording();

    void setBpm(float bpm);
    void setMetronomeEnabled(bool enabled);
    bool isBeatStarted();

    void renderPatternToFile(const std::string& path);

    // Buffer Status
    int32_t getXRunCount();
    int32_t getBufferSize();
    int32_t getFramesPerBurst();
    void checkAndApplyBufferSize();

    // External MIDI
    void processExternalMidi(const uint8_t* data, int32_t length);

    // Pad Sampling
    void startPadSampling(int padIndex);
    void stopPadSampling();
    void startAutomatedSampling(int padIndex, float durationSeconds);
    bool isPadSampling() const { return mSamplingPadIndex != -1; }
    void loadFactorySample(int padIndex, int sampleId);
    void savePadSample(int padIndex, const char* path);
    void loadPadSample(int padIndex, const char* path);

    // Sequencer
    void setSequencerPlaying(bool playing) { mMidiSequencer.setPlaying(playing, mVoiceManager); }
    bool isSequencerPlaying() const { return mMidiSequencer.isPlaying(); }
    void setSequencerRecording(bool recording) {
        mIsSequencerRecording.store(recording);
        mMidiSequencer.setRecording(recording);
    }
    bool isSequencerRecording() const { return mIsSequencerRecording.load(); }
    void setSequencerNote(int step, int note, bool active) { mMidiSequencer.setNote(step, note, active); }
    bool isSequencerNoteActive(int step, int note) const { return mMidiSequencer.getNote(step, note); }
    void getSequencerActiveNotes(int step, std::vector<int>& notes) const { mMidiSequencer.getActiveNotes(step, notes); }
    bool isSequencerStepActive(int step) const { return mMidiSequencer.isStepActive(step); }
    int recordSequencerNote(int note) { return mMidiSequencer.recordNote(note); }
    void handleRealTimeNoteOn(int note) { mMidiSequencer.handleRealTimeNoteOn(note); }
    void handleRealTimeNoteOff(int note) { mMidiSequencer.handleRealTimeNoteOff(note); }
    void clearSequencer() { mMidiSequencer.clear(); }
    void setSequencerStepDuration(float division) { mMidiSequencer.setStepDuration(division); }
    void setSequencerNumSteps(int steps) { mMidiSequencer.setNumSteps(steps); }
    void setInputQuantize(bool enabled) { mMidiSequencer.setInputQuantize(enabled); }
    void setOverdub(bool enabled) { mMidiSequencer.setOverdub(enabled); }
    int getSequencerCurrentStep() const { return mMidiSequencer.getCurrentStep(); }

    void saveProject(const std::string& directory);
    void loadProject(const std::string& directory);

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

private:
    static constexpr float PI_F = 3.1415926535f;
    static constexpr int MAX_RESTART_RETRIES = 5;
    static constexpr auto MIN_RESTART_INTERVAL = std::chrono::seconds(2);

    bool isPadAvailable(int padIndex);
    struct SampleHeader {
        char magic[4];
        uint32_t version;
        uint64_t numSamples;
    };
    static constexpr uint32_t HEADER_VERSION = 1;

    struct MidiEvent {
        uint8_t status;
        uint8_t data1;
        uint8_t data2;
    };
    LockFreeQueue<MidiEvent> mMidiQueue{1024};

    std::shared_ptr<oboe::AudioStream> mStream;
    VoiceManager mVoiceManager;
    MidiSequencer mMidiSequencer;
    FftProcessor mFftProcessor;
    Delay mDelay;
    Reverb mReverb;
    int mOctaveShift = 0;

    static const int MAX_PADS = 256;
    std::vector<float> mPadBuffers[MAX_PADS];
    int mSamplingPadIndex = -1;
    int32_t mAutoSampleRemaining = 0;
    SamplePlayer mSampleRecorder;

    LockFreeQueue<float> mVizQueue{4096};
    LockFreeQueue<float> mFftQueue{4096};
    LockFreeQueue<float> mRecordQueue{262144};
    std::atomic<bool> mIsRecording{false};
    std::atomic<bool> mIsSequencerRecording{false};
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

    int32_t mLastXRunCount = 0;
    int32_t mFramesSinceLastStabilityCheck = 0;
    std::atomic<int32_t> mRequestedBufferSize{0};

    void recordingLoop(const std::string& path);
    void updateMetronomeParams();
    float getMetronomeSample();
};

#endif //MINI_SYNTH_AUDIOENGINE_H
