# Implementation Plan - Milestone 23: MIDI Loop Recording

Implement real-time MIDI performance capture and automatic quantization to enable seamless loop recording.

## User Review Required

> [!IMPORTANT]
> **Quantization Window**: By default, performance will snap to the nearest 16th note based on the current BPM. An 'Input Quantize' toggle will be added to the sequencer bar.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [MidiSequencer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/MidiSequencer.cpp)
- Add `startRealTimeRecording()` and `stopRealTimeRecording()`.
- Implement quantization logic that maps native timestamps to step indices `[0..15]`.
- Support overdubbing (additive note entry) during playback.

### [UI & Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Update 'REC' button logic:
    - If sequencer is PLAYING, entering REC mode starts loop-overdubbing.
- Add 'IQ' (Input Quantize) toggle to the control bar.

## Verification Plan

### Automated Tests
- **Native Test**: Verify that a timestamp halfway between step 0 and 1 snaps correctly with IQ enabled.

### Manual Verification
- Start sequencer playback.
- Press REC.
- Play a melody on the keyboard.
- Verify that notes are added to the pattern and repeat on the next loop.
