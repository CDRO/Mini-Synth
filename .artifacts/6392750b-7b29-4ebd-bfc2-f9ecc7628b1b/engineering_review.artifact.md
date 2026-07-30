# Engineering Review: Oboe Setup & Native Engine

## Review Phase 1

### [Review 1] [Optimization] [AudioEngine.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/AudioEngine.cpp#L45)
- **Observation**: The current loop for silence is functional but inefficient for large buffers.
- **Expected Change**: Use `memset` or `std::fill` for clearing the buffer.
- **Reason**: Better utilization of platform-specific memory clearing optimizations.

### [Review 2] [Architecture] [native-lib.cpp](file:///C:/Users/schmidlintiz/Projekte/Mini-Synth/app/src/main/cpp/native-lib.cpp)
- **Observation**: The `engine` pointer is a raw static pointer. If `startEngine` is called multiple times without `stopEngine`, it might leak or cause undefined behavior (though currently checked).
- **Expected Change**: Encapsulate the engine management more robustly, perhaps using a `std::unique_ptr` and ensuring thread safety if JNI calls come from different threads.
- **Reason**: Stability and memory safety.

---

## Review Phase 2 (Pending)
