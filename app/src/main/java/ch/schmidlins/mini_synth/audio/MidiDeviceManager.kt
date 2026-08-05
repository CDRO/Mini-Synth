package ch.schmidlins.mini_synth.audio

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper
import android.util.Log

class MidiDeviceManager(
    private val context: Context,
    private val synthManager: SynthManager
) {
    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    private val connectedDevices = mutableMapOf<Int, MidiDevice>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val deviceCallback = object : MidiManager.OnDeviceOpenedListener {
        override fun onDeviceOpened(device: MidiDevice?) {
            if (device != null) {
                val info = device.info
                Log.d("MidiDeviceManager", "Device opened: ${info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)}")
                connectedDevices[info.id] = device
                
                // Open all input ports
                for (portInfo in info.ports) {
                    if (portInfo.type == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                        val outputPort = device.openOutputPort(portInfo.portNumber)
                        outputPort?.connect(object : MidiReceiver() {
                            override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
                                // Simplified: send only 3-byte messages for now
                                if (count >= 3) {
                                    synthManager.processMidi(data.sliceArray(offset until offset + 3), 3)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun start() {
        midiManager.registerDeviceCallback(object : MidiManager.DeviceCallback() {
            override fun onDeviceAdded(device: MidiDeviceInfo) {
                openDevice(device)
            }
            override fun onDeviceRemoved(device: MidiDeviceInfo) {
                connectedDevices.remove(device.id)?.close()
            }
        }, mainHandler)

        // Initial scan
        for (device in midiManager.devices) {
            openDevice(device)
        }
    }

    private fun openDevice(info: MidiDeviceInfo) {
        midiManager.openDevice(info, deviceCallback, mainHandler)
    }

    fun stop() {
        for (device in connectedDevices.values) {
            device.close()
        }
        connectedDevices.clear()
    }
}
