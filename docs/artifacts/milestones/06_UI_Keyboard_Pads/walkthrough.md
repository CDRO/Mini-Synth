# Walkthrough: UI Keyboard and Pads

Implemented a custom, low-latency UI component that provides both a traditional piano keyboard and a 4x4 sound board pad grid.

## Changes Made

### Custom View
- **KeyboardPadView**: Created a custom [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt) that handles:
    - **Dual Modes**: Toggles between a 1-octave keyboard (C4 to C5) and a 4x4 grid of 16 trigger pads.
    - **Multi-Touch**: Robust tracking of multiple pointers to support chords and simultaneous pad triggers.
    - **Backlighting**: Visual feedback using color-coded overlays:
        - `Yellow`: Active touch input.
        - `Red/Blue`: Placeholders for Record/Playback states.
    - **Performance**: Cached `RectF` objects in `onSizeChanged` to ensure zero allocations during the draw loop.

### Integration
- **Layout**: Updated [content_main.xml](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml) with the custom view and a control bar for:
    - Mode switching (Keys/Pads).
    - Waveform selection.
    - Polyphony toggle.
    - Octave shifting (±4 range).
- **MainActivity**: Rewrote [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt) to connect the UI events to the native `SynthManager`.
- **Manifest**: Locked orientation to landscape for optimal keyboard layout.

## Engineering Reviews Applied

### Phase 1
- **Performance**: Eliminated object allocations in `onDraw` by caching key and pad geometries.
- **Precision**: Implemented proper multi-touch pointer tracking to prevent stuck notes and allow fluid playing.

### Phase 2
- **Architecture**: Decoupled the engine from the View by implementing an `OnNoteEventListener` callback.
- **UX**: Added reactive button state (disabling) for the octave shift limits.

## Verification Results
- **Build**: Successfully compiled.
- **Functional**: Verified multi-touch interaction and mode switching in the layout preview/unit tests.
- **Visual**: Backlighting correctly highlights active notes.
