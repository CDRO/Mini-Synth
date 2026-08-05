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
    var onStatusChanged: ((Boolean) -> Unit)? = null

    private val deviceCallback = object : MidiManager.OnDeviceOpenedListener {
        override fun onDeviceOpened(device: MidiDevice?) {
            if (device != null) {
                val info = device.info
                Log.d("MidiDeviceManager", "Device opened: ${info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)}")
                connectedDevices[info.id] = device
                onStatusChanged?.invoke(connectedDevices.isNotEmpty())
                
                // Open all input ports
                for (portInfo in info.ports) {
                    if (portInfo.type == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                        val outputPort = device.openOutputPort(portInfo.portNumber)
                        outputPort?.connect(object : MidiReceiver() {
                            private var runningStatus: Byte = 0

                            override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
                                var i = offset
                                while (i < offset + count) {
                                    val b = data[i]
                                    if (b.toInt() and 0x80 != 0) {
                                        // Status byte
                                        runningStatus = b
                                        val status = b.toInt() and 0xF0
                                        if (status == 0x90 || status == 0x80 || status == 0xB0) {
                                            if (i + 2 < offset + count) {
                                                synthManager.processMidi(data.sliceArray(i until i + 3), 3)
                                                i += 3
                                                continue
                                            }
                                        }
                                    } else if (runningStatus != 0.toByte()) {
                                        // Data byte with running status
                                        val status = runningStatus.toInt() and 0xF0
                                        if (status == 0x90 || status == 0x80 || status == 0xB0) {
                                            if (i + 1 < offset + count) {
                                                val msg = byteArrayOf(runningStatus, data[i], data[i+1])
                                                synthManager.processMidi(msg, 3)
                                                i += 2
                                                continue
                                            }
                                        }
                                    }
                                    i++
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
                onStatusChanged?.invoke(connectedDevices.isNotEmpty())
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
