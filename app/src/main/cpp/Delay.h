#ifndef MINI_SYNTH_DELAY_H
#define MINI_SYNTH_DELAY_H

#include <vector>
#include <stdint.h>

class Delay {
public:
    Delay();
    void setSampleRate(int32_t sampleRate);
    void setTime(float seconds);
    void setFeedback(float feedback);
    void setMix(float mix);

    float process(float input);

private:
    float mSampleRate = 48000.0f;
    float mFeedback = 0.5f;
    float mMix = 0.5f;

    std::vector<float> mBuffer;
    size_t mWriteIndex = 0;
    float mCurrentDelaySamples = 0.0f;
    float mTargetDelaySamples = 0.0f;

    void updateDelayLength();
};

#endif //MINI_SYNTH_DELAY_H
