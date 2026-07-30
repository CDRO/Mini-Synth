#ifndef MINI_SYNTH_AUDIOENGINE_H
#define MINI_SYNTH_AUDIOENGINE_H

#include <oboe/Oboe.h>

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    ~AudioEngine();

    void start();
    void stop();

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
};

#endif //MINI_SYNTH_AUDIOENGINE_H
