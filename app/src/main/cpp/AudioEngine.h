#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include "VoiceManager.h"
#include "MidiSequencer.h"
#include "LockFreeQueue.h"
#include "Delay.h"
#include "Reverb.h"
#include "FftProcessor.h"
#include "Track.h"
#include <thread>
#include <atomic>
#include <chrono>
#include <array>

class AudioEngine : public oboe::AudioStreamDataCallback, public oboe::AudioStreamErrorCallback {
public:
    AudioEngine();
    ~AudioEngine();

    void start();
    void stop();
    bool isRunning() { return mStream != nullptr; }

    void noteOn(int midiNote, float velocity, int trackId = 0);
    void noteOff(int midiNote);

    void padNoteOn(int padIndex, float velocity);
    void padNoteOff(int padIndex);
    void setPadLooping(int padIndex, bool looping);

    // Track Management
    void setTrackWaveform(int track, Waveform waveform);
    void setTrackAttack(int track, float seconds);
    void setTrackDecay(int track, float seconds);
    void setTrackSustain(int track, float level);
    void setTrackRelease(int track, float seconds);
    void setTrackLfoRate(int track, float frequency);
    void setTrackLfoDepth(int track, float depth);
    void setTrackLfoWaveform(int track, Waveform waveform);
    void setTrackLfoTarget(int track, LfoTarget target);
    void setTrackLfoSync(int track, bool enabled, float beatsPerCycle);
    void setTrackLfoMatrixAmount(int track, int targetIndex, float amount);
    void setTrackFilterCutoff(int track, float frequency);
    void setTrackFilterResonance(int track, float resonance);
    void setTrackUnison(int track, int count, float detune, float spread);
    void setTrackMorph(int track, float morph);
    void setTrackPhaseDistortion(int track, float pd);
    void setTrackPanning(int track, float panning);
    void setTrackVolume(int track, float volume);

    void setPolyphonic(bool isPolyphonic) { mVoiceManager.setPolyphonic(isPolyphonic); }
    void setOctaveShift(int shift) { mOctaveShift = shift; }
    void setMasterVolume(float volume) { mVoiceManager.setMasterVolume(volume); }

    void setWavetable(const float* data, int32_t size) {
        // For now applies to track 0 or all? The plan says "Wavetable Engine support".
        // We'll update the active track.
    }

    void renderStereoSampleForTest(float& left, float& right);

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
    void setAutoLatencyEnabled(bool enabled) { mAutoLatencyEnabled.store(enabled); }
    bool isAutoLatencyEnabled() const { return mAutoLatencyEnabled.load(); }
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
    void setSequencerNote(int track, int step, int note, bool active) { mMidiSequencer.setNote(track, step, note, active); }
    bool isSequencerNoteActive(int track, int step, int note) const { return mMidiSequencer.getNote(track, step, note); }
    void getSequencerActiveNotes(int track, int step, std::vector<int>& notes) const { mMidiSequencer.getActiveNotes(track, step, notes); }
    bool isSequencerStepActive(int track, int step) const { return mMidiSequencer.isStepActive(track, step); }
    int recordSequencerNote(int track, int note) { return mMidiSequencer.recordNote(track, note); }
    void clearSequencer() { mMidiSequencer.clear(); }
    void clearSequencerTrack(int track) { mMidiSequencer.clearTrack(track); }

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

    void updateTrackParams(int trackId);
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

    static const int MAX_TRACKS = 4;
    std::array<Track, MAX_TRACKS> mTracks;

    FftProcessor mFftProcessor;
    Delay mDelay;
    Reverb mReverb;
    int mOctaveShift = 0;

    static const int MAX_PADS = 256;
    std::vector<float> mPadBuffers[MAX_PADS];
    float mPadPanning[MAX_PADS];
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
    std::atomic<bool> mAutoLatencyEnabled{true};

    void recordingLoop(const std::string& path);
    void updateMetronomeParams();
    float getMetronomeSample();
};

#endif //MINI_SYNTH_AUDIOENGINE_H
