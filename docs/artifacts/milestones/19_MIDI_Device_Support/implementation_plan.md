# Implementation Plan - Milestone 19: MIDI Device Support

Integrate USB and Bluetooth MIDI device support via the Android MIDI API (AMidi) to allow external control of the synthesizer.

## User Review Required

> [!IMPORTANT]
> **API Level**: AMidi requires Android API 23+ (USB) and 29+ (Native NDK AMidi). We will use the Java-based `MidiManager` for device discovery and port connection, then route events to the C++ engine via JNI.

## Proposed Changes

### [Audio Engine]

#### [MODIFY] [AudioEngine.h](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.h)
- Add thread-safe methods for processing external MIDI events (Note On, Note Off, Control Change).

#### [MODIFY] [native-lib.cpp](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- Add JNI exports for routing external MIDI data.

### [Android MIDI Integration]

#### [NEW] MidiDeviceManager.kt
- Implement `MidiManager` listener for device attachment/detachment.
- Handle USB-MIDI and BLE-MIDI discovery.
- Implement `MidiReceiver` to capture bytes and send them to `SynthManager`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
- Initialize `MidiDeviceManager`.
- Add UI indicators for connected MIDI devices.

## Verification Plan

### Automated Tests
- **Unit Tests (Kotlin)**: Mock `MidiDevice` and verify byte parsing.
- **Integration Tests**: Verify that calling the JNI MIDI methods correctly triggers engine voices.

### Manual Verification
- Connect a physical USB MIDI Keyboard.
- Verify Note On/Off responsiveness and low latency.
- Test CC messages (if implemented) for filter cutoff control.
