#include <gtest/gtest.h>
#include "Lfo.h"

class LfoTest : public ::testing::Test {
protected:
    Lfo lfo;
    const int32_t sampleRate = 48000;

    void SetUp() override {
        lfo.setSampleRate(sampleRate);
    }
};

TEST_F(LfoTest, ModulationDepth) {
    lfo.setDepth(0.5f);
    lfo.setFrequency(1.0f);
    lfo.setWaveform(Waveform::Sine);

    float maxVal = -1.0f;
    float minVal = 1.0f;

    for (int i = 0; i < sampleRate; ++i) {
        float val = lfo.nextValue();
        if (val > maxVal) maxVal = val;
        if (val < minVal) minVal = val;
    }

    EXPECT_NEAR(maxVal, 0.5f, 0.001f);
    EXPECT_NEAR(minVal, -0.5f, 0.001f);
}

TEST_F(LfoTest, ZeroDepth) {
    lfo.setDepth(0.0f);
    lfo.setFrequency(1.0f);
    EXPECT_EQ(lfo.nextValue(), 0.0f);
}

TEST_F(LfoTest, FrequencyClamping) {
    lfo.setFrequency(0.001f); // Should clamp to 0.1Hz
    // At 0.1Hz, period is 10s = 480,000 samples.
    // Phase increment = (0.1 * 2 * PI) / 48000 = 2*PI / 480,000
    // After 1 sample, phase should be 2*PI / 480,000
    lfo.setDepth(1.0f);
    lfo.setWaveform(Waveform::Sine);
    lfo.nextValue(); // phase 0
    float val = lfo.nextValue(); // phase inc
    EXPECT_NEAR(val, sinf(2.0f * M_PI / 480000.0f), 1e-6f);
}

TEST_F(LfoTest, WaveformSymmetry) {
    lfo.setWaveform(Waveform::Square);
    lfo.setFrequency(1.0f);
    lfo.setDepth(1.0f);

    // First 24000 samples should be 1.0, next 24000 -1.0
    EXPECT_NEAR(lfo.nextValue(), 1.0f, 1e-5f);
    for (int i = 0; i < 23998; ++i) lfo.nextValue();
    EXPECT_NEAR(lfo.nextValue(), 1.0f, 1e-5f);
    EXPECT_NEAR(lfo.nextValue(), -1.0f, 1e-5f);
}
