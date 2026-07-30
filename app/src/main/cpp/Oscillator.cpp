#include "Oscillator.h"

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void Oscillator::setFrequency(double frequency) {
    mFrequency = frequency;
    updatePhaseIncrement();
}

void Oscillator::setSampleRate(int32_t sampleRate) {
    mSampleRate = static_cast<double>(sampleRate);
    updatePhaseIncrement();
}

void Oscillator::updatePhaseIncrement() {
    mPhaseIncrement = (mFrequency * 2.0 * M_PI) / mSampleRate;
}

float Oscillator::nextSample() {
    float result = 0.0f;
    float phaseFloat = static_cast<float>(mPhase);

    switch (mWaveform) {
        case Waveform::Sine:
            result = sinf(phaseFloat);
            break;
        case Waveform::Square:
            result = (phaseFloat < static_cast<float>(M_PI)) ? 1.0f : -1.0f;
            break;
        case Waveform::Saw:
            result = (phaseFloat / static_cast<float>(M_PI)) - 1.0f;
            break;
        case Waveform::Triangle:
            result = 2.0f * std::abs((phaseFloat / static_cast<float>(M_PI)) - 1.0f) - 1.0f;
            break;
    }

    mPhase += mPhaseIncrement;
    if (mPhase >= 2.0 * M_PI) {
        mPhase -= 2.0 * M_PI;
    }

    return result;
}
