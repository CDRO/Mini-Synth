# Implementation Plan - Milestone 46: Phase Distortion Refinement

Optimize the Phase Distortion synthesis algorithm and provide visual feedback for the timbral warping.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [Oscillator.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Oscillator.cpp)
- **Optimization**: Replace the real-time `sinf()` call in the PD mapping with a high-performance approximation or a pre-calculated lookup table.
- **Math**: Ensure the phase warping logic is branching-minimized for better CPU pipelining.

### [UI / Kotlin]

#### [NEW] [PhaseDistortionView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/PhaseDistortionView.kt)
- Create a new custom view that renders the "Warped Phase" transfer function.
- It will plot the input phase (X) vs the distorted output phase (Y).
- The curve will dynamically update as the user moves the PD slider.

#### [MODIFY] [layout_header.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/layout_header.xml)
- Integrate the `PhaseDistortionView` into the header, potentially as an inset or side-car to the main visualizer.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Link the PD slider to the new `PhaseDistortionView` so it updates in real-time.

## Verification Plan

### Automated Tests
- **Performance Benchmark**: Use a native stress test to compare the CPU usage of the optimized PD math vs the original `sinf` implementation.
- **Visual Bounds**: Ensure the `PhaseDistortionView` curve stays within view bounds for all PD values (0.0 to 1.0).

### Manual Verification
- Move the PD slider. Confirm the new visualization correctly represents the "kink" in the phase line.
- Verify that sound quality remains identical to the original implementation (no new artifacts).
