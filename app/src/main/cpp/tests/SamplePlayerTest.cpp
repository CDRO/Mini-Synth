#include <gtest/gtest.h>
#include "SamplePlayer.h"
#include <vector>

TEST(SamplePlayerTest, BoundsAndReverse) {
    SamplePlayer player;
    std::vector<float> buffer = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};

    // Test normal playback
    player.setBounds(1, 4); // Indices 1, 2, 3 (values 0.1, 0.2, 0.3)
    player.trigger(buffer);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.1f);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.2f);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.3f);
    EXPECT_FALSE(player.isActive());

    // Test reverse playback
    player.setReversed(true);
    player.trigger(buffer);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.3f);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.2f);
    EXPECT_FLOAT_EQ(player.nextSample(), 0.1f);
    EXPECT_FALSE(player.isActive());
}

TEST(SamplePlayerTest, ZeroCrossing) {
    std::vector<float> buffer = {0.5f, 0.3f, 0.1f, -0.1f, -0.3f, 0.1f};

    // Zero crossing between index 2 (0.1) and 3 (-0.1)
    uint32_t zc = SamplePlayer::findZeroCrossing(buffer, 1);
    EXPECT_EQ(zc, 3); // 3 is the first sample with a different sign than 2

    // Zero crossing between index 4 (-0.3) and 5 (0.1)
    zc = SamplePlayer::findZeroCrossing(buffer, 4);
    EXPECT_EQ(zc, 5);
}
