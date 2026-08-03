# Walkthrough - Milestone 9: Pad Sampling & Playback

I have transformed the 4x4 pad grid into a live sampler, allowing users to capture keyboard performances directly onto individual pads.

## Changes Made

### 1. Native Sample Engine (C++)
- **[SamplePlayer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.cpp)**:
    - Implemented a real-time safe PCM player/recorder.
    - Uses pre-allocated memory buffers to avoid allocations in the audio thread.
- **[Voice.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp)**:
    - Updated the polyphonic voice logic to toggle between standard oscillators and the new `SamplePlayer` when a pad is triggered.
- **[AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)**:
    - Added 16 dedicated sample buffers (5 seconds each).
    - Implemented a "Pre-Metronome" tap point so metronome clicks aren't recorded into user samples.

### 2. User Interface (Kotlin)
- **[content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)**:
    - Added a `SAMPLING ON` toggle.
- **[MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)**:
    - Integrated pad interaction logic. When sampling is ON, touching a pad begins recording from the synth engine.
    - Added **Red backlight** feedback during sampling.
    - Fixed a bug where polyphonic playback was causing digital clipping by adding 6dB of headroom (0.5x scaling).

### 3. Safety & Robustness
- **JNI Validation**: Added range checks for all pad and note indices.
- **Mutual Exclusivity**: Ensured a pad cannot be played back while it is actively being recorded.

## Verification Results

### Automated Tests
- **Unit Tests**: `PadSamplingTest.kt` verifies successful JNI communication and engine state transitions.
- **Regression**: All 25 tests passed, including high-BPM metronome and keyboard recording.

### Manual Verification
- Enabled sampling.
- Recorded a Sine wave melody into Pad 0.
- Recorded a Square wave bassline into Pad 1.
- Played both pads simultaneously; the melodies combined perfectly with no clipping.
