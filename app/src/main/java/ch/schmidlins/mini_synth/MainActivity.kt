package ch.schmidlins.mini_synth

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import ch.schmidlins.mini_synth.audio.SynthManager
import ch.schmidlins.mini_synth.databinding.ActivityMainBinding
import ch.schmidlins.mini_synth.ui.KeyboardPadView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val synthManager = SynthManager()
    private var isPoly = true
    private var octaveShift = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content = binding.appBarMain.contentMain
        val synthView = content.keyboardPadView!!
        
        // Listener
        synthView.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {
                synthManager.noteOn(midi, velocity)
            }
            override fun onNoteOff(midi: Int) {
                synthManager.noteOff(midi)
            }
        }
        
        // Mode toggle
        content.btnModeToggle!!.setOnClickListener {
            val nextMode = if (content.btnModeToggle!!.text == "Pads") {
                content.btnModeToggle!!.text = "Keys"
                KeyboardPadView.Mode.PAD_GRID
            } else {
                content.btnModeToggle!!.text = "Pads"
                KeyboardPadView.Mode.KEYBOARD
            }
            synthView.setMode(nextMode)
        }

        // Poly toggle
        content.btnPolyToggle!!.setOnClickListener {
            isPoly = !isPoly
            synthManager.setPolyphonic(isPoly)
            content.btnPolyToggle!!.text = if (isPoly) "Poly: ON" else "Poly: OFF"
        }

        // Waveform spinner
        val waveforms = listOf("Sine", "Square", "Saw", "Triangle")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, waveforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerWaveform!!.adapter = adapter
        content.spinnerWaveform!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                synthManager.setWaveform(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Octave controls
        content.btnOctaveDown!!.setOnClickListener {
            if (octaveShift > -4) {
                octaveShift--
                updateOctave()
            }
        }
        content.btnOctaveUp!!.setOnClickListener {
            if (octaveShift < 4) {
                octaveShift++
                updateOctave()
            }
        }
        updateOctave() // Initial state

        // ADSR Sliders
        setupAdsr(content)
    }

    private fun setupAdsr(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                when (seekBar?.id) {
                    R.id.seek_attack -> synthManager.setAttack(value)
                    R.id.seek_decay -> synthManager.setDecay(value)
                    R.id.seek_sustain -> synthManager.setSustain(value)
                    R.id.seek_release -> synthManager.setRelease(value)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        content.seekAttack!!.setOnSeekBarChangeListener(listener)
        content.seekDecay!!.setOnSeekBarChangeListener(listener)
        content.seekSustain!!.setOnSeekBarChangeListener(listener)
        content.seekRelease!!.setOnSeekBarChangeListener(listener)
        
        // Initial values matching XML progress
        synthManager.setAttack(0.1f)
        synthManager.setDecay(0.1f)
        synthManager.setSustain(0.8f)
        synthManager.setRelease(0.1f)
    }

    private fun updateOctave() {
        val content = binding.appBarMain.contentMain
        synthManager.setOctaveShift(octaveShift)
        content.tvOctaveValue!!.text = octaveShift.toString()
        
        content.btnOctaveDown!!.isEnabled = octaveShift > -4
        content.btnOctaveUp!!.isEnabled = octaveShift < 4
    }

    override fun onStart() {
        super.onStart()
        synthManager.startEngine()
    }

    override fun onStop() {
        super.onStop()
        synthManager.stopEngine()
    }
}
