# Implementation Plan - Milestone 25: Waveform Visualization Refinement (FFT)

Enhance the real-time visualizer by adding a Frequency Spectrum (FFT) display to visualize the harmonic content of the synth output.

## User Review Required

> [!IMPORTANT]
> **Performance Overhead**: Calculating FFT on the main thread or every frame might impact UI performance. We will implement a downsampled FFT calculation in the background or native layer.

## Proposed Changes

### [Audio Engine]

#### [NEW] [FftProcessor.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/FftProcessor.cpp)
- Implement a Radix-2 FFT algorithm for 1024 samples.
- Add windowing (Hann or Hamming) to reduce spectral leakage.

#### [MODIFY] [AudioEngine.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Feed audio tap data to `FftProcessor`.
- Add JNI method `getFftData(floatArray)`.

### [UI & Logic]

#### [MODIFY] [VisualizerView.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/VisualizerView.kt)
- Split view into Waveform (Top half) and Spectrum (Bottom half).
- Use a logarithmic scale for the frequency axis (20Hz to 20kHz).
- Render spectrum bars with Acid Green gradients.

## Verification Plan

### Automated Tests
- **Native Unit Test**: Verify that a pure sine wave at 440Hz produces a single peak in the FFT output.

### Manual Verification
- Play different waveforms (Sine, Square, Saw).
- Verify that the harmonic peaks (overtones) are clearly visible for Square/Saw but not for Sine.
- Verify that the Filter Cutoff visually "cuts" the high-frequency bars in the spectrum.
