# Implementation Plan - Milestone 21: Automated Demo & Sampling

Implement automated sampling and a full engine demonstration to showcase the app's capabilities to new users.

## User Review Required

> [!IMPORTANT]
> **Demo Scope**: The demo will consist of a pre-programmed musical sequence that automatically triggers sampling of its own output into a pad, then plays back the sampled pad with effects.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Add native `autoSampleToPad(int padIndex, int durationSamples)` logic.
- Ensure sample playback triggers correctly after automated recording.

### [UI & Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Enhance `playDemoSong` to include:
    1. Setting up a complex synth patch.
    2. Playing a short melody.
    3. Triggering automated sampling to a specific pad.
    4. Switching to Pad Mode and playing the new sample.
    5. Applying Delay/Reverb to the demonstration.

## Verification Plan

### Automated Tests
- **Integration Test**: Verify that `autoSampleToPad` populates the target buffer.

### Manual Verification
- Click 'DEMO' button.
- Observe the automated workflow: melody -> sampling indicator -> pad playback.
