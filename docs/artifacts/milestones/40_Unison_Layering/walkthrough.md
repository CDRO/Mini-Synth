# Walkthrough - Milestone 40: Unison & Voice Layering

Enabled thick, cinematic timbres by implementing oscillator stacking (Unison) with adjustable pitch detuning and stereo spread.

## Features Delivered

### 1. High-Performance Unison Engine
- **Oscillator Stacking**: Refactored the `Voice` architecture to support up to 8 sub-oscillators per note.
- **Detune (cents)**: Added a parameter to offset the pitch of unison voices, creating rich chorus and "supersaw" effects.
- **Automatic Normalization**: Implemented power-law amplitude compensation (1/sqrt(N)) to maintain consistent volume as voice count increases.

### 2. Spatial Unison Spread
- **Stereo Distribution**: Unison voices are automatically distributed across the stereo field based on their detune offset, creating a wide and immersive soundstage.
- **Integrated Panning**: The unison spread logic integrates seamlessly with the master and per-pad panning controls.

### 3. Interactive UI Controls
- **Unison Selector**: Added a spinner to the parameter bar to toggle between OFF, 2x, 4x, and 8x unison modes.
- **Detune Slider**: Introduced a high-resolution slider (0-100 cents) for precise control over the thickness of the sound.
- **Visual Feedback**: Real-time "L/C/R" and cents labels provide immediate parameter context.

### 4. Educational Demo Update
- **Unison Masterclass**: The automated demo now includes a dedicated segment that showcases the transition from a thin mono lead to a thick, detuned unison pad.

## Review Loop Summary
1.  **Cycle 1**: Updated `Voice` class to support multiple oscillators with detune and spread logic.
2.  **Cycle 2**: Propagated Unison parameters in `VoiceManager` and updated `EngineParams`.
3.  **Cycle 3**: Implemented unison stereo spreading with a dedicated Spread parameter.
4.  **Cycle 4**: Exposed Unison and Spread parameters to JNI and updated internal routing.
5.  **Cycle 5**: Added Unison Spinner and Detune slider to the UI.
6.  **Cycle 6**: Updated `SynthPreset` and `ProjectManager` to persist Unison and Detune values.
7.  **Cycle 7**: Optimized unison sum normalization by pre-calculating the compensation factor.
8.  **Cycle 8**: Verified high-load unison performance (16 voices * 8x unison) via stress tests.
9.  **Cycle 9**: Updated educational demo to showcase Unison with proper string resources.
10. **Cycle 10**: Final polish, clang-tidy fixes and documentation.

## Verification
- **Unit Tests**: 28/28 passed (including new Unison stress tests).
- **Performance**: Confirmed stable 48kHz stereo processing even with 128 active oscillators (16 voices * 8x unison).
- **Quality**: Verified rich chorus effect and wide stereo spread via headphones.
