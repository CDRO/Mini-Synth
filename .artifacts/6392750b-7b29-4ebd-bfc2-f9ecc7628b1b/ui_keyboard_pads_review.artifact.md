# Engineering Review: UI Keyboard and Pads

## Review Phase 1

### [Review 1] [Performance] [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- **Observation**: `onDraw` creates multiple `RectF` objects on every frame.
- **Expected Change**: Pre-calculate and cache `RectF` objects in `onSizeChanged`.
- **Reason**: Allocating objects in the drawing path triggers frequent GC pauses, which can cause UI stutter and potentially impact audio timing if the main thread is heavily loaded.

### [Review 2] [Logic] [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- **Observation**: `onTouchEvent` does not correctly handle multi-touch for note release. Pointer IDs are not tracked.
- **Expected Change**: Use a map to track `pointerId -> midiNote`. Handle `ACTION_POINTER_UP` and `ACTION_MOVE` (if a finger slides across keys).
- **Reason**: A synthesizer needs robust multi-touch support for chords and fluid playing.

---

## Review Phase 2

### [Review 3] [Architecture] [KeyboardPadView.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/ui/KeyboardPadView.kt)
- **Observation**: `KeyboardPadView` instantiates its own `SynthManager`. This duplicates the native engine management (even if the native part is static/singleton).
- **Expected Change**: Remove `SynthManager` from the View. Use a listener/callback interface to notify the Activity/ViewModel of note events.
- **Reason**: Separation of concerns. Views should only handle presentation and input, not business logic or engine management.

### [Review 4] [UX] [MainActivity.kt](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- **Observation**: The `Octave Shift` buttons allow values from -4 to 4, but there's no visual limit feedback other than the text.
- **Expected Change**: Disable the buttons when the limit is reached.
- **Reason**: Better user guidance and preventing redundant clicks.
