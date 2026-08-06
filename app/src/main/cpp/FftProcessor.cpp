#include "FftProcessor.h"
#include <cmath>

#ifndef PI
#define PI 3.14159265358979323846
#endif

FftProcessor::FftProcessor() {
    mBuffer.resize(FFT_SIZE);
}

void FftProcessor::process(const float* input, float* magnitudes) {
    for (int i = 0; i < FFT_SIZE; ++i) {
        mBuffer[i] = std::complex<double>(input[i], 0.0);
    }

    fft(mBuffer, false);

    // Only need the first half (Nyquist frequency)
    for (int i = 0; i < FFT_SIZE / 2; ++i) {
        magnitudes[i] = static_cast<float>(std::abs(mBuffer[i]));
    }
}

void FftProcessor::fft(std::vector<std::complex<double>>& a, bool invert) {
    int n = a.size();

    for (int i = 1, j = 0; i < n; i++) {
        int bit = n >> 1;
        for (; j & bit; bit >>= 1)
            j ^= bit;
        j ^= bit;

        if (i < j)
            swap(a[i], a[j]);
    }

    for (int len = 2; len <= n; len <<= 1) {
        double ang = 2 * PI / len * (invert ? -1 : 1);
        std::complex<double> wlen(cos(ang), sin(ang));
        for (int i = 0; i < n; i += len) {
            std::complex<double> w(1);
            for (int j = 0; j < len / 2; j++) {
                std::complex<double> u = a[i + j], v = a[i + j + len / 2] * w;
                a[i + j] = u + v;
                a[i + j + len / 2] = u - v;
                w *= wlen;
            }
        }
    }

    if (invert) {
        for (std::complex<double>& x : a)
            x /= n;
    }
}
