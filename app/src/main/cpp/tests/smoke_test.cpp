#include <gtest/gtest.h>
#include "Oscillator.h"

TEST(SmokeTest, OscillatorInitialization) {
    Oscillator osc;
    osc.setSampleRate(48000);
    osc.setFrequency(440.0);
    float sample = osc.nextSample();
    EXPECT_GE(sample, -1.1f);
    EXPECT_LE(sample, 1.1f);
}
