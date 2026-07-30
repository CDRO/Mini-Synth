# Engineering Review: LFO Modulation

## Review Cycles 1-5 (10 Reviews Total)

### Cycle 1
- **Review 1**: [Performance] Lfo::nextValue is called per-sample in Voice::nextSample. The switch statement on waveform is fine, but we should consider if a function pointer or pre-calculated look-up table would be faster for mobile devices if we add more complex modulations.
- **Review 2**: [UX] The LFO target "Filter" is currently a placeholder. We should probably disable this option in the UI or add a comment explaining it's for future use to avoid user confusion.

### Cycle 2
- **Review 3**: [Logic] Lfo::setFrequency uses `static_cast<double>(frequency)`. While safe, we should ensure the input is clamped to a reasonable range (0.01Hz to 20Hz) to avoid extremely fast or static phases that might cause DC offset or aliasing.
- **Review 4**: [Reliability] VoiceManager::nextSample updates LFO parameters for EVERY voice on EVERY sample. This is highly inefficient. Parameters should be updated once per block or only when they actually change.

### Cycle 3
- **Review 5**: [Architecture] Lfo class uses `double` for phase but `float` for output. Given it's a slow oscillator, `float` for everything would likely suffice and save a few cycles on conversions.
- **Review 6**: [Cleanliness] Unused import warning in `LfoTest.kt`. `sampleBase` is assigned but never used. Scrub before merge.

### Cycle 4
- **Review 7**: [UX] Vibrato depth is hardcoded to +/- 1 semitone in `Voice.cpp`. This should be a configurable parameter or at least mentioned in the design guide as the "Vibrato Limit".
- **Review 8**: [Reliability] Lfo::nextValue uses `sinf`. For LFO frequencies (very low), floating point precision for sine is definitely enough, but the phase increment should remain high precision to avoid "drift" over long periods.

### Cycle 5
- **Review 9**: [Testing] LfoTest only checks for "amplitude variance". A more specific test would verify that the variance period matches the requested frequency.
- **Review 10**: [Optimization] `std::abs` in triangle wave calculation. Use `fabsf` to stay in float world consistently.
