# Mini-Synth: Professional Android Synthesizer

Mini-Synth is a high-performance, low-latency polyphonic synthesizer for Android. Built with a C++ (Oboe) audio engine and a modern Kotlin UI, it delivers studio-quality sound with a professional "Stealth Synth" aesthetic.

## 🚀 Key Features

*   **16-Voice Polyphonic Engine**: Native C++ synthesis with Sine, Square, Saw, Triangle, and continuous **Waveform Morphing**.
*   **High-Fidelity Stereo Pipeline**: Interleaved stereo routing with Equal Power Panning and spatial distribution.
*   **Rich Sound Design**:
    *   **Unison Stacking**: Stack up to 8 oscillators per note with adjustable detuning and stereo spread.
    *   **Resonant LPF**: Professional 2-pole low-pass filter with resonance control.
    *   **Modulation Matrix**: ADSR Envelopes, multi-target LFO, and per-voice Aftertouch.
*   **Integrated Effects**: High-density Reverb and Stereo Ping-Pong Delay.
*   **MIDI Sequencer**: 64-step sequencer with real-time recording, quantization, and high-speed offline export (WAV).
*   **Custom Sound Board**: Customizable pad grid with sampling capabilities and bank management.

## 🛠 Technology Stack

*   **Audio Core**: C++17, Oboe (AAudio / OpenSL ES), LAME (MP3 Encoding).
*   **UI/Logic**: Kotlin, Jetpack Lifecycle, Material Design 3, Coroutines.
*   **Persistence**: DataStore (Preferences), Native JSON Serialization (nlohmann/json).
*   **Build System**: Gradle, CMake, NDK (v28+).

## 📥 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer).
*   Android NDK 28.2.13676358+.
*   Physical device or emulator running Android 9.0 (API 28) or higher.

### Building from Source
1.  Clone the repository: `git clone https://github.com/CDRO/Mini-Synth.git`
2.  Open the project in Android Studio.
3.  Ensure NDK and CMake are installed via the SDK Manager.
4.  Sync Gradle and run the `:app` module.

## 🤝 Contributing

We follow a rigid **10-step iterative review loop** for all features. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
