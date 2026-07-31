# Implementation Plan - Audio Stability & Stress Testing

This plan addresses the reported audio crashes during high BPM metronome usage and the "no sound" issue after playing multiple notes.

## User Review Required

> [!IMPORTANT]
> - I will change the audio mixing logic from a "mean" (averaging) to a "sum" approach. This prevents the volume from dropping significantly as more notes are played.
> - I will implement Oboe's `onErrorAfterClose` callback to automatically restart the audio engine if the stream is disconnected by the system (e.g., due to underruns at high BPM).
> - I will add a safety threshold to the envelope deactivation to ensure voices reach the `Idle` state reliably, preventing voice leakage and silent "active" voices.

## Proposed Changes

### Audio Engine (C++)

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Inherit from `oboe::AudioStreamErrorCallback`.
- Declare `onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error)` to restart the engine.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Update `start()` to set the error callback.
- Implement `onErrorAfterClose()` to log the error and call `start()`.
- Update `onAudioReady()` to include the metronome in the final mix more safely.

#### [MODIFY] [VoiceManager.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- Remove the division by `activeCount` in `nextSample()`.
- Instead, sum the active voices and apply the master volume. This preserves consistent per-note loudness.

#### [MODIFY] [Envelope.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Envelope.cpp)
- Add a small epsilon check in `nextLevel()` Release phase: if `mCurrentLevel < 0.0001f`, force state to `Idle`.

### Testing

#### [MODIFY] [SoundOutputTest.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/androidTest/java/ch/schmidlins/mini_synth/audio/SoundOutputTest.kt)
- Add `testMetronomeStress()`: Sets BPM to 240, starts metronome, waits, and verifies engine is still running and producing data.
- Add `testPolyphonyStress()`: Triggers 32 notes rapidly and verifies that sound is still produced and the engine hasn't crashed.

## Verification Plan

### Automated Tests
- Run all instrumentation tests: `./gradlew :app:connectedDebugAndroidTest`.
- Pay close attention to the new stress tests.

### Manual Verification
- Deploy to emulator.
- Set metronome to 240 BPM and let it run for 10+ seconds.
- Play many notes on the keyboard simultaneously.
- Verify volume remains consistent and no "silence crash" occurs.
