#ifndef MINI_SYNTH_AUDIOCOMMON_H
#define MINI_SYNTH_AUDIOCOMMON_H

enum class Waveform {
    Sine,
    Square,
    Saw,
    Triangle,
    Morph,
    Wavetable,
    Random
};

enum class LfoTarget {
    Pitch,
    Volume,
    Filter,
    PhaseDistortion
};

enum class ArpMode {
    Off,
    Up,
    Down,
    UpDown,
    Random
};

enum class ChordMode {
    Off,
    Major,
    Minor,
    Diminished,
    Augmented,
    Major7,
    Minor7,
    Dominant7
};

struct EngineParams {
    Waveform waveform;
    float attack, decay, sustain, release;
    float masterVolume;
    float lfoRate, lfoDepth;
    Waveform lfoWaveform;

    // Matrix Weights [Pitch, Vol, Filter, PD]
    float lfoMatrix[4];
    bool lfoSync;
    float lfoSyncDivision; // Beats per cycle
    LfoTarget aftertouchTarget;

    float filterCutoff, filterResonance;
    bool isPolyphonic;
    float pitchBend;
    float modulation;
    float panning;
    int unisonCount;
    float unisonDetune;
    float unisonSpread;
    float morph;
    float phaseDistortion;

    // Performance
    ArpMode arpMode = ArpMode::Off;
    float arpDivision = 0.25f;
    int arpOctaves = 1;
    ChordMode chordMode = ChordMode::Off;
    int chordInversion = 0;
};

struct SampleMetadata {
    uint32_t start = 0;
    uint32_t end = 0;
    bool reverse = false;
    float gain = 1.0f;
};

#endif //MINI_SYNTH_AUDIOCOMMON_H
