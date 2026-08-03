# Implementation Plan - Milestone 11: Sample Mapping & Workspace Layout

Adding a sound browser sidebar and layout controls to streamline the sampling workflow and maximize grid visibility.

## User Review Required

> [!IMPORTANT]
> - A "Sample Browser" will be added as a sidebar (left side) containing a list of available sound files.
> - Clicking a sample in the browser will enter "Mapping Mode," where the next pad touched will have that sample assigned.
> - A "Zen Mode" (Config Visibility) toggle will be implemented to hide all synth parameter sliders, giving full screen height to the sequencer and pad grid.
> - This requires a responsive update to the `ScrollView` and `KeyboardPadView` constraints.

## Proposed Changes

### UI & Layout (Kotlin)

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add a vertical `LinearLayout` (sidebar) on the left of the `ScrollView`.
- Add a `Button` "ZEN" to the top header or control bar.
- Wrap parameter modules (ADSR, LFO, etc.) in a container that can be set to `visibility = GONE`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement the Zen Mode toggle logic.
- Implement the "Sample Browser" logic (simulating a list of factory samples for now).

### Native Engine (C++)

#### [MODIFY] [AudioEngine.h/cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Add a JNI method to load factory PCM data into pad buffers.

## Verification Plan

### Automated Tests
- **Instrumented Test**: Verify that toggling Zen Mode correctly hides the parameter bars and expands the remaining views.
- **Unit Test**: Verify that sample mapping correctly updates the pad assignment state.

### Manual Verification
- Open the Sample Browser.
- Select "Fat Kick".
- Touch Pad 0.
- Verify Pad 0 now plays the "Fat Kick" sample instead of the previous sound.
- Toggle "ZEN" mode and verify the UI collapses parameters and shows a large pad grid.
