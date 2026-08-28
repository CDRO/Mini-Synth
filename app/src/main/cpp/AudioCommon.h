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

struct EngineParams {
    Waveform waveform;
    float attack, decay, sustain, release;
    float masterVolume;
    float lfoRate, lfoDepth;
    Waveform lfoWaveform;
    LfoTarget lfoTarget;
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
};

#endif //MINI_SYNTH_AUDIOCOMMON_H
