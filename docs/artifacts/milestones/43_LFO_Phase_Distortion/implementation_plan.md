# Implementation Plan - Milestone 43: LFO Expansion & Phase Distortion

Expand the synthesis capabilities of Mini-Synth with advanced modulation shapes and Casio CZ-style Phase Distortion synthesis.

## User Review Required

> [!NOTE]
> **Phase Distortion (PD)**: This effect will be applied to all waveforms. It works by non-linearly warping the oscillator's phase, creating rich harmonic shifts similar to a resonant filter sweep but with a distinct digital character.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [Lfo.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Lfo.h)
- Add `Random` (Sample & Hold) waveform type.
- Add `mWavetable` support to allow user-defined LFO shapes.
- Implement a `step()` function to handle the random value generation based on frequency.

#### [MODIFY] [Oscillator.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Oscillator.h)
- Add `mPhaseDistortion` parameter (0.0 to 1.0).
- Update `nextSample()` to apply a Casio CZ-style "resonant" phase distortion mapping before waveform generation.

#### [MODIFY] [Voice.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/Voice.h)
- Propagate Phase Distortion amount to sub-oscillators.
- Allow LFO and Aftertouch to target Phase Distortion.

#### [MODIFY] [VoiceManager.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.h)
- Add `phaseDistortion` to `EngineParams`.
- Add `LfoTarget::PhaseDistortion` and `AftertouchTarget::PhaseDistortion`.

### [JNI Bridge]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Add `external fun setPhaseDistortion(value: Float)`.

### [UI / Kotlin]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a "PD" (Phase Distortion) slider in the oscillator section.
- Update LFO Target spinner to include "Phase Dist".

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Bind new PD slider.
- Update LFO target mapping.
- Add Discovery Help for Phase Distortion.

## Verification Plan

### Automated Tests
- **PD Math Verification**: Unit test the phase distortion mapping function to ensure it stays within [0, 2PI] and produces expected "kinks" in the phase line.
- **LFO Randomness**: Verify the Random LFO produces unique values and respects the rate parameter.

### Manual Verification
- Move the PD slider with a Square wave. Verify it adds "brightness" and harmonic movement.
- Assign LFO to Phase Distortion. Verify it creates a "sweeping" timbre effect.
- Use Help mode to verify documentation for the new controls.
