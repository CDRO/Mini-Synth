#ifndef MINI_SYNTH_TRACK_H
#define MINI_SYNTH_TRACK_H

#include "VoiceManager.h"
#include <atomic>

struct Track {
    EngineParams params;
    std::atomic<float> volume{0.8f};
    std::atomic<float> panning{0.0f};
    std::atomic<bool> muted{false};
    std::atomic<bool> soloed{false};

    Track() {
        params.waveform = Waveform::Sine;
        params.attack = 0.1f;
        params.decay = 0.1f;
        params.sustain = 0.8f;
        params.release = 0.1f;
        params.masterVolume = 0.8f;
        params.lfoRate = 1.0f;
        params.lfoDepth = 0.0f;
        params.lfoWaveform = Waveform::Sine;
        params.lfoTarget = LfoTarget::Pitch;
        params.filterCutoff = 1000.0f;
        params.filterResonance = 0.5f;
        params.isPolyphonic = true;
        params.pitchBend = 0.0f;
        params.modulation = 0.0f;
        params.panning = 0.0f;
        params.unisonCount = 1;
        params.unisonDetune = 0.0f;
        params.unisonSpread = 0.0f;
        params.morph = 0.0f;
        params.phaseDistortion = 0.0f;
    }
};

#endif //MINI_SYNTH_TRACK_H
