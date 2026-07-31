#ifndef MINI_SYNTH_MP3ENCODER_H
#define MINI_SYNTH_MP3ENCODER_H

#include <string>
#include <vector>
#include "lame/lame.h"

class Mp3Encoder {
public:
    Mp3Encoder();
    ~Mp3Encoder();

    bool init(const std::string& path, int sampleRate, int channels, int bitRate);
    void encode(const float* samples, int numSamples);
    void flush();
    void close();

private:
    lame_global_flags* mLame = nullptr;
    FILE* mFile = nullptr;
    std::vector<unsigned char> mBuffer;
};

#endif //MINI_SYNTH_MP3ENCODER_H
