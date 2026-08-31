# Implementation Plan - Milestone 51: Advanced Sampler & Pad Editing

Enhance the Sound Board (Track 0) with professional sample manipulation tools, including trimming, reverse playback, and normalization.

## User Review Required

> [!IMPORTANT]
> **Waveform Editor**: I will implement a dedicated dialog that opens when you long-press a Pad (or click an 'Edit' button). This dialog will display the Pad's PCM data and allow you to interactively drag Start/End markers.

> [!NOTE]
> **Non-Destructive Trimming**: Trimming will be non-destructive in memory; the original buffer is preserved while the playback indices are constrained.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [SamplePlayer.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/SamplePlayer.h)
- Add `mStartSample`, `mEndSample`, and `mIsReversed` properties.
- Update `nextSample()` logic to respect the bounds and playback direction.
- Implement zero-crossing search for smoother loop points.

#### [MODIFY] [AudioEngine.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Add JNI methods to set per-pad start/end/reverse parameters.
- Implement `normalizePad(int padIndex)` in C++.

### [UI / Kotlin]

#### [NEW] [WaveformEditorDialog.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/WaveformEditorDialog.kt)
- A new `DialogFragment` with a custom `WaveformView`.
- Touch listeners for dragging markers.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Trigger the Waveform Editor when long-pressing a pad in "EDIT" mode.
- Update `PadState` to persist the new metadata.

## Verification Plan

### Automated Tests
- **Reverse Test**: Verify that a reversed buffer plays from end to start.
- **Trim Test**: Verify that playback starts exactly at `mStartSample`.

### Manual Verification
- Record a sample. Open Editor. Trim the silence at the beginning. Verify instant update.
- Toggle **REVERSE**. Confirm the sound plays backward.
