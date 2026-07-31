#include "AudioEngine.h"
#include <android/log.h>
#include "Mp3Encoder.h"

#define TAG "AudioEngine"

AudioEngine::AudioEngine() {
    updateMetronomeParams();
}

AudioEngine::~AudioEngine() {
    stop();
    stopRecording();
}

void AudioEngine::start() {
    if (mStream) return;

    oboe::AudioStreamBuilder builder;
    // Note: Oboe handles fallback from AAudio to OpenSL ES automatically for API < 26.
    oboe::Result result = builder.setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(oboe::ChannelCount::Mono)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setDataCallback(this)
        ->openStream(mStream);

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening stream: %s", oboe::convertToText(result));
        return;
    }

    mVoiceManager.setSampleRate(mStream->getSampleRate());
    updateMetronomeParams();

    result = mStream->requestStart();
    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error starting stream: %s", oboe::convertToText(result));
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
    int32_t channelCount = audioStream->getChannelCount();
    if (channelCount < 1) return oboe::DataCallbackResult::Stop;

    for (int i = 0; i < numFrames; ++i) {
        float sample = mVoiceManager.nextSample();

        if (mMetronomeEnabled) {
            sample += getMetronomeSample();
        }

        // Tap for visualizer
        mVizQueue.push(sample);

        // Tap for recording
        if (mIsRecording.load(std::memory_order_relaxed)) {
            mRecordQueue.push(sample);
        }

        // Duplicate mono sample to all output channels (Stereo mapping fix)
        for (int channel = 0; channel < channelCount; ++channel) {
            output[i * channelCount + channel] = sample;
        }
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t AudioEngine::getVisualizerData(float* buffer, int32_t size) {
    int32_t count = 0;
    while (count < size && mVizQueue.pop(buffer[count])) {
        count++;
    }
    return count;
}

void AudioEngine::startRecording(const std::string& path) {
    if (mIsRecording) return;
    mIsRecording = true;
    mRecordQueue.clear();
    // Copy path into the loop thread to avoid race conditions
    mRecordingThread = std::thread(&AudioEngine::recordingLoop, this, path);
}

void AudioEngine::stopRecording() {
    if (!mIsRecording) return;
    mIsRecording = false;
    if (mRecordingThread.joinable()) {
        mRecordingThread.join();
    }
}

void AudioEngine::recordingLoop(const std::string& path) {
    Mp3Encoder encoder;
    int sampleRate = mStream ? mStream->getSampleRate() : 48000;

    if (!encoder.init(path, sampleRate, 1, 128)) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to initialize MP3 encoder for path: %s", path.c_str());
        mIsRecording = false;
        return;
    }

    std::vector<float> pcmBuffer(4096);
    while (mIsRecording || mRecordQueue.size() > 0) {
        int count = 0;
        while (count < pcmBuffer.size() && mRecordQueue.pop(pcmBuffer[count])) {
            count++;
        }

        if (count > 0) {
            encoder.encode(pcmBuffer.data(), count);
        } else {
            // Wait for more data
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
    }

    encoder.flush();
    encoder.close();
}

void AudioEngine::setBpm(float bpm) {
    mBpm = bpm;
    updateMetronomeParams();
}

void AudioEngine::updateMetronomeParams() {
    if (mStream) {
        mSamplesPerBeat = static_cast<int32_t>(mStream->getSampleRate() * 60.0f / mBpm);
    } else {
        mSamplesPerBeat = static_cast<int32_t>(48000 * 60.0f / mBpm);
    }
}

float AudioEngine::getMetronomeSample() {
    float sample = 0.0f;

    // Generates a simple tick: a decaying burst of noise or sine
    if (mSampleCounter < 500) { // 500 samples duration (~10ms)
        float phase = 2.0f * M_PI * (mBeatCounter == 0 ? 880.0f : 440.0f) * mSampleCounter / (mStream ? mStream->getSampleRate() : 48000);
        float amplitude = 0.5f * (1.0f - mSampleCounter / 500.0f);
        sample = sinf(phase) * amplitude;
    }

    mSampleCounter++;
    if (mSampleCounter >= mSamplesPerBeat) {
        mSampleCounter = 0;
        mBeatCounter = (mBeatCounter + 1) % 4;
    }

    return sample;
}
