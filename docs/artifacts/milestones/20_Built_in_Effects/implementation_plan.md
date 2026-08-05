# Implementation Plan - Milestone 20: Built-in Effects

Implement Delay and Reverb DSP modules in the C++ engine to enhance the synthesizer's sonic character.

## User Review Required

> [!IMPORTANT]
> **DSP Topology**: Effects will be implemented as a global serial chain (Delay -> Reverb) following the summing mixer in `AudioEngine`. This ensures consistent spatial imaging for all voices.

## Proposed Changes

### [Audio Engine]

#### [NEW] Delay.h / Delay.cpp
- Ring-buffer based digital delay.
- Parameters: Time (samples), Feedback (0-1), Mix (0-1).
- Linear interpolation for smooth time changes.

#### [NEW] Reverb.h / Reverb.cpp
- Lightweight algorithmic reverb (Schroeder or Freeverb-style).
- Parameters: Size, Damping, Mix.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Integrate Delay and Reverb into the `onAudioReady` callback.
- Add thread-safe parameter updates.

### [JNI & UI]

#### [MODIFY] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- Add JNI exports for FX parameters.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Add FX control panel with sliders for Delay and Reverb.

## Verification Plan

### Automated Tests
- **Native Tests**: Unit tests for `Delay` and `Reverb` processing buffers.
- **Math Verification**: Ensure feedback loops don't explode (clamping).

### Manual Verification
- Play keys and verify the audible delay and spatial reverb.
- Stress test rapid feedback changes.
