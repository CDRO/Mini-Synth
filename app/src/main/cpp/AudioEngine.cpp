#include "AudioEngine.h"
#include <android/log.h>
#include <fstream>
#include <cstring>
#include <algorithm>
#include "WavEncoder.h"
#include "ProjectManager.h"

#define TAG "AudioEngine"

AudioEngine::AudioEngine() {
    updateMetronomeParams();
    for (int i = 0; i < MAX_PADS; ++i) mPadPanning[i] = 0.0f;
    for (int t = 0; t < MAX_TRACKS; ++t) {
        updateTrackParams(t);
    }
}

AudioEngine::~AudioEngine() {
    stop();
    stopRecording();
}

void AudioEngine::start() {
    if (mStream) return;

    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(oboe::ChannelCount::Stereo)
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

#if defined(__i386__) || defined(__x86_64__)
    int32_t burstSize = mStream->getFramesPerBurst();
    mStream->setBufferSizeInFrames(burstSize * 4);
#endif

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

void AudioEngine::updateTrackParams(int trackId) {
    if (trackId >= 0 && trackId < MAX_TRACKS) {
        mVoiceManager.updateTrackParams(trackId, mTracks[trackId].params);
    }
}

void AudioEngine::setTrackWaveform(int track, Waveform waveform) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.waveform = waveform; updateTrackParams(track); } }
void AudioEngine::setTrackAttack(int track, float s) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.attack = s; updateTrackParams(track); } }
void AudioEngine::setTrackDecay(int track, float s) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.decay = s; updateTrackParams(track); } }
void AudioEngine::setTrackSustain(int track, float l) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.sustain = l; updateTrackParams(track); } }
void AudioEngine::setTrackRelease(int track, float s) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.release = s; updateTrackParams(track); } }
void AudioEngine::setTrackLfoRate(int track, float f) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.lfoRate = f; updateTrackParams(track); } }
void AudioEngine::setTrackLfoDepth(int track, float d) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.lfoDepth = d; updateTrackParams(track); } }
void AudioEngine::setTrackLfoWaveform(int track, Waveform w) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.lfoWaveform = w; updateTrackParams(track); } }
void AudioEngine::setTrackLfoTarget(int track, LfoTarget t) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.lfoTarget = t; updateTrackParams(track); } }
void AudioEngine::setTrackFilterCutoff(int track, float f) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.filterCutoff = f; updateTrackParams(track); } }
void AudioEngine::setTrackFilterResonance(int track, float r) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.filterResonance = r; updateTrackParams(track); } }
void AudioEngine::setTrackUnison(int track, int c, float d, float s) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.unisonCount = c; mTracks[track].params.unisonDetune = d; mTracks[track].params.unisonSpread = s; updateTrackParams(track); } }
void AudioEngine::setTrackMorph(int track, float m) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.morph = m; updateTrackParams(track); } }
void AudioEngine::setTrackPhaseDistortion(int track, float p) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.phaseDistortion = p; updateTrackParams(track); } }
void AudioEngine::setTrackPanning(int track, float p) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.panning = p; updateTrackParams(track); } }
void AudioEngine::setTrackVolume(int track, float v) { if (track >= 0 && track < MAX_TRACKS) { mTracks[track].params.masterVolume = v; updateTrackParams(track); } }

void AudioEngine::noteOn(int midiNote, float velocity, int trackId) {
    int shifted = midiNote + (mOctaveShift * 12);
    shifted = std::max(0, std::min(shifted, 127));
    mVoiceManager.noteOn(shifted, velocity, nullptr, 0.0f, trackId);
}

void AudioEngine::noteOff(int midiNote) {
    int shifted = midiNote + (mOctaveShift * 12);
    shifted = std::max(0, std::min(shifted, 127));
    mVoiceManager.noteOff(shifted);
}

void AudioEngine::padNoteOn(int padIndex, float velocity) {
    if (padIndex < 0 || padIndex >= MAX_PADS) return;
    float panning = mPadPanning[padIndex];
    if (mPadBuffers[padIndex].empty()) mVoiceManager.noteOn(60 + padIndex, velocity, nullptr, panning, 0);
    else mVoiceManager.noteOn(60 + padIndex, velocity, &mPadBuffers[padIndex], panning, 0);
}

void AudioEngine::padNoteOff(int padIndex) { if (padIndex >= 0 && padIndex < MAX_PADS) mVoiceManager.noteOff(60 + padIndex); }
void AudioEngine::setPadLooping(int padIndex, bool looping) { if (padIndex >= 0 && padIndex < MAX_PADS) mVoiceManager.setPadLooping(60 + padIndex, looping); }

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    float *output = static_cast<float *>(audioData);
    int32_t channelCount = audioStream->getChannelCount();
    mMidiSequencer.process(numFrames, mSamplesPerBeat, mVoiceManager);
    for (int i = 0; i < numFrames; ++i) {
        float left = 0.0f, right = 0.0f;
        mVoiceManager.nextSample(left, right);
        if (mMetronomeEnabled) { float met = getMetronomeSample(); left += met; right += met; }
        mDelay.process(left, right, left, right);
        mReverb.process(left, right, left, right);
        left = left / (1.0f + fabsf(left)); right = right / (1.0f + fabsf(right));
        float monoSum = (left + right) * 0.5f;
        mVizQueue.push(monoSum); mFftQueue.push(monoSum);
        if (mIsRecording.load()) { mRecordQueue.push(left); mRecordQueue.push(right); }
        output[i * channelCount] = left; if (channelCount > 1) output[i * channelCount + 1] = right;
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t AudioEngine::getVisualizerData(float* buffer, int32_t size) { int32_t count = 0; while (count < size && mVizQueue.pop(buffer[count])) count++; return count; }
int32_t AudioEngine::getFftData(float* buffer, int32_t size) { if (size < FftProcessor::FFT_SIZE / 2) return 0; float s; std::vector<float> all; while (mFftQueue.pop(s)) all.push_back(s); if (all.size() < FftProcessor::FFT_SIZE) return 0; mFftProcessor.process(all.data() + all.size() - FftProcessor::FFT_SIZE, buffer); return FftProcessor::FFT_SIZE / 2; }

void AudioEngine::startRecording(const std::string& path) { mIsRecording = true; mRecordQueue.clear(); mRecordingThread = std::thread(&AudioEngine::recordingLoop, this, path); }
void AudioEngine::stopRecording() { mIsRecording = false; if (mRecordingThread.joinable()) mRecordingThread.join(); }
void AudioEngine::recordingLoop(const std::string& path) { WavEncoder encoder; if (!encoder.init(path, 48000, 2, 128)) return; std::vector<float> pcm(4096); while (mIsRecording || mRecordQueue.size() > 0) { int count = 0; while (count < pcm.size() && mRecordQueue.pop(pcm[count])) count++; if (count > 0) encoder.encode(pcm.data(), count); else std::this_thread::sleep_for(std::chrono::milliseconds(10)); } encoder.flush(); encoder.close(); }

void AudioEngine::setBpm(float bpm) { mBpm = bpm; updateMetronomeParams(); }
void AudioEngine::setMetronomeEnabled(bool enabled) { mMetronomeEnabled = enabled; }
void AudioEngine::renderPatternToFile(const std::string& path) {}
int32_t AudioEngine::getXRunCount() { return mStream ? mStream->getXRunCount().value() : 0; }
int32_t AudioEngine::getBufferSize() { return mStream ? mStream->getBufferSizeInFrames() : 0; }
int32_t AudioEngine::getFramesPerBurst() { return mStream ? mStream->getFramesPerBurst() : 192; }
void AudioEngine::checkAndApplyBufferSize() { int32_t req = mRequestedBufferSize.exchange(0); if (req > 0 && mStream) mStream->setBufferSizeInFrames(req); }
void AudioEngine::processExternalMidi(const uint8_t* data, int32_t len) { if (len >= 3) mMidiQueue.push({data[0], data[1], data[2]}); }
void AudioEngine::startPadSampling(int idx) { mSamplingPadIndex = idx; }
void AudioEngine::stopPadSampling() { mSamplingPadIndex = -1; }
void AudioEngine::startAutomatedSampling(int idx, float dur) { mAutoSampleRemaining = static_cast<int32_t>(dur * 48000); startPadSampling(idx); }
void AudioEngine::loadFactorySample(int idx, int sId) {}
void AudioEngine::savePadSample(int idx, const char* path) {}
void AudioEngine::loadPadSample(int idx, const char* path) {}
bool AudioEngine::isPadAvailable(int idx) { return idx >= 0 && idx < MAX_PADS; }
void AudioEngine::renderStereoSampleForTest(float& l, float& r) { mVoiceManager.nextSample(l, r); }
void AudioEngine::onErrorAfterClose(oboe::AudioStream *s, oboe::Result e) {}

void AudioEngine::saveProject(const std::string& directory) { std::vector<std::vector<float>> pads(MAX_PADS); std::vector<float> pans(MAX_PADS); for (int i = 0; i < MAX_PADS; ++i) { pads[i] = mPadBuffers[i]; pans[i] = mPadPanning[i]; } ProjectManager::saveProject(directory, mTracks, mMidiSequencer, pads, pans, mBpm); }
void AudioEngine::loadProject(const std::string& directory) { std::vector<std::vector<float>> pads; std::vector<float> pans; float b; if (ProjectManager::loadProject(directory, mTracks, mMidiSequencer, pads, pans, b)) { mBpm = b; updateMetronomeParams(); for (int t = 0; t < MAX_TRACKS; ++t) updateTrackParams(t); for (int i = 0; i < MAX_PADS && i < pads.size(); ++i) { mPadBuffers[i] = pads[i]; mPadPanning[i] = pans[i]; } } }

void AudioEngine::updateMetronomeParams() { float sr = mStream ? (float)mStream->getSampleRate() : 48000.0f; mSamplesPerBeat = (int32_t)(sr * 60.0f / mBpm); }
bool AudioEngine::isBeatStarted() { return mBeatFlag.exchange(false); }
float AudioEngine::getMetronomeSample() { float s = 0.0f; if (mSampleCounter == 0) mBeatFlag.store(true); if (mSampleCounter < 500) { float freq = (mBeatCounter == 0 ? 880.0f : 440.0f); float sr = mStream ? (float)mStream->getSampleRate() : 48000.0f; float phase = 2.0f * PI_F * freq * (float)mSampleCounter / sr; s = sinf(phase) * 0.5f * (1.0f - (float)mSampleCounter / 500.0f); } mSampleCounter++; if (mSampleCounter >= mSamplesPerBeat) { mSampleCounter = 0; mBeatCounter = (mBeatCounter + 1) % 4; } return s; }
