#ifndef MINI_SYNTH_FFTPROCESSOR_H
#define MINI_SYNTH_FFTPROCESSOR_H

#include <vector>
#include <complex>

class FftProcessor {
public:
    static const int FFT_SIZE = 1024;

    FftProcessor();
    void process(const float* input, float* magnitudes);

private:
    void fft(std::vector<std::complex<double>>& a, bool invert);
    std::vector<std::complex<double>> mBuffer;
    std::vector<double> mWindow;
};

#endif //MINI_SYNTH_FFTPROCESSOR_H
