#include <gtest/gtest.h>
#include "Envelope.h"

class EnvelopeTest : public ::testing::Test {
protected:
    Envelope env;
    const int32_t sampleRate = 48000;

    void SetUp() override {
        env.setSampleRate(sampleRate);
    }
};

TEST_F(EnvelopeTest, FullLifecycle) {
    env.setAttack(0.1f);  // 4800 samples
    env.setDecay(0.1f);   // 4800 samples
    env.setSustain(0.5f);
    env.setRelease(0.1f); // 4800 samples

    EXPECT_FALSE(env.isActive());
    env.trigger();
    EXPECT_TRUE(env.isActive());

    // Attack phase
    for (int i = 0; i < 2400; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.5f, 0.01f);
    for (int i = 0; i < 2398; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 1.0f, 0.001f);

    // Decay phase
    for (int i = 0; i < 2400; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.75f, 0.01f);
    for (int i = 0; i < 2398; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.5f, 0.001f);

    // Sustain phase
    for (int i = 0; i < 1000; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.5f, 0.001f);

    // Release phase
    env.release();
    for (int i = 0; i < 2400; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.25f, 0.01f);
    for (int i = 0; i < 2398; ++i) env.nextLevel();
    EXPECT_NEAR(env.nextLevel(), 0.0f, 0.001f);
    EXPECT_FALSE(env.isActive());
}

TEST_F(EnvelopeTest, ImmediateRelease) {
    env.setAttack(1.0f);
    env.trigger();
    env.nextLevel();
    EXPECT_TRUE(env.isActive());
    env.release(); // Interrupt attack
    EXPECT_TRUE(env.isActive());
    // Should transition to release state
    float level = env.nextLevel();
    EXPECT_LT(level, 1.0f);
}

TEST_F(EnvelopeTest, ZeroTiming) {
    env.setAttack(0.0f);
    env.setDecay(0.0f);
    env.setSustain(1.0f);
    env.trigger();
    // Should jump to 1.0 immediately (or in 1 sample)
    EXPECT_NEAR(env.nextLevel(), 1.0f, 0.001f);
}
