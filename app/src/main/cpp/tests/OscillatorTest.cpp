#include <gtest/gtest.h>
#include "Oscillator.h"
#include <cmath>

class OscillatorTest : public ::testing::Test {
protected:
    Oscillator osc;
    const int32_t sampleRate = 48000;

    void SetUp() override {
        osc.setSampleRate(sampleRate);
    }
};

TEST_F(OscillatorTest, SineWaveRange) {
    osc.setWaveform(Waveform::Sine);
    osc.setFrequency(100.0);
    for (int i = 0; i < sampleRate; ++i) {
        float s = osc.nextSample();
        EXPECT_GE(s, -1.0001f);
        EXPECT_LE(s, 1.0001f);
    }
}

TEST_F(OscillatorTest, SquareWaveLogic) {
    osc.setWaveform(Waveform::Square);
    osc.setFrequency(1.0); // 1Hz = 1 cycle per second
    // At 48000Hz SR, first 24000 samples should be 1.0, next 24000 should be -1.0
    for (int i = 0; i < 24000; ++i) {
        EXPECT_NEAR(osc.nextSample(), 1.0f, 1e-5f) << "Failure at sample " << i;
    }
    for (int i = 0; i < 24000; ++i) {
        EXPECT_NEAR(osc.nextSample(), -1.0f, 1e-5f) << "Failure at sample " << i + 24000;
    }
}

TEST_F(OscillatorTest, SawWaveLinearity) {
    osc.setWaveform(Waveform::Saw);
    osc.setFrequency(1.0);
    float prev = osc.nextSample();
    for (int i = 1; i < 48000; ++i) {
        float current = osc.nextSample();
        if (current > prev) {
            EXPECT_NEAR(current - prev, 2.0f / 48000.0f, 1e-5f);
        } else {
            // Wrapping occurred
            EXPECT_NEAR(current, -1.0f, 1e-4f);
        }
        prev = current;
    }
}

TEST_F(OscillatorTest, TriangleWaveSymmetry) {
    osc.setWaveform(Waveform::Triangle);
    osc.setFrequency(1.0);
    // Triangle at 1Hz: 0 -> 1 (1/4 cycle), 1 -> -1 (1/2 cycle), -1 -> 0 (1/4 cycle)
    EXPECT_NEAR(osc.nextSample(), -1.0f, 1e-4f); // Starts at phase 0, but Triangle math is 2*abs((p/PI)-1)-1
    // Phase increment per sample = (1 * 2 * PI) / 48000 = PI / 24000
    // After 12000 samples, phase = PI/2 -> result = 2*abs(0.5-1)-1 = 0
    // After 24000 samples, phase = PI -> result = 2*abs(1-1)-1 = -1
    // Wait, let's re-verify Triangle math: 2.0f * std::abs((phaseFloat / PI_F) - 1.0f) - 1.0f
    // phase 0: 2*abs(0-1)-1 = 1.0
    // phase PI/2: 2*abs(0.5-1)-1 = 0.0
    // phase PI: 2*abs(1-1)-1 = -1.0
    // phase 3PI/2: 2*abs(1.5-1)-1 = 0.0
    // phase 2PI: 2*abs(2-1)-1 = 1.0

    osc.setFrequency(1.0);
    EXPECT_NEAR(osc.nextSample(), 1.0f, 1e-4f);
    for (int i = 0; i < 11999; ++i) osc.nextSample();
    EXPECT_NEAR(osc.nextSample(), 0.0f, 1e-3f);
    for (int i = 0; i < 11999; ++i) osc.nextSample();
    EXPECT_NEAR(osc.nextSample(), -1.0f, 1e-3f);
}

TEST_F(OscillatorTest, FrequencyChangeStability) {
    osc.setFrequency(440.0);
    float s1 = osc.nextSample();
    osc.setFrequency(880.0);
    float s2 = osc.nextSample();
    EXPECT_NE(s1, s2);
    osc.resetPhase();
    osc.setPhaseDistortion(0.5f);
    float s2 = osc.nextSample();

    EXPECT_NE(s1, s2);
}

TEST_F(OscillatorTest, FastSinAccuracy) {
    osc.setFrequency(440.0); // Triggers initSineLut
    for (int i = 0; i < 1000; ++i) {
        float phase = (static_cast<float>(i) / 1000.0f) * TWO_PI_F;
        float standard = sinf(phase);
        float fast = osc.fastSin(phase);
        EXPECT_NEAR(standard, fast, 1e-4f);
    }
}

TEST_F(OscillatorTest, PhaseDistortionEffect) {
    osc.setWaveform(Waveform::Sine);
    osc.setFrequency(100.0);

    osc.resetPhase();
    // Render one sample without PD
    osc.setPhaseDistortion(0.0f);
    float s1 = osc.nextSample();

    // Reset and render with PD
    osc.resetPhase();
    osc.setPhaseDistortion(0.5f);
    float s2 = osc.nextSample();

    EXPECT_NE(s1, s2);
}
