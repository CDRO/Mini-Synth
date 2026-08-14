#include "Lfo.h"
#include <algorithm>
#include <cstdlib>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

static const float PI_F = static_cast<float>(M_PI);
static const float TWO_PI_F = 2.0f * PI_F;

Lfo::Lfo() {
    updatePhaseIncrement();
}

void Lfo::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<double>(sampleRate);
    updatePhaseIncrement();
}

void Lfo::setFrequency(float frequency) {
    // Clamp 0.1Hz to 20Hz
    float clamped = frequency;
    if (clamped < 0.1f) clamped = 0.1f;
    if (clamped > 20.0f) clamped = 20.0f;
    mFrequency = static_cast<double>(clamped);
    updatePhaseIncrement();
}

void Lfo::updatePhaseIncrement() {
    mPhaseIncrement = (mFrequency * 2.0 * M_PI) / mSampleRate;
}

float Lfo::nextValue() {
    if (mDepth == 0.0f) return 0.0f;

    float result = 0.0f;
    float phaseFloat = static_cast<float>(mPhase);

    switch (mWaveform) {
        case Waveform::Sine:
            result = sinf(phaseFloat);
            break;
        case Waveform::Square:
            result = (phaseFloat < PI_F) ? 1.0f : -1.0f;
            break;
        case Waveform::Saw:
            result = (phaseFloat / PI_F) - 1.0f;
            break;
        case Waveform::Triangle:
            result = 2.0f * fabsf((phaseFloat / PI_F) - 1.0f) - 1.0f;
            break;
        case Waveform::Random:
            result = mRandomValue;
            break;
        default:
            result = 0.0f;
    }

    mPhase += mPhaseIncrement;
    if (mPhase >= static_cast<double>(TWO_PI_F)) {
        mPhase -= static_cast<double>(TWO_PI_F);
        if (mWaveform == Waveform::Random) {
            mRandomValue = ((static_cast<float>(rand()) / static_cast<float>(RAND_MAX)) * 2.0f) - 1.0f;
        }
    }

    return result * mDepth;
}
