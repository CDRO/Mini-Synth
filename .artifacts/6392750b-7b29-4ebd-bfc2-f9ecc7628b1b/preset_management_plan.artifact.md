# Implementation Plan: Preset Management

Implement a robust system for saving and loading synthesizer configurations (Presets). This allows users to capture their sound designs (Waveform, ADSR, LFO, Filter) and recall them later.

## User Review Required

> [!IMPORTANT]
> Preset storage will use **Jetpack DataStore (Preferences)** for simplicity and reliability. JSON serialization will be handled by **Kotlinx Serialization**.

## Proposed Changes

### [Dependency Management]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/tizia/Projekte/Mini-Synth/gradle/libs.versions.toml)
- Add versions and library definitions for `datastore-preferences` and `kotlinx-serialization`.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/tizia/Projekte/Mini-Synth/app/build.gradle.kts)
- Apply the `kotlinx-serialization` plugin.
- Add implementation dependencies for DataStore and Serialization.

---

### [Logic]

#### [NEW] [SynthPreset.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthPreset.kt)
- Define a `@Serializable` data class `SynthPreset` containing:
    - `name: String`
    - `waveformIndex: Int`
    - `attack, decay, sustain, release: Float`
    - `lfoRate, lfoDepth: Float`
    - `lfoWaveformIndex, lfoTargetIndex: Int`
    - `filterCutoff, filterResonance: Float`

#### [NEW] [PresetRepository.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/PresetRepository.kt)
- Create a class to handle saving and loading a list of `SynthPreset` objects from DataStore.
- Use a `Flow<List<SynthPreset>>` to expose available presets.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Add logic to capture current UI state into a `SynthPreset`.
- Add logic to apply a `SynthPreset` to the UI and `SynthManager`.

---

### [UI]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
- Add "Save" and "Load" buttons in the `control_bar` or a new `preset_bar`.
- I'll add them to the `control_bar` for quick access.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement a `MaterialAlertDialog` to prompt for a preset name on save.
- Implement a `MaterialAlertDialog` with a list to select a preset on load.

## Verification Plan

### Automated Tests
- **Unit Test**: `PresetRepositoryTest` to verify that saving and loading from DataStore works (using a temporary file).
- **Instrumented Test**: `PresetUiTest` to verify that clicking "Save" creates a preset and "Load" restores parameters.

### Manual Verification
- Deploy to device/emulator.
- Tweak filter and ADSR.
- Save as "Bass Lead".
- Reset parameters.
- Load "Bass Lead" and verify sliders jump back to saved positions and sound changes.
