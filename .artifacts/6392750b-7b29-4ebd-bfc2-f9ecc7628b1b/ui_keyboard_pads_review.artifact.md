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

## Review Phase 2 (Pending)
