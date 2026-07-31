#include "Mp3Encoder.h"
#include <android/log.h>

#define TAG "Mp3Encoder"

Mp3Encoder::Mp3Encoder() {
    mBuffer.resize(8192);
}

Mp3Encoder::~Mp3Encoder() {
    close();
}

bool Mp3Encoder::init(const std::string& path, int sampleRate, int channels, int bitRate) {
    mFile = fopen(path.c_str(), "wb");
    if (!mFile) return false;

    mLame = lame_init();
    lame_set_in_samplerate(mLame, sampleRate);
    lame_set_num_channels(mLame, channels);
    lame_set_brate(mLame, bitRate);
    lame_set_quality(mLame, 2); // High quality

    if (lame_init_params(mLame) < 0) {
        close();
        return false;
    }
    return true;
}

void Mp3Encoder::encode(const float* samples, int numSamples) {
    if (!mLame || !mFile) return;

    // LAME expects interleaved floats for stereo, or separate for mono
    // Our samples are mono.
    int result = lame_encode_buffer_ieee_float(mLame, samples, samples, numSamples, mBuffer.data(), mBuffer.size());
    if (result > 0) {
        fwrite(mBuffer.data(), 1, result, mFile);
    }
}

void Mp3Encoder::flush() {
    if (!mLame || !mFile) return;
    int result = lame_encode_flush(mLame, mBuffer.data(), mBuffer.size());
    if (result > 0) {
        fwrite(mBuffer.data(), 1, result, mFile);
    }
}

void Mp3Encoder::close() {
    if (mLame) {
        lame_close(mLame);
        mLame = nullptr;
    }
    if (mFile) {
        fclose(mFile);
        mFile = nullptr;
    }
}
