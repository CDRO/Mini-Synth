# Code Review: Visual MIDI Sequencer

## Review Cycle 1

### [Review 1] [Logic] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- **Observation**: `stepDurationSamples` calculation was `samplesPerBeat * mStepDivision * 4.0f`.
- **Logic Error**: Resulted in 1 beat per step instead of 1/4 beat for 1/16th notes.
- **Status**: **FIXED** (Removed `* 4.0f`).

## Review Cycle 2

### [Review 2] [Robustness] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- **Observation**: `setStepDuration` did not validate input.
- **Status**: **FIXED** (Added clamping).

## Review Cycle 3

### [Review 3] [Thread Safety] [MidiSequencer.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.h)
- **Observation**: `mIsPlaying` and `mStepDivision` were modified from JNI but read in the audio thread without protection.
- **Status**: **FIXED** (Used `std::atomic`).

## Review Cycle 4

### [Review 4] [Resource Management] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: UI poller ran continuously.
- **Status**: **FIXED** (Poller stopped in `onStop`).

## Review Cycle 5

### [Review 5] [Logic] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- **Observation**: Notes were released and re-triggered instantly.
- **Status**: **FIXED** (Added 90% Gate logic).

## Review Cycle 6

### [Review 6] [Technical Debt] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- **Observation**: `triggerStep` used a hardcoded velocity of `0.8f`.
- **Status**: **FIXED** (Added `mVelocity` member).

## Review Cycle 7

### [Review 7] [UX] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: Current step indicator was `alpha = 0.5f`, which was barely visible.
- **Status**: **FIXED** (Used `setBackgroundColor` with `Acid Green`).

## Review Cycle 8

### [Review 8] [Stability] [MidiSequencer.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- **Observation**: `reset()` did not stop active notes.
- **Status**: **FIXED** (Added `releaseStep` to `reset`).

## Review Cycle 9

### [Review 9] [Robustness] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- **Observation**: JNI methods did not validate ranges.
- **Status**: **FIXED** (Added range checks).

## Review Cycle 10

### [Review 10] [Performance] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: `resources.getIdentifier` is used inside loops and in the UI poller to find sequencer step buttons.
- **Expected Change**: Pre-cache the button IDs in a `List<Int>` during initialization.
- **Reason**: Avoid the overhead of string-based resource lookups in the UI thread, adhering to Android performance best practices.
