# Implementation Plan - Milestone 9: Pad Sampling & Playback

Transforming the 4x4 pad grid into a powerful sampler by allowing users to record their keyboard performances directly onto individual pads.

## User Review Required

> [!IMPORTANT]
> - Pads 0-15 will each have a dedicated (initially empty) 5-second PCM buffer.
> - When "Sample to Pad" is active, pressing a pad will begin recording the master output into that pad's buffer.
> - Once recorded, that pad will act as a sample trigger instead of a standard MIDI note trigger when the UI is in "Pads" mode.
> - Samples are currently VOLATILE and not persisted to disk (this is part of Milestone 10).

## Proposed Changes

### Native Audio Engine (C++)

#### [NEW] [SamplePlayer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.h)
- Manage a `std::vector<float>` buffer.
- Implement `nextSample()` with linear interpolation (for future pitch shifting) or simple indexing.
- State: `Idle`, `Playing`, `Recording`.

#### [MODIFY] [Voice.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Voice.h)
- Add `SamplePlayer` instance.
- Update `nextSample()` to choose between `mOscillator` and `mSamplePlayer` based on pad assignment.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Tap the final mixed sample *before* normalization into the active recording pad's buffer.

### UI & Integration (Kotlin)

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "SAMPLING" section with a "MAP TO PAD" toggle.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- If "Map to Pad" is ON and UI is in "Pads" mode, handle pad touches by starting/stopping native sampling.

## Verification Plan

### Automated Tests
- **Unit Test**: `SamplePlayerTest.cpp` to verify that recording 1 second of silence actually fills the buffer with zeros.
- **Unit Test**: Verify playback reaches the end of the buffer and stops.

### Manual Verification
- Select "Sine" waveform.
- Toggle "Map to Pad".
- Switch to "Pads" mode.
- Press Pad 0 and play a few notes on the keyboard.
- Release Pad 0.
- Toggle "Map to Pad" OFF.
- Press Pad 0 and verify the recorded melody plays back.
