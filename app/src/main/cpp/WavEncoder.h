#ifndef MINI_SYNTH_WAVENCODER_H
#define MINI_SYNTH_WAVENCODER_H

#include <string>
#include <vector>
#include <stdint.h>
#include <stdio.h>

struct WavHeader {
    char chunkId[4] = {'R', 'I', 'F', 'F'};
    uint32_t chunkSize = 0;
    char format[4] = {'W', 'A', 'V', 'E'};
    char subchunk1Id[4] = {'f', 'm', 't', ' '};
    uint32_t subchunk1Size = 16;
    uint16_t audioFormat = 1; // PCM (16-bit)
    uint16_t numChannels = 1;
    uint32_t sampleRate = 48000;
    uint32_t byteRate = 0;
    uint16_t blockAlign = 0;
    uint16_t bitsPerSample = 16;
    char subchunk2Id[4] = {'d', 'a', 't', 'a'};
    uint32_t subchunk2Size = 0;
};

class WavEncoder {
public:
    WavEncoder();
    ~WavEncoder();

    bool init(const std::string& path, int sampleRate, int channels, int bitRate);
    void encode(const float* samples, int numSamples);
    void flush();
    void close();

private:
    FILE* mFile = nullptr;
    std::vector<unsigned char> mBuffer;
    int mChannels = 1;
    int mSampleRate = 48000;
};

#endif //MINI_SYNTH_WAVENCODER_H
