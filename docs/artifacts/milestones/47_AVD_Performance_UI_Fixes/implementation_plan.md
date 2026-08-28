# Implementation Plan - Milestone 47: Virtual Device Performance & UI Polish

Address audio stability in emulated environments and resolve overlapping UI elements.

## Proposed Changes

### [Audio Engine / Performance]

#### [ANALYSIS] Virtual Device Output
- Investigate buffer under-runs in AVD environments.
- Measure JNI round-trip latency in emulators.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Optimize the audio callback for lower CPU usage.
- Implement a more aggressive buffer auto-scaling logic for emulators (detecting x86/x86_64 arch).

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- **Fix Playmode Button Overlap**: 
    - Move `toggle_pads_fullscreen` away from the header area.
    - Reposition it to ensure it doesn't overlap with the `tv_bpm_value` in the header.
- **Fix Bank Select Overlap**:
    - Re-constrain `toggle_keyboard` or the bank control `LinearLayout`.
    - Ensure `toggle_keyboard` (Hide Keys) doesn't cover the bank selection buttons at the bottom right.

## Verification Plan

### Manual Verification
1.  **Emulator Audio**: Deploy to a standard Pixel 4 AVD. Verify that the sound works continuously without crackling.
2.  **Overlap Check**:
    - Verify `toggle_pads_fullscreen` is clearly visible and not touching the BPM display.
    - Verify the bank buttons are clickable and not obscured by the "▼" (Hide Keys) button.
