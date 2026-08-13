#include "WavEncoder.h"
#include <android/log.h>
#include <algorithm>

#define TAG "SynthEngine_WavEncoder"

WavEncoder::WavEncoder() {
    mBuffer.resize(8192);
}

WavEncoder::~WavEncoder() {
    close();
}

bool WavEncoder::init(const std::string& path, int sampleRate, int channels, int bitRate) {
    mFile = fopen(path.c_str(), "wb");
    if (!mFile) return false;

    mChannels = channels;
    mSampleRate = sampleRate;

    WavHeader header;
    header.sampleRate = sampleRate;
    header.numChannels = channels;
    header.bitsPerSample = 16;
    header.byteRate = sampleRate * channels * (header.bitsPerSample / 8);
    header.blockAlign = channels * (header.bitsPerSample / 8);

    fwrite(&header, sizeof(WavHeader), 1, mFile);
    return true;
}

void WavEncoder::encode(const float* samples, int numSamples) {
    if (!mFile) return;

    // Convert 32-bit Float to 16-bit PCM (Short) for maximum compatibility
    std::vector<int16_t> intBuffer(numSamples);
    for (int i = 0; i < numSamples; ++i) {
        float s = std::max(-1.0f, std::min(samples[i], 1.0f));
        intBuffer[i] = static_cast<int16_t>(s * 32767.0f);
    }

    fwrite(intBuffer.data(), sizeof(int16_t), numSamples, mFile);
}

void WavEncoder::flush() {
    if (!mFile) return;
    fflush(mFile);
}

void WavEncoder::close() {
    if (mFile) {
        long fileSize = ftell(mFile);

        WavHeader header;
        header.sampleRate = mSampleRate;
        header.numChannels = mChannels;
        header.bitsPerSample = 16;
        header.byteRate = mSampleRate * mChannels * (header.bitsPerSample / 8);
        header.blockAlign = mChannels * (header.bitsPerSample / 8);
        header.chunkSize = fileSize - 8;
        header.subchunk2Size = fileSize - sizeof(WavHeader);

        fseek(mFile, 0, SEEK_SET);
        fwrite(&header, sizeof(WavHeader), 1, mFile);

        fclose(mFile);
        mFile = nullptr;
    }
}
