# Implementation Plan - Milestone 26: Professional Demo Experience

Establish a comprehensive, automated demonstration workflow that showcases the synth's full power, from oscillators to effects and automated sampling.

## User Review Required

> [!IMPORTANT]
> **Demo Flow**: The demo will be a 3-part sequence:
> 1. **Synthesis Stage**: Melodic sequence with automated Filter/LFO sweeps.
> 2. **Sampling Stage**: Automated recording of a polyphonic chord into Pad 0.
> 3. **Performance Stage**: Transition to Pad Mode and rhythmic triggering of the new sample with FX swell.

## Proposed Changes

### [UI & Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Refine `playDemoSong` with scripted stages.
- Implement `showDemoToast` for real-time educational overlays.
- Ensure proper UI mode synchronization during automated transitions.

### [Audio Engine]

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Optimize `startAutomatedSampling` to ensure sample integrity at start/stop boundaries.

## Verification Plan

### Automated Tests
- **UI Test (Robolectric)**: Trigger 'DEMO' and verify `isDemoPlaying` state and basic voice activity.

### Manual Verification
- Run Demo.
- Verify that Pad 0 contains the sampled chord after Stage 2.
- Verify that FX sliders move (visually or via engine state) during the demo.
