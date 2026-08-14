#include "Oscillator.h"

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

static const float PI_F = static_cast<float>(M_PI);
static const float TWO_PI_F = 2.0f * PI_F;

void Oscillator::setFrequency(double frequency) {
    mFrequency = frequency;
    updatePhaseIncrement();
}

void Oscillator::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<double>(sampleRate);
    updatePhaseIncrement();
}

void Oscillator::updatePhaseIncrement() {
    mPhaseIncrement = (mFrequency * TWO_PI_F) / mSampleRate;
}

void Oscillator::setWavetable(const float* data, int32_t size) {
    mWavetable.assign(data, data + size);
}

float Oscillator::getWaveformSample(Waveform type, float phase) {
    switch (type) {
        case Waveform::Sine:
            return sinf(phase);
        case Waveform::Triangle:
            return 2.0f * std::abs((phase / PI_F) - 1.0f) - 1.0f;
        case Waveform::Saw:
            return (phase / PI_F) - 1.0f;
        case Waveform::Square:
            return (phase < PI_F) ? 1.0f : -1.0f;
        default:
            return 0.0f;
    }
}

float Oscillator::nextSample() {
    float result = 0.0f;
    float phaseFloat = static_cast<float>(mPhase);

    // Apply Phase Distortion
    float distortedPhase = phaseFloat;
    if (mPhaseDistortion > 0.001f) {
        // Warp phase: phase' = phase - PD * sin(phase)
        // This is a classic "resonant" sweep approximation
        distortedPhase = phaseFloat - (mPhaseDistortion * sinf(phaseFloat));

        // Wrap back to [0, TWO_PI]
        while (distortedPhase < 0) distortedPhase += TWO_PI_F;
        while (distortedPhase >= TWO_PI_F) distortedPhase -= TWO_PI_F;
    }

    if (mWaveform == Waveform::Wavetable && !mWavetable.empty()) {
        float normalizedPhase = distortedPhase / TWO_PI_F;
        float tablePos = normalizedPhase * static_cast<float>(mWavetable.size());
        int index1 = static_cast<int>(tablePos) % mWavetable.size();
        int index2 = (index1 + 1) % mWavetable.size();
        float frac = tablePos - floorf(tablePos);
        result = mWavetable[index1] * (1.0f - frac) + mWavetable[index2] * frac;
    } else if (mWaveform == Waveform::Morph) {
        // Sine(0) - Triangle(1) - Saw(2) - Square(3)
        float m = std::max(0.0f, std::min(mMorph, 3.0f));
        int stage = static_cast<int>(floorf(m));
        float frac = m - floorf(m);

        Waveform w1, w2;
        if (stage == 0) { w1 = Waveform::Sine; w2 = Waveform::Triangle; }
        else if (stage == 1) { w1 = Waveform::Triangle; w2 = Waveform::Saw; }
        else { w1 = Waveform::Saw; w2 = Waveform::Square; }

        float s1 = getWaveformSample(w1, distortedPhase);
        float s2 = getWaveformSample(w2, distortedPhase);
        result = s1 * (1.0f - frac) + s2 * frac;
    } else {
        result = getWaveformSample(mWaveform, distortedPhase);
    }

    mPhase += mPhaseIncrement;
    if (mPhase >= static_cast<double>(TWO_PI_F)) {
        mPhase -= static_cast<double>(TWO_PI_F);
    }

    return result;
}
