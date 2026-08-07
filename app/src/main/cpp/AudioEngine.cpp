#include "AudioEngine.h"
#include <android/log.h>
#include <fstream>
#include <cstring>
#include "Mp3Encoder.h"
#include "ProjectManager.h"

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
        ->setErrorCallback(this)
        ->openStream(mStream);

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error opening stream: %s", oboe::convertToText(result));
        return;
    }

    mVoiceManager.setSampleRate(mStream->getSampleRate());
    mDelay.setSampleRate(mStream->getSampleRate());
    mReverb.setSampleRate(mStream->getSampleRate());
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
        mPadBuffers[padIndex].clear();
        mPadBuffers[padIndex].resize(48000 * 5); // 5s limit
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

/**
 * BINARY SERIALIZATION FORMAT:
 * [4 bytes] Magic: 'SNTH'
 * [4 bytes] Version: uint32 (Current: 1)
 * [8 bytes] Size: uint64 (Number of float samples)
 * [N bytes] Data: float[] (Raw PCM data)
 */
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

void AudioEngine::setPadLooping(int padIndex, bool looping) {
    if (padIndex < 0 || padIndex >= MAX_PADS) return;
    mVoiceManager.setPadLooping(60 + padIndex, looping);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    float *output = static_cast<float *>(audioData);
    int32_t channelCount = audioStream->getChannelCount();
    if (channelCount < 1) return oboe::DataCallbackResult::Stop;

    // Process External MIDI Queue
    MidiEvent event;
    while (mMidiQueue.pop(event)) {
        uint8_t status = event.status & 0xF0;
        uint8_t note = event.data1;
        uint8_t velocity = event.data2;

        if (status == 0x90 && velocity > 0) {
            mVoiceManager.noteOn(note, velocity / 127.0f);
            if (mIsSequencerRecording.load()) mMidiSequencer.handleRealTimeNoteOn(note);
        } else if (status == 0x80 || (status == 0x90 && velocity == 0)) {
            mVoiceManager.noteOff(note);
        } else if (status == 0xB0) {
            uint8_t ccNumber = event.data1;
            uint8_t ccValue = event.data2;
            if (ccNumber == 1) {
                mVoiceManager.setModulation(ccValue / 127.0f);
            } else if (ccNumber == 74) {
                float normalized = ccValue / 127.0f;
                float frequency = 20.0f * powf(1000.0f, normalized);
                mVoiceManager.setFilterCutoff(frequency);
            }
        }
    }

    mMidiSequencer.process(numFrames, mSamplesPerBeat, mVoiceManager);

    // Adaptive Buffer Management Check
    mFramesSinceLastStabilityCheck += numFrames;
    if (mFramesSinceLastStabilityCheck >= 24000) { // Check every 0.5s for faster response
        mFramesSinceLastStabilityCheck = 0;
        int32_t currentXRuns = getXRunCount();
        if (currentXRuns > mLastXRunCount) {
            int32_t diff = currentXRuns - mLastXRunCount;
            // Underrun occurred! Increase buffer size proportional to xRun delta
            int32_t currentSize = mStream->getBufferSizeInFrames();
            int32_t burstSize = mStream->getFramesPerBurst();
            int32_t increase = burstSize * (diff > 1 ? 2 : 1);
            int32_t newSize = std::min(currentSize + increase, mStream->getBufferCapacityInFrames());

            if (newSize != currentSize) {
                mStream->setBufferSizeInFrames(newSize);
                __android_log_print(ANDROID_LOG_INFO, TAG, "Adaptive Buffer: Increased to %d due to %d new xRuns", newSize, diff);
            }
            mLastXRunCount = currentXRuns;
        }
    }

    for (int i = 0; i < numFrames; ++i) {
        float sample = mVoiceManager.nextSample();

        if (mSamplingPadIndex != -1) {
            // Sampling timeout/limit check (5s)
            if (mPadBuffers[mSamplingPadIndex].size() < (48000 * 5)) {
                mSampleRecorder.recordSample(sample);
                if (mAutoSampleRemaining > 0) {
                    mAutoSampleRemaining--;
                    if (mAutoSampleRemaining == 0) {
                        stopPadSampling();
                    }
                }
            } else {
                stopPadSampling();
            }
        }

        if (mMetronomeEnabled) {
            sample += getMetronomeSample();
        }

        sample = mDelay.process(sample);
        sample = mReverb.process(sample);

        sample = std::max(-1.0f, std::min(sample, 1.0f));
        mVizQueue.push(sample);
        mFftQueue.push(sample);

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

int32_t AudioEngine::getFftData(float* buffer, int32_t size) {
    if (size < FftProcessor::FFT_SIZE / 2) return 0;

    std::vector<float> samples(FftProcessor::FFT_SIZE);
    int count = 0;

    // We want the LATEST samples.
    // If we have more than FFT_SIZE in queue, skip some?
    // Or just pull until empty and keep last 1024.
    float s;
    std::vector<float> allSamples;
    while (mFftQueue.pop(s)) {
        allSamples.push_back(s);
    }

    if (allSamples.size() < FftProcessor::FFT_SIZE) return 0;

    // Take the last 1024
    size_t start = allSamples.size() - FftProcessor::FFT_SIZE;
    for (int i = 0; i < FftProcessor::FFT_SIZE; ++i) {
        samples[i] = allSamples[start + i];
    }

    mFftProcessor.process(samples.data(), buffer);
    return FftProcessor::FFT_SIZE / 2;
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

void AudioEngine::renderPatternToFile(const std::string& path) {
    EngineParams params = mVoiceManager.getParams();
    int32_t sampleRate = mStream ? mStream->getSampleRate() : 48000;
    int32_t samplesPerBeat = static_cast<int32_t>(static_cast<float>(sampleRate) * 60.0f / mBpm);

    VoiceManager renderVm;
    renderVm.setSampleRate(sampleRate);
    renderVm.setParams(params);

    Mp3Encoder encoder;
    if (!encoder.init(path, sampleRate, 1, 192)) return;

    float stepDivision = mMidiSequencer.getStepDivision();
    int32_t stepDuration = static_cast<int32_t>(static_cast<float>(samplesPerBeat) * stepDivision);
    int32_t gateSamples = static_cast<int32_t>(static_cast<float>(stepDuration) * 0.9f);

    std::vector<float> pcmBuffer(stepDuration);
    const std::atomic<uint64_t>* grid = mMidiSequencer.getGridData();

    // Render 16 steps
    for (int step = 0; step < 16; ++step) {
        // Trigger
        for (int word = 0; word < 2; ++word) {
            uint64_t val = grid[step * 2 + word].load();
            if (val == 0) continue;
            for (int bit = 0; bit < 64; ++bit) {
                if (val & (1ULL << bit)) renderVm.noteOn(word * 64 + bit, 0.8f);
            }
        }

        for (int s = 0; s < stepDuration; ++s) {
            if (s == gateSamples) {
                 for (int word = 0; word < 2; ++word) {
                    uint64_t val = grid[step * 2 + word].load();
                    if (val == 0) continue;
                    for (int bit = 0; bit < 64; ++bit) {
                        if (val & (1ULL << bit)) renderVm.noteOff(word * 64 + bit);
                    }
                 }
            }
            pcmBuffer[s] = renderVm.nextSample(); // Use internal mix logic (includes 0.5f headroom)
        }
        encoder.encode(pcmBuffer.data(), stepDuration);
    }

    encoder.flush();
    encoder.close();
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

void AudioEngine::saveProject(const std::string& directory) {
    std::vector<std::vector<float>> pads(MAX_PADS);
    for (int i = 0; i < MAX_PADS; ++i) {
        pads[i] = mPadBuffers[i];
    }
    ProjectManager::saveProject(directory, mVoiceManager.getParams(), mMidiSequencer, pads, mBpm);
}

void AudioEngine::loadProject(const std::string& directory) {
    EngineParams params;
    std::vector<std::vector<float>> pads;
    float bpm;
    if (ProjectManager::loadProject(directory, params, mMidiSequencer, pads, bpm)) {
        mVoiceManager.setParams(params);
        mBpm = bpm;
        updateMetronomeParams();
        for (int i = 0; i < MAX_PADS && i < pads.size(); ++i) {
            mPadBuffers[i] = pads[i];
        }
    }
}

void AudioEngine::processExternalMidi(const uint8_t* data, int32_t length) {
    if (length < 3) return;
    mMidiQueue.push({data[0], data[1], data[2]});
}

void AudioEngine::noteOn(int midiNote, float velocity) {
    int shifted = midiNote + (mOctaveShift * 12);
    if (shifted < 0) shifted = 0;
    if (shifted > 127) shifted = 127;
    mMidiQueue.push({0x90, static_cast<uint8_t>(shifted), static_cast<uint8_t>(velocity * 127.0f)});
}

void AudioEngine::noteOff(int midiNote) {
    int shifted = midiNote + (mOctaveShift * 12);
    if (shifted < 0) shifted = 0;
    if (shifted > 127) shifted = 127;
    mMidiQueue.push({0x80, static_cast<uint8_t>(shifted), 0});
}

void AudioEngine::startAutomatedSampling(int padIndex, float durationSeconds) {
    if (padIndex < 0 || padIndex >= MAX_PADS) return;
    int32_t sampleRate = mStream ? mStream->getSampleRate() : 48000;
    mAutoSampleRemaining = static_cast<int32_t>(durationSeconds * static_cast<float>(sampleRate));
    startPadSampling(padIndex);
}

int32_t AudioEngine::getXRunCount() {
    if (!mStream) return 0;
    auto result = mStream->getXRunCount();
    return result.value_or(0);
}

int32_t AudioEngine::getBufferSize() {
    if (!mStream) return 0;
    return mStream->getBufferSizeInFrames();
}

int32_t AudioEngine::getFramesPerBurst() {
    if (!mStream) return 192; // Default
    return mStream->getFramesPerBurst();
}
