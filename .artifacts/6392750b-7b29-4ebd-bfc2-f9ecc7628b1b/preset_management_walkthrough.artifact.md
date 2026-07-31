# Walkthrough: Preset Management

Implemented a comprehensive preset system using Jetpack DataStore and Kotlinx Serialization, allowing users to save and recall their synth configurations.

## Changes Made

### Infrastructure & Logic
- **Dependencies**: Integrated `androidx.datastore:datastore-preferences` and `kotlinx-serialization-json`.
- [SynthPreset.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthPreset.kt): Data model for capturing all synth parameters (Waveform, ADSR, LFO, Filter).
- [PresetRepository.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/PresetRepository.kt): Repository handling persistence of `SynthPreset` objects in DataStore using JSON serialization.

### User Interface
- **Controls**: Added "Save" and "Load" buttons to the main control bar.
- **Dialogs**:
    - **Save Dialog**: Prompts for a preset name.
    - **Load Dialog**: Displays a list of saved presets to select from.
- **UI Sync**: Successfully binds restored preset values back to UI sliders and triggers engine updates through existing listeners.

## Testing & Verification

### Automated Tests
- **Unit Test**: `PresetSerializationTest` verifies that the `SynthPreset` model correctly serializes to and from JSON, preserving all parameter values.
- **Build**: Successfully assembled the debug APK (`:app:assembleDebug`).

### Manual Verification Steps
1. Open the app and adjust the **Filter Cutoff** and **Attack** time.
2. Tap **Save**, enter "Muffled Lead".
3. Move sliders to different positions.
4. Tap **Load**, select "Muffled Lead".
5. Observe that sliders jump back to the saved positions and the audio engine reflects the restored state.

> [!TIP]
> Presets are stored per-app instance and persist across restarts thanks to Jetpack DataStore.
