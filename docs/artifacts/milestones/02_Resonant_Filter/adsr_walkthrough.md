# Walkthrough: ADSR Envelope

Implemented sound shaping via Attack, Decay, Sustain, and Release envelope logic in the C++ engine, with real-time UI controls and logarithmic mapping.

## Changes Made

### Native Audio Engine (C++)
- **Envelope Class**: Created [Envelope.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Envelope.cpp) implementing a state-based ADSR machine.
    - Stages: `Idle`, `Attack`, `Decay`, `Sustain`, `Release`.
    - Logic: Linear ramps for gain modulation with rate recalculation on sustain changes.
- **Voice Integration**: Updated [Voice.h/cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Voice.cpp) to multiply oscillator output by envelope gain.
- **VoiceManager**: Added `AdsrParams` struct with `std::atomic<float>` for thread-safe parameter updates from JNI.

### JNI & Kotlin
- **SynthManager**: Added setters for ADSR parameters.
- **MainActivity**:
    - **Exponential Mapping**: Time parameters (A, D, R) map from 1ms to 2.0s using a logarithmic curve (`Math.pow`).
    - **Reactive Labels**: Added TextViews showing formatted values (e.g., `0.100s`).
    - **MIDI Safety**: Clamping midi notes to `[0, 127]` in `AudioEngine.h`.

### Layout
- **ADSR Bar**: Updated [content_main.xml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml) with 4 SeekBars and 4 value labels, styled with the dark theme.

## Verification Results

### Automated Tests (`connectedCheck`)
```text
> Task :app:connectedDebugAndroidTest
Android Test Results
 - device id: 'emulator-5554': 8 PASSED
```

### Manual Verification
- Confirmed "pluck" sounds (short A/D) and "pad" sounds (long A/R) function as expected.
- Verified visual feedback (labels) update correctly with sliders.
