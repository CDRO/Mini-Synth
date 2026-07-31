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

        # LAME sources (Must be provided in src/main/cpp/lame)
        lame/bitstream.c
        lame/encoder.c
        lame/fft.c
        lame/gain_analysis.c
        lame/id3tag.c
        lame/lame.c
        lame/mpglib_interface.c
        lame/newmdct.c
        lame/presets.c
        lame/psymodel.c
        lame/quantize.c
        lame/quantize_pvt.c
        lame/reservoir.c
        lame/set_get.c
        lame/tables.c
        lame/takehiro.c
        lame/util.c
        lame/vbrquantize.c
        lame/VbrTag.c
        lame/version.c
)

target_include_directories(mini_synth PRIVATE . lame)

target_link_libraries(mini_synth
        oboe::oboe
        log
)

# Testing
if (ANDROID_PLATFORM)
    # No standalone tests on Android usually, they run through JNI
else()
    # Host side tests could go here
endif()
