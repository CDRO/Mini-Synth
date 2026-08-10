# Implementation Plan - Milestone 35: Performance Visualization & FFT Polish

Refine the real-time frequency analysis and visual feedback to professional standards.

## User Review Required

> [!NOTE]
> **Visual Aesthetic**: The FFT bars will be increased in density (128 bars) and will feature "Peak Caps" that linger at maximum values, providing a more professional and informative display.

## Proposed Changes

### [UI / Visualization]

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- Increase `smoothedMagnitudes` array size to 128.
- Add `peakMagnitudes` and `peakDropSpeeds` arrays for peak tracking.
- Implement frequency grid lines and text labels (100Hz, 1kHz, 10kHz).
- Refine the log-scaling logic for better visibility across the human hearing range.
- Add a "Decay" setting or refine the current smoothing to be more responsive to transients.

### [Kotlin / Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Ensure the visualizer polling rate is optimized for the increased data points.

## Verification Plan

### Manual Verification
- **Responsiveness**: Verify that the FFT bars respond accurately to Sine/Square/Saw waveforms (e.g., seeing harmonics in Sawtooth).
- **Peak Tracking**: Confirm that peaks linger and fall at a pleasing speed.
- **Labels**: Ensure frequency labels are legible and correctly aligned with the log-scale.
