# Implementation Plan - Milestone 37: Recording Export & Sample Sharing

Provide users with the ability to export their creations to high-quality audio files and share them via standard Android system intents.

## User Review Required

> [!NOTE]
> **Export Format**: For this milestone, we will prioritize high-quality **WAV (16-bit PCM)** as it requires minimal processing overhead. MP3 support via LAME is already integrated into the real-time recording path, but high-speed offline rendering will start with WAV.

## Proposed Changes

### [Logic / Audio Engine]

#### [MODIFY] [SynthManager.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/audio/SynthManager.kt)
- Expose `renderPatternToFile(path: String)` via JNI.

#### [MODIFY] [AudioEngine.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp)
- Implement `renderPatternToFile`. This function will:
    1.  Temporarily pause the real-time callback if necessary (or use a separate engine instance).
    2.  Iterate through the sequencer pattern steps.
    3.  Generate PCM frames by calling `mVoiceManager.nextSample()` for each step's duration.
    4.  Write the resulting buffer to a file in WAV format.

### [UI / Android]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Implement the "EXP" button listener in the sequencer section.
- Add a progress dialog to indicate background rendering status.
- Implement `shareFile(file: File)` using `FileProvider` and `Intent.ACTION_SEND`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/AndroidManifest.xml)
- Ensure a `FileProvider` is configured for sharing from the app's internal storage.

## Verification Plan

### Manual Verification
- Create a simple 4-step sequence.
- Click "EXP".
- Verify the progress dialog appears and completes.
- Confirm the Android Share Sheet opens with the rendered file.
- Share to a "File Manager" or "Drive" and verify the audio content plays correctly on an external player.
