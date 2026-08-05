#ifndef MINI_SYNTH_REVERB_H
#define MINI_SYNTH_REVERB_H

#include <vector>
#include <stdint.h>

class Reverb {
public:
    Reverb();
    void setSampleRate(int32_t sampleRate);
    void setSize(float size);
    void setDamping(float damping);
    void setMix(float mix);

    float process(float input);

private:
    float mSampleRate = 48000.0f;
    float mSize = 0.5f;
    float mDamping = 0.5f;
    float mMix = 0.3f;

    struct CombFilter {
        std::vector<float> buffer;
        size_t index = 0;
        float feedback = 0.8f;
        float damping = 0.2f;
        float filterState = 0.0f;

        float process(float input) {
            float output = buffer[index];
            filterState = (output * (1.0f - damping)) + (filterState * damping);
            buffer[index] = input + (filterState * feedback);
            index = (index + 1) % buffer.size();
            return output;
        }
    };

    struct AllPassFilter {
        std::vector<float> buffer;
        size_t index = 0;
        float feedback = 0.5f;

        float process(float input) {
            float bufVal = buffer[index];
            float output = -input + bufVal;
            buffer[index] = input + (bufVal * feedback);
            index = (index + 1) % buffer.size();
            return output;
        }
    };

    static const int NUM_COMBS = 4;
    static const int NUM_ALLPASS = 2;
    CombFilter mCombs[NUM_COMBS];
    AllPassFilter mAllPass[NUM_ALLPASS];

    void initFilters();
};

#endif //MINI_SYNTH_REVERB_H
