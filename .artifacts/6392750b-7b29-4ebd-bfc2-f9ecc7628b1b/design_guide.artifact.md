# Design Guide: Mini-Synth (FL Studio Aesthetic)

A dark, high-contrast theme inspired by professional DAW environments like FL Studio.

## Color Palette

| Element | Color | Hex Code |
| :--- | :--- | :--- |
| **Main Background** | Deep Charcoal | `#121212` |
| **Surface/Panels** | Matte Grey | `#1A1A1A` |
| **Primary Accent** | Acid Green | `#C0FF00` |
| **Secondary Accent** | Electric Blue | `#00A3FF` |
| **Warning/Record** | Vibrant Red | `#FF3B30` |
| **Text (Primary)** | Off-White | `#E1E1E1` |
| **Text (Secondary)** | Dim Grey | `#8E8E93` |

## UI Components

### 1. Keyboard
- **White Keys**: Light Grey (`#CCCCCC`) to avoid harsh white. Dark grey borders (`#333333`).
- **Black Keys**: Deep Matte Black (`#0A0A0A`).
- **Backlighting (Touch)**: Acid Green Glow (`#C0FF00` at 50% alpha).
- **Backlighting (Playback)**: Electric Blue Glow (`#00A3FF` at 50% alpha).

### 2. Pads
- **Resting State**: Matte Grey (`#1A1A1A`) with Acid Green borders.
- **Active State**: Solid Acid Green with slight inner glow.

### 3. Controls
- **Knobs/Sliders**: Circular "DAW-style" knobs with Electric Blue indicators.
- **Buttons**: Flat, dark grey with colored labels or icons.

## Typography
- **Font**: Monospace or clean Sans-Serif (e.g., Roboto Mono).
- **Style**: All-caps for labels, small font size for technical feel.

## Layout
- **Orientation**: Locked Landscape.
- **Organization**: Group controls into "Modules" (Oscillator, ADSR, Filter) using subtle borders or darker backgrounds.
