# Implementation Plan - Milestone 33: Design System & Accessibility

Final polish of the UI to ensure accessibility compliance, localization readiness, and scaling stability across diverse hardware.

## User Review Required

> [!IMPORTANT]
> **Layout Ratios**: This milestone may adjust the 20/30/50 layout ratio to be more dynamic (using constraints and bias) to avoid whitespace on ultra-wide devices.

## Proposed Changes

### [Resources]

#### [MODIFY] [strings.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/values/strings.xml)
*   Extract all hardcoded strings from `content_main.xml`, `layout_header.xml`, and `MainActivity.kt`.

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
*   Replace all hardcoded sizes/margins with dimension resources.
*   Enforce a minimum `12sp` text size for all functional labels.
*   Refine `ConstraintLayout` biased constraints to prevent horizontal stretching on wide screens.

## Verification Plan

### Automated Tests
*   Run `testDebugUnitTest` to ensure no binding regressions.

### Manual Verification
*   Verify that the app scales gracefully on both a narrow phone and a large tablet (emulator).
*   Confirm that all text is legible and follows the "Stealth Synth" aesthetic.
