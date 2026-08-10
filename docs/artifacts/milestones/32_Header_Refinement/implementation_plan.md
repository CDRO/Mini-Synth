# Implementation Plan - Milestone 32: Header Refinement & Engine Status

Modularize and declutter the top header by unifying status indicators and optimizing the information density of the support/demo area.

## User Review Required

> [!IMPORTANT]
> **Status Unification**: I propose merging the MIDI indicator and Latency text into a single horizontal "Engine Status" line to reduce vertical clutter and free up space for the Help and Demo triggers.

## Proposed Changes

### [UI / Layout]

#### [MODIFY] [content_main.xml](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/res/layout/content_main.xml)
*   Refactor the left-hand side of the header to use a `ConstraintLayout` or a nested `LinearLayout` for better alignment.
*   Combine `midi_status_indicator` and `tv_latency_status` into a cohesive group.
*   Introduce `include` tags to move header logic into separate files if it becomes too large.

### [Kotlin / Logic]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/tizia/Projekte/Mini-Synth/app/src/main/java/ch/schmidlins/mini_synth/MainActivity.kt)
*   Update status polling logic to handle the unified UI component.
*   Ensure that Help/Demo mode transitions handle the new header structure correctly.

## Verification Plan

### Manual Verification
*   Verify that MIDI connection status is still clearly visible.
*   Confirm that latency metrics (Buffer size/xRuns) are readable.
*   Ensure that the Demo button has sufficient tap area and doesn't overlap with Help trigger.
