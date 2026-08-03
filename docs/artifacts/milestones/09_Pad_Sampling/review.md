# Code Review: Pad Sampling & Playback

## Review Cycle 1

### [Review 1] [Performance] [SamplePlayer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.cpp)
- **Observation**: `push_back` was used in the audio thread.
- **Status**: **FIXED** (Pre-allocated buffers and indexing).

## Review Cycle 2

### [Review 2] [Performance] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Observation**: Pad buffers were allocated lazily.
- **Status**: **FIXED** (Reserved in constructor).

## Review Cycle 3

### [Review 3] [Technical Debt] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: UI mode was checked via button text.
- **Status**: **FIXED** (Added `isPadMode`).

## Review Cycle 4

### [Review 4] [Stability] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Observation**: `startPadSampling` was not idempotent.
- **Status**: **FIXED** (Added `stopPadSampling` call).

## Review Cycle 5

### [Review 5] [Logic] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Observation**: Metronome click was being recorded.
- **Status**: **FIXED** (Moved recording tap).

## Review Cycle 6

### [Review 6] [Stability] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Observation**: Concurrent record/play on the same pad was risky.
- **Status**: **FIXED** (Mutual exclusivity added).

## Review Cycle 7

### [Review 7] [Stability] [VoiceManager.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- **Observation**: Summing 16 voices caused digital clipping.
- **Status**: **FIXED** (Added 0.5x gain reduction).

## Review Cycle 8

### [Review 8] [UX] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: No visual feedback during sampling.
- **Status**: **FIXED** (Added Red backlight trigger).

## Review Cycle 9

### [Review 9] [Robustness] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- **Observation**: Pad indices were not validated.
- **Status**: **FIXED** (Added range checks).

## Review Cycle 10

### [Review 10] [Performance] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- **Observation**: `AudioEngine` constructor used a legacy for-loop to initialize pad buffers.
- **Expected Change**: Use a range-based for loop or ensure the initialization is clean.
- **Reason**: Adhere to modern C++ standards and resolve Clang-Tidy static analysis warnings.
