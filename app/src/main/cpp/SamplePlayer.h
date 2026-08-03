#ifndef MINI_SYNTH_SAMPLEPLAYER_H
#define MINI_SYNTH_SAMPLEPLAYER_H

#include <vector>
#include <atomic>
#include <stdint.h>

class SamplePlayer {
public:
    SamplePlayer();

    void startRecording(std::vector<float>& buffer);
    void stopRecording();
    void recordSample(float sample);

    void trigger(const std::vector<float>& buffer);
    void stop();
    float nextSample();

    bool isActive() const { return mIsPlaying; }
    bool isRecording() const { return mIsRecording; }

private:
    std::vector<float>* mCurrentBuffer = nullptr;
    const std::vector<float>* mPlaybackBuffer = nullptr;
    size_t mPlaybackIndex = 0;
    size_t mRecordIndex = 0;
    std::atomic<bool> mIsRecording{false};
    std::atomic<bool> mIsPlaying{false};
    static const size_t MAX_SAMPLES = 48000 * 5; // 5 seconds at 48kHz

    void reset();
};

#endif //MINI_SYNTH_SAMPLEPLAYER_H
