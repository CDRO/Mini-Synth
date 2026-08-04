#include "Mp3Encoder.h"
#include <android/log.h>

#define TAG "SynthEngine_Mp3Encoder"

Mp3Encoder::Mp3Encoder() {
    mBuffer.resize(8192);
}

Mp3Encoder::~Mp3Encoder() {
    close();
}

bool Mp3Encoder::init(const std::string& path, int sampleRate, int channels, int bitRate) {
    mFile = fopen(path.c_str(), "wb");
    if (!mFile) return false;

    mChannels = channels;
    mSampleRate = sampleRate;

    WavHeader header;
    header.sampleRate = sampleRate;
    header.numChannels = channels;
    header.byteRate = sampleRate * channels * (header.bitsPerSample / 8);
    header.blockAlign = channels * (header.bitsPerSample / 8);

    fwrite(&header, sizeof(WavHeader), 1, mFile);
    return true;
}

void Mp3Encoder::encode(const float* samples, int numSamples) {
    if (!mFile) return;
    fwrite(samples, sizeof(float), numSamples, mFile);
}

void Mp3Encoder::flush() {
    if (!mFile) return;
    fflush(mFile);
}

void Mp3Encoder::close() {
    if (mFile) {
        long fileSize = ftell(mFile);

        WavHeader header;
        header.sampleRate = mSampleRate;
        header.numChannels = mChannels;
        header.bitsPerSample = 32;
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
