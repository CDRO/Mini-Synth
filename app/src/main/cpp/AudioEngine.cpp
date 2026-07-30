#include "AudioEngine.h"
#include <android/log.h>

#define TAG "AudioEngine"

AudioEngine::AudioEngine() {}

AudioEngine::~AudioEngine() {
    stop();
}

void AudioEngine::start() {
    oboe::AudioStreamBuilder builder;
    builder.setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(oboe::ChannelCount::Mono)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setDataCallback(this)
        ->openStream(mStream);

    if (mStream) {
        mStream->requestStart();
    }
}

void AudioEngine::stop() {
    if (mStream) {
        mStream->stop();
        mStream->close();
        mStream.reset();
    }
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    float *output = static_cast<float *>(audioData);
    std::fill(output, output + numFrames, 0.0f);
    return oboe::DataCallbackResult::Continue;
}
