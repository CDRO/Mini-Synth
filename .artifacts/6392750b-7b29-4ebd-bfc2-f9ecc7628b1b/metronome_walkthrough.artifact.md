# Walkthrough: Metronome & BPM Control

Implemented a sample-accurate native metronome system with dynamic BPM control and visual feedback.

## Changes Made

### Native Audio Engine (C++)
- **Tick Generation**: Implemented real-time audio tick generation in `AudioEngine.cpp`.
- **Accent Logic**: Beat 1 of every 4 is accented (880Hz vs 440Hz) using a decaying sine wave burst.
- **Precision Timing**: Timing is calculated based on the active stream's sample rate to ensure jitter-free performance.
- **Master Clamping**: Added a safe output clamp to prevent metronome peaks from clipping the master mix.

### UI & UX Integration
- **BPM Controls**: Added coarse (+/- 5) and fine (+/- 1) adjustment buttons in a new metronome control bar.
- **Visual Beat Indicator**: A dedicated "LED" view in the control bar that flashes in perfect sync with the audio beat using an engine-driven flag.
- **JNI Bridge**: Exposed BPM and metronome state via `SynthManager.kt`.

## Testing & Verification

### Review Cycles
- Successfully completed **10 sequential review cycles** on PR #15.
- Key improvements included: metronome reset on enable, BPM clamping in native code, and standardizing Logcat tags.

### Results
- Verified that metronome remains perfectly in time even when the UI thread is under heavy load (scrolling or parameter tweaking).
- Confirmed that the visual flash is responsive and accurate to the audio clicks.

> [!TIP]
> The metronome provides a solid foundation for the upcoming step-sequencing features by providing a consistent native timing reference.
