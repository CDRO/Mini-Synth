cmake_minimum_required(VERSION 3.22.1)
project("mini_synth")

find_package(oboe REQUIRED CONFIG)

add_library(mini_synth SHARED
        native-lib.cpp
        AudioEngine.cpp
        Oscillator.cpp
        Voice.cpp
        VoiceManager.cpp
        Envelope.cpp
        Lfo.cpp
        Filter.cpp
        Mp3Encoder.cpp
        MidiSequencer.cpp
        SamplePlayer.cpp
)

target_include_directories(mini_synth PRIVATE . lame)

target_link_libraries(mini_synth
        oboe::oboe
        log
)

# Testing
if (NOT ANDROID_PLATFORM)
    include(FetchContent)
    FetchContent_Declare(
      googletest
      URL https://github.com/google/googletest/archive/03597a01eeebcda3a818e9769796667a5e01bbd3.zip
    )
    # For Windows: prevent overriding the parent project's compiler/linker settings
    set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(googletest)

    add_executable(synth_tests
        tests/smoke_test.cpp
        tests/OscillatorTest.cpp
        tests/FilterTest.cpp
        tests/EnvelopeTest.cpp
        Oscillator.cpp
        Filter.cpp
        Envelope.cpp
        Lfo.cpp
    )
    target_include_directories(synth_tests PRIVATE . tests)
    target_link_libraries(synth_tests GTest::gtest_main)

    include(GoogleTest)
    gtest_discover_tests(synth_tests)
endif()
