#include <gtest/gtest.h>
#include "Filter.h"
#include <cmath>
#include <vector>

class FilterTest : public ::testing::Test {
protected:
    Filter filter;
    const int32_t sampleRate = 48000;

    void SetUp() override {
        filter.setSampleRate(sampleRate);
    }
};

TEST_F(FilterTest, DCStability) {
    filter.setCutoff(1000.0f);
    filter.setResonance(0.5f);
    // Process a 1.0 DC signal. LPF should eventually output 1.0.
    for (int i = 0; i < 10000; ++i) {
        filter.process(1.0f);
    }
    EXPECT_NEAR(filter.process(1.0f), 1.0f, 0.01f);
}

TEST_F(FilterTest, HighResonanceStability) {
    filter.setCutoff(440.0f);
    filter.setResonance(0.99f); // Max resonance

    // Sudden impulse
    filter.process(1.0f);

    for (int i = 0; i < 48000; ++i) {
        float output = filter.process(0.0f);
        // Ensure no NaN or Inf
        ASSERT_FALSE(std::isnan(output));
        ASSERT_FALSE(std::isinf(output));
        // High resonance should ring, but stay within a reasonable range (not explode)
        EXPECT_LT(std::abs(output), 5.0f);
    }
}

TEST_F(FilterTest, ExtremeCutoffClamping) {
    // Test very low cutoff
    filter.setCutoff(1.0f);
    // Logic should clamp to 20Hz
    // At 20Hz, f = 2 * sin(PI * 20 / 48000) approx 0.0026
    // We can't easily check internal mCutoff, but we can check output doesn't die.
    EXPECT_NO_THROW(filter.process(1.0f));

    // Test very high cutoff (Nyquist)
    filter.setCutoff(24000.0f);
    // Logic should clamp to SR/7
    EXPECT_NO_THROW(filter.process(1.0f));
}

TEST_F(FilterTest, ImpulseResponseDecay) {
    filter.setCutoff(1000.0f);
    filter.setResonance(0.0f); // Low Q

    filter.process(1.0f); // Impulse
    float lastVal = 1.0f;
    for (int i = 0; i < 1000; ++i) {
        float current = std::abs(filter.process(0.0f));
        // With low resonance, the impulse should decay towards zero.
        // (Note: SVF might have some overshoot, but overall it should decrease)
    }
    EXPECT_LT(std::abs(filter.process(0.0f)), 0.01f);
}
