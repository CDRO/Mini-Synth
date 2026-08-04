#include "AudioEngine.h"
#include <android/log.h>
#include <fstream>
#include <cstring>
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
    oboe::Result result = builder.setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(oboe::ChannelCount::Mono)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)
        ->setDataCallback(this)
        ->setErrorCallback(this)
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

void AudioEngine::startPadSampling(int padIndex) {
    stopPadSampling();
    if (padIndex >= 0 && padIndex < MAX_PADS) {
        mSamplingPadIndex = padIndex;
        mPadBuffers[padIndex].resize(48000 * 5);
        mSampleRecorder.startRecording(mPadBuffers[padIndex]);
    }
}

void AudioEngine::stopPadSampling() {
    mSamplingPadIndex = -1;
    mSampleRecorder.stopRecording();
}

void AudioEngine::loadFactorySample(int padIndex, int sampleId) {
    if (!isPadAvailable(padIndex)) return;
    if (!mStream || mStream->getSampleRate() <= 0) return;

    mPadBuffers[padIndex].clear();
    float freq = (sampleId == 0) ? 60.0f : 440.0f;
    int numSamples = static_cast<int>(mStream->getSampleRate() * 0.2f);
    mPadBuffers[padIndex].reserve(numSamples);
    for (int i = 0; i < numSamples; ++i) {
        float phase = 2.0f * PI_F * freq * static_cast<float>(i) / static_cast<float>(mStream->getSampleRate());
        float decay = 1.0f - (static_cast<float>(i) / static_cast<float>(numSamples));
        mPadBuffers[padIndex].push_back(sinf(phase) * decay * 0.8f);
    }
}

bool AudioEngine::isPadAvailable(int padIndex) {
    return (padIndex >= 0 && padIndex < MAX_PADS && mSamplingPadIndex != padIndex);
}

void AudioEngine::savePadSample(int padIndex, const char* path) {
    if (!isPadAvailable(padIndex)) return;

    std::ofstream file(path, std::ios::binary);
    if (file.is_open()) {
        SampleHeader header;
        std::memcpy(header.magic, "SNTH", 4);
        header.version = HEADER_VERSION;
        header.numSamples = static_cast<uint64_t>(mPadBuffers[padIndex].size());

        file.write(reinterpret_cast<const char*>(&header), sizeof(header));
        if (header.numSamples > 0) {
            file.write(reinterpret_cast<const char*>(mPadBuffers[padIndex].data()),
                       static_cast<std::streamsize>(header.numSamples * sizeof(float)));
        }
        file.close();
    }
}

void AudioEngine::loadPadSample(int padIndex, const char* path) {
    if (!isPadAvailable(padIndex)) return;

    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) return;

    SampleHeader header;
    if (file.read(reinterpret_cast<char*>(&header), sizeof(header))) {
        if (std::memcmp(header.magic, "SNTH", 4) == 0 && header.version <= HEADER_VERSION) {
            const uint64_t MAX_ALLOWED_SAMPLES = 48000 * 60;
            if (header.numSamples > 0 && header.numSamples <= MAX_ALLOWED_SAMPLES) {
                mPadBuffers[padIndex].resize(static_cast<size_t>(header.numSamples));
                file.read(reinterpret_cast<char*>(mPadBuffers[padIndex].data()),
                          static_cast<std::streamsize>(header.numSamples * sizeof(float)));
            }
        } else {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "Invalid format or version for pad %d", padIndex);
        }
    }
    file.close();
}

void AudioEngine::padNoteOn(int padIndex, float velocity) {
    if (padIndex < 0 || padIndex >= MAX_PADS || padIndex == mSamplingPadIndex) return;

    if (mPadBuffers[padIndex].empty()) {
        mVoiceManager.noteOn(60 + padIndex, velocity);
    } else {
        mVoiceManager.noteOn(60 + padIndex, velocity, &mPadBuffers[padIndex]);
    }
}

void AudioEngine::padNoteOff(int padIndex) {
    if (padIndex < 0 || padIndex >= 16) return;
    mVoiceManager.noteOff(60 + padIndex);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    float *output = static_cast<float *>(audioData);
    int32_t channelCount = audioStream->getChannelCount();
    if (channelCount < 1) return oboe::DataCallbackResult::Stop;

    mMidiSequencer.process(numFrames, mSamplesPerBeat, mVoiceManager);

    for (int i = 0; i < numFrames; ++i) {
        float sample = mVoiceManager.nextSample();

        if (mSamplingPadIndex != -1) {
            mSampleRecorder.recordSample(sample);
        }

        if (mMetronomeEnabled) {
            sample += getMetronomeSample();
        }

        sample = std::max(-1.0f, std::min(sample, 1.0f));
        mVizQueue.push(sample);

        if (mIsRecording.load(std::memory_order_relaxed)) {
            mRecordQueue.push(sample);
        }

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
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
    }

    encoder.flush();
    encoder.close();
}

void AudioEngine::setBpm(float bpm) {
    mBpm = std::max(10.0f, std::min(bpm, 600.0f));
    updateMetronomeParams();
}

void AudioEngine::setMetronomeEnabled(bool enabled) {
    if (enabled && !mMetronomeEnabled) {
        mSampleCounter = 0;
        mBeatCounter = 0;
    }
    mMetronomeEnabled = enabled;
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) {
    auto now = std::chrono::steady_clock::now();
    if (now - mLastRestartTime < MIN_RESTART_INTERVAL) {
        mRestartRetryCount++;
    } else {
        mRestartRetryCount = 0;
    }
    mLastRestartTime = now;

    if (mRestartRetryCount < MAX_RESTART_RETRIES) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "Restarting audio engine after error: %s (Retry %d)",
                           oboe::convertToText(error), mRestartRetryCount + 1);
        start();
    }
}

float AudioEngine::renderSampleForTest() {
    float sample = mVoiceManager.nextSample();
    if (mMetronomeEnabled) {
        sample += getMetronomeSample();
    }
    return sample;
}

void AudioEngine::updateMetronomeParams() {
    float sampleRate = mStream ? static_cast<float>(mStream->getSampleRate()) : 48000.0f;
    mSamplesPerBeat = static_cast<int32_t>(sampleRate * 60.0f / mBpm);
}

bool AudioEngine::isBeatStarted() {
    return mBeatFlag.exchange(false);
}

float AudioEngine::getMetronomeSample() {
    float sample = 0.0f;
    if (mSampleCounter == 0) {
        mBeatFlag.store(true);
    }
    if (mSampleCounter < 500) {
        float freq = (mBeatCounter == 0 ? 880.0f : 440.0f);
        float sampleRate = (mStream ? static_cast<float>(mStream->getSampleRate()) : 48000.0f);
        float phase = 2.0f * PI_F * freq * static_cast<float>(mSampleCounter) / sampleRate;
        float amplitude = 0.5f * (1.0f - static_cast<float>(mSampleCounter) / 500.0f);
        sample = sinf(phase) * amplitude;
    }
    mSampleCounter++;
    if (mSampleCounter >= mSamplesPerBeat) {
        mSampleCounter = 0;
        mBeatCounter = (mBeatCounter + 1) % 4;
    }
    return sample;
}
