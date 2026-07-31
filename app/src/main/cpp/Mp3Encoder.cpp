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

    __android_log_print(ANDROID_LOG_WARN, TAG, "USING STUBBED LAME ENCODER. Install LAME to enable MP3.");
    // mLame = lame_init();
    // ...
    return true;
}

void Mp3Encoder::encode(const float* samples, int numSamples) {
    if (!mFile) return;
    // Stub: just write raw PCM to file (pretending it's MP3 for build testing)
    fwrite(samples, sizeof(float), numSamples, mFile);
}

void Mp3Encoder::flush() {
    if (!mFile) return;
}

void Mp3Encoder::close() {
    if (mFile) {
        fclose(mFile);
        mFile = nullptr;
    }
}
