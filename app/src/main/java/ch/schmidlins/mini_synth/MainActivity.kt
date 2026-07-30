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
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val synthManager = SynthManager()
    private var isPoly = true
    private var octaveShift = 0
    private var isMockRec = false
    private var isMockPlay = false

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
                if (isMockRec) {
                    synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, true)
                }
            }
            override fun onNoteOff(midi: Int) {
                synthManager.noteOff(midi)
                if (isMockRec) {
                    synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, false)
                }
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

        // Mock Rec/Play
        content.btnMockRec!!.setOnClickListener {
            isMockRec = !isMockRec
            content.btnMockRec!!.alpha = if (isMockRec) 1.0f else 0.5f
        }
        content.btnMockPlay!!.setOnClickListener {
            isMockPlay = !isMockPlay
            content.btnMockPlay!!.alpha = if (isMockPlay) 1.0f else 0.5f
            synthView.setNoteBacklight(60, KeyboardPadView.Backlight.PLAY, isMockPlay)
        }

        // Waveform Toggle Group
        content.toggleWaveform!!.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val index = when (checkedId) {
                    R.id.btn_wave_sine -> 0
                    R.id.btn_wave_square -> 1
                    R.id.btn_wave_saw -> 2
                    R.id.btn_wave_triangle -> 3
                    else -> 0
                }
                synthManager.setWaveform(index)
            }
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
        updateOctave()

        // Master Volume
        content.seekMasterVol!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                synthManager.setMasterVolume(progress / 100f)
                content.tvMasterVolVal!!.text = String.format(Locale.US, "%d%%", progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupAdsr(content)
        setupLfo(content)
    }

    private fun setupAdsr(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeValue = (Math.pow(2000.0, progress / 100.0) / 1000.0).toFloat()
                val sustainValue = progress / 100f
                val formattedTime = String.format(Locale.US, "%.3fs", timeValue)

                when (seekBar) {
                    content.seekAttack -> {
                        synthManager.setAttack(timeValue)
                        content.tvAttackVal!!.text = formattedTime
                    }
                    content.seekDecay -> {
                        synthManager.setDecay(timeValue)
                        content.tvDecayVal!!.text = formattedTime
                    }
                    content.seekSustain -> {
                        synthManager.setSustain(sustainValue)
                        content.tvSustainVal!!.text = String.format(Locale.US, "%.2f", sustainValue)
                    }
                    content.seekRelease -> {
                        synthManager.setRelease(timeValue)
                        content.tvReleaseVal!!.text = formattedTime
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        content.seekAttack!!.setOnSeekBarChangeListener(listener)
        content.seekDecay!!.setOnSeekBarChangeListener(listener)
        content.seekSustain!!.setOnSeekBarChangeListener(listener)
        content.seekRelease!!.setOnSeekBarChangeListener(listener)
        
        content.seekAttack!!.progress = content.seekAttack!!.progress
        content.seekDecay!!.progress = content.seekDecay!!.progress
        content.seekSustain!!.progress = content.seekSustain!!.progress
        content.seekRelease!!.progress = content.seekRelease!!.progress
    }

    private fun setupLfo(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar) {
                    content.seekLfoRate -> {
                        val rate = (Math.pow(200.0, progress / 100.0) / 10.0).toFloat()
                        synthManager.setLfoRate(rate)
                        content.tvLfoRateVal!!.text = String.format(Locale.US, "%.1fHz", rate)
                    }
                    content.seekLfoDepth -> {
                        val depth = progress / 100f
                        synthManager.setLfoDepth(depth)
                        content.tvLfoDepthVal!!.text = String.format(Locale.US, "%.2f", depth)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        content.seekLfoRate!!.setOnSeekBarChangeListener(listener)
        content.seekLfoDepth!!.setOnSeekBarChangeListener(listener)

        // LFO Waveform
        val waveforms = listOf("Sine", "Square", "Saw", "Triangle")
        val waveAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, waveforms)
        waveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerLfoWaveform!!.adapter = waveAdapter
        content.spinnerLfoWaveform!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                synthManager.setLfoWaveform(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // LFO Target
        val targets = listOf("Pitch", "Volume", "Filter")
        val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targets)
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerLfoTarget!!.adapter = targetAdapter
        content.spinnerLfoTarget!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                synthManager.setLfoTarget(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        content.seekLfoRate!!.progress = 20
        content.seekLfoDepth!!.progress = 0
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
        
        // Refresh values to engine after start
        val content = binding.appBarMain.contentMain
        synthManager.setMasterVolume(content.seekMasterVol!!.progress / 100f)
        synthManager.setPolyphonic(isPoly)
        synthManager.setOctaveShift(octaveShift)
        
        // Refresh ADSR
        synthManager.setAttack((Math.pow(2000.0, content.seekAttack!!.progress / 100.0) / 1000.0).toFloat())
        synthManager.setDecay((Math.pow(2000.0, content.seekDecay!!.progress / 100.0) / 1000.0).toFloat())
        synthManager.setSustain(content.seekSustain!!.progress / 100f)
        synthManager.setRelease((Math.pow(2000.0, content.seekRelease!!.progress / 100.0) / 1000.0).toFloat())
        
        // Refresh LFO
        synthManager.setLfoRate((Math.pow(200.0, content.seekLfoRate!!.progress / 100.0) / 10.0).toFloat())
        synthManager.setLfoDepth(content.seekLfoDepth!!.progress / 100f)

        // Waveform
        val index = when (content.toggleWaveform!!.checkedButtonId) {
            R.id.btn_wave_sine -> 0
            R.id.btn_wave_square -> 1
            R.id.btn_wave_saw -> 2
            R.id.btn_wave_triangle -> 3
            else -> 0
        }
        synthManager.setWaveform(index)
    }

    override fun onStop() {
        super.onStop()
        synthManager.stopEngine()
    }
}
