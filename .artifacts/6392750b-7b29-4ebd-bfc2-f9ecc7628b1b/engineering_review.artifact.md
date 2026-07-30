# Engineering Review: Voice Manager and Oscillators

## Review Phase 1

### [Review 1] [Optimization] [Oscillator.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/Oscillator.cpp)
- **Observation**: Floating point math uses `double` literals and `std::abs` (double version) in the inner loop (`nextSample`).
- **Expected Change**: Use `float` literals (e.g., `2.0f`) and `std::fabsf` or `std::abs` on floats to avoid casting.
- **Reason**: Better performance on mobile CPUs where `float` operations are often faster and use less register pressure in high-frequency audio callbacks.

### [Review 2] [Logic] [VoiceManager.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/VoiceManager.cpp)
- **Observation**: `findFreeVoice` always steals voice `0` if all are active.
- **Expected Change**: Implement a "round-robin" or "least recently used" stealing strategy.
- **Reason**: Stealing the same voice repeatedly creates audible artifacts and prevents natural decay (if added later).

---

## Review Phase 2 (Pending)
