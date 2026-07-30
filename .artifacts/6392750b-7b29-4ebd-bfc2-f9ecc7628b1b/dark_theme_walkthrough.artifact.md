# Walkthrough: Dark Theme and Device Compatibility

Implemented a dark, FL Studio-inspired aesthetic and adjusted the SDK configuration to support standard virtual devices (Android 9.0+).

## Changes Made

### Design & Aesthetic
- **FL Studio Palette**: Defined a high-contrast dark theme in `colors.xml` and `themes.xml` using Charcoal (#121212), Acid Green (#C0FF00), and Electric Blue (#00A3FF).
- **Custom View Styling**: Updated `KeyboardPadView.kt` to use the new color palette. Touch feedback now glows with Acid Green, and key colors are desaturated to reduce eye strain.
- **Theme Consistency**: Consolidated `themes.xml` across night/day modes to ensure a consistent dark "DAW" feel regardless of system settings.

### Infrastructure & Compatibility
- **SDK Adjustment**: Lowered `minSdk` to **28 (Android 9.0)** and aligned `compileSdk`/`targetSdk` to **35**. This allows the app to run on common emulators while retaining modern build features.
- **Oboe Maintenance**: Added technical documentation within the native code acknowledging Oboe's automatic fallback behavior for older API levels.

## Engineering Reviews Applied
Completed **5 rounds of self-review** with individual comments posted to PR #5:
- **Renamed Colors**: Switched from keyboard-specific names (`white_key`) to generic design names (`surface_bright`) for future reuse in knobs and sliders.
- **Enhanced Testing**: Added Espresso checks for the control bar and custom view visibility.
- **Build Integrity**: Fixed resource linking errors caused by removing legacy color definitions.

## Verification Results

### Instrumented Tests (`connectedCheck`)
```text
> Task :app:connectedDebugAndroidTest
Android Test Results
 - device id: 'emulator-5554': 7 PASSED
```

### Visual Verification
- Verified that the "Stealth Synth" aesthetic is correctly applied to the control bar and the keyboard/pad grid.
