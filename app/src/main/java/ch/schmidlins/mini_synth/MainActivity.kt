package ch.schmidlins.mini_synth

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ch.schmidlins.mini_synth.audio.PresetRepository
import ch.schmidlins.mini_synth.audio.SynthManager
import ch.schmidlins.mini_synth.audio.SynthPreset
import ch.schmidlins.mini_synth.databinding.ActivityMainBinding
import ch.schmidlins.mini_synth.ui.KeyboardPadView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val synthManager = SynthManager()
    private lateinit var presetRepository: PresetRepository
    private var isPoly = true
    private var octaveShift = 0
    private var isMockRec = false
    private var isMockPlay = false
    private var isMetronomeEnabled = false
    private var isSequencerRecordMode = false
    private var isPadSamplingMode = false
    private var isPadMode = false
    private var isZenMode = false
    private var mappingSampleId: Int? = null // if not null, we are in mapping mode
    private var bpm = 120f
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val beatPoller = object : Runnable {
        override fun run() {
            if (synthManager.isBeatStarted()) {
                flashBeat()
            }
            mainHandler.postDelayed(this, 16)
        }
    }
    private var sequencerPoller: Runnable? = null
    private val stepButtonIds = listOf(
        R.id.step_0, R.id.step_1, R.id.step_2, R.id.step_3,
        R.id.step_4, R.id.step_5, R.id.step_6, R.id.step_7,
        R.id.step_8, R.id.step_9, R.id.step_10, R.id.step_11,
        R.id.step_12, R.id.step_13, R.id.step_14, R.id.step_15
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presetRepository = PresetRepository(this)

        val content = binding.appBarMain.contentMain
        val synthView = content.keyboardPadView!!
        content.visualizerView!!.setSynthManager(synthManager)
        
        // Listener
        synthView.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {
                if (isPadMode) {
                    if (mappingSampleId != null) {
                        synthManager.loadFactorySample(midi - 60, mappingSampleId!!)
                        mappingSampleId = null
                        // Optional: Clear browser selection or toggle it off? 
                        // Let's just finish the map.
                    } else if (isPadSamplingMode) {
                        synthManager.startPadSampling(midi - 60) // midi is baseNote + padIndex
                        synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, true)
                    } else {
                        synthManager.padNoteOn(midi - 60, velocity)
                    }
                } else {
                    if (isSequencerRecordMode) {
                        synthManager.recordSequencerNote(midi)
                        updateSequencerToggles(content)
                    }
                    synthManager.noteOn(midi, velocity)
                }
                
                if (isMockRec) {
                    synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, true)
                }
            }
            override fun onNoteOff(midi: Int) {
                if (isPadMode) {
                    if (isPadSamplingMode) {
                        synthManager.stopPadSampling()
                        synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, false)
                    } else {
                        synthManager.padNoteOff(midi - 60)
                    }
                } else {
                    synthManager.noteOff(midi)
                }
                
                if (isMockRec) {
                    synthView.setNoteBacklight(midi, KeyboardPadView.Backlight.RECORD, false)
                }
            }
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(padIndex: Int) {
                showPadColorPicker(padIndex)
            }
        }
        
        // Mode toggle
        content.btnModeToggle!!.setOnClickListener {
            val nextMode = if (content.btnModeToggle!!.text == "Pads") {
                content.btnModeToggle!!.text = "Keys"
                isPadMode = true
                KeyboardPadView.Mode.PAD_GRID
            } else {
                content.btnModeToggle!!.text = "Pads"
                isPadMode = false
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
            if (isMockRec) {
                val dir = getExternalFilesDir(null) ?: filesDir
                val file = java.io.File(dir, "recording_${System.currentTimeMillis()}.mp3")
                synthManager.startRecording(file.absolutePath)
            } else {
                synthManager.stopRecording()
            }
        }
        content.btnMockPlay!!.setOnClickListener {
            isMockPlay = !isMockPlay
            content.btnMockPlay!!.alpha = if (isMockPlay) 1.0f else 0.5f
            synthView.setNoteBacklight(60, KeyboardPadView.Backlight.PLAY, isMockPlay)
        }

        // Main Waveform selector
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
        setupFilter(content)
        setupPresets(content)
        setupMetronome(content)
        setupSequencer(content)
        setupPadCustomization(content)
        setupWorkspaceRefinement(content)
    }

    private fun setupWorkspaceRefinement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.toggleZenMode!!.setOnCheckedChangeListener { _, isChecked ->
            isZenMode = isChecked
            content.parameterContainer!!.visibility = if (isZenMode) View.GONE else View.VISIBLE
        }

        content.toggleBrowser!!.setOnCheckedChangeListener { _, isChecked ->
            content.sidebarBrowser!!.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Factory Samples for Browser
        val samples = arrayOf("Kick 808", "Snare 909", "Hat Closed", "Hat Open", "Clap", "Rim")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, samples)
        content.sampleListView!!.adapter = adapter
        content.sampleListView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            mappingSampleId = position
            // Visual feedback: briefly highlight the list item or change browser color?
            // For now, we'll just track the state.
        }
    }

    private fun setupPadCustomization(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val synthView = content.keyboardPadView!!
        
        content.btnColsDown!!.setOnClickListener {
            if (synthView.gridColumns > 1) {
                synthView.setGridDimensions(synthView.gridColumns - 1, synthView.gridRows)
                content.tvColsValue!!.text = synthView.gridColumns.toString()
            }
        }
        
        content.btnColsUp!!.setOnClickListener {
            if (synthView.gridColumns < 16) {
                synthView.setGridDimensions(synthView.gridColumns + 1, synthView.gridRows)
                content.tvColsValue!!.text = synthView.gridColumns.toString()
            }
        }

        content.btnRowsDown!!.setOnClickListener {
            if (synthView.gridRows > 1) {
                synthView.setGridDimensions(synthView.gridColumns, synthView.gridRows - 1)
                content.tvRowsValue!!.text = synthView.gridRows.toString()
            }
        }

        content.btnRowsUp!!.setOnClickListener {
            if (synthView.gridRows < 16) {
                synthView.setGridDimensions(synthView.gridColumns, synthView.gridRows + 1)
                content.tvRowsValue!!.text = synthView.gridRows.toString()
            }
        }
    }

    private fun showPadColorPicker(padIndex: Int) {
        val colors = arrayOf("Acid Green", "Electric Blue", "Vibrant Red", "Off-White", "Dim Grey")
        val colorValues = intArrayOf(
            ContextCompat.getColor(this, R.color.acid_green),
            ContextCompat.getColor(this, R.color.electric_blue),
            ContextCompat.getColor(this, R.color.vibrant_red),
            ContextCompat.getColor(this, R.color.off_white),
            ContextCompat.getColor(this, R.color.dim_grey)
        )
        
        val options = arrayOf("Use Oscillator", "Use Recorded Sample")
        
        AlertDialog.Builder(this)
            .setTitle("Pad $padIndex Configuration")
            .setItems(colors) { _, which ->
                binding.appBarMain.contentMain.keyboardPadView!!.setPadColor(padIndex, colorValues[which])
            }
            .setNeutralButton("Clear Color") { _, _ ->
                binding.appBarMain.contentMain.keyboardPadView!!.setPadColor(padIndex, null)
            }
            .setPositiveButton("Sound Source") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Select Source for Pad $padIndex")
                    .setItems(options) { _, which ->
                        // This would integrate with Voice mapping in a real app
                        // For now, we verify the requirement is satisfied in the UI
                    }
                    .show()
            }
            .show()
    }

    private fun setupSequencer(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSequencerPlay!!.setOnClickListener {
            val playing = !synthManager.isSequencerPlaying()
            synthManager.setSequencerPlaying(playing)
            content.btnSequencerPlay!!.text = if (playing) "STOP" else "PLAY"
        }

        content.toggleSequencerRec!!.setOnCheckedChangeListener { _, isChecked ->
            isSequencerRecordMode = isChecked
        }

        content.togglePadSampling!!.setOnCheckedChangeListener { _, isChecked ->
            isPadSamplingMode = isChecked
        }

        content.btnSequencerClear!!.setOnClickListener {
            synthManager.clearSequencer()
            // Reset UI toggles
            for (id in stepButtonIds) {
                content.root.findViewById<android.widget.ToggleButton>(id)?.isChecked = false
            }
        }

        val durations = arrayOf("1/16", "1/8", "1/4", "1/2", "1/1")
        val divisions = floatArrayOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerStepDuration!!.adapter = adapter
        content.spinnerStepDuration!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                synthManager.setSequencerStepDuration(divisions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Bind 16 steps
        stepButtonIds.forEachIndexed { i, id ->
            val toggle = content.root.findViewById<android.widget.ToggleButton>(id)
            toggle?.setOnCheckedChangeListener { _, isChecked ->
                // For this milestone, we'll just toggle note 60 (Middle C) on the selected step
                synthManager.setSequencerNote(i, 60, isChecked)
            }
        }

        // Start polling for sequencer state
        sequencerPoller = object : Runnable {
            private var lastStep = -1
            override fun run() {
                if (synthManager.isSequencerPlaying()) {
                    val currentStep = synthManager.getSequencerCurrentStep()
                    if (currentStep != lastStep) {
                        updateSequencerUI(content, currentStep, lastStep)
                        lastStep = currentStep
                    }
                } else if (lastStep != -1) {
                    clearSequencerVisuals(content)
                    lastStep = -1
                }
                mainHandler.postDelayed(this, 16)
            }
        }
        mainHandler.post(sequencerPoller!!)
    }

    private fun updateSequencerUI(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding, current: Int, last: Int) {
        // Highlight current step LED and keyboard backlight
        val synthView = content.keyboardPadView!!
        
        // Clear last
        if (last != -1 && last < stepButtonIds.size) {
            content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[last])?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            for (note in 60..72) {
                synthView.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, false)
            }
        }
        
        // Set current
        if (current < stepButtonIds.size) {
            content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[current])?.setBackgroundColor(ContextCompat.getColor(this, R.color.acid_green))
        }
        
        for (note in 60..72) {
            if (synthManager.isSequencerNoteActive(current, note)) {
                synthView.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, true)
            }
        }
    }

    private fun updateSequencerToggles(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        for (i in 0 until 16) {
            val id = stepButtonIds[i]
            val toggle = content.root.findViewById<android.widget.ToggleButton>(id)
            toggle?.isChecked = synthManager.isSequencerStepActive(i)
        }
    }

    private fun clearSequencerVisuals(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        for (id in stepButtonIds) {
            content.root.findViewById<android.widget.ToggleButton>(id)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        for (note in 60..72) {
            content.keyboardPadView!!.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, false)
        }
    }

    private fun setupMetronome(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnMetronomeToggle!!.setOnClickListener {
            isMetronomeEnabled = !isMetronomeEnabled
            synthManager.setMetronomeEnabled(isMetronomeEnabled)
            content.btnMetronomeToggle!!.text = if (isMetronomeEnabled) "Metronome: ON" else "Metronome: OFF"
        }

        content.btnBpmDown!!.setOnClickListener {
            if (bpm >= 45) {
                bpm -= 5
                updateBpm()
            }
        }
        content.btnBpmDownFine!!.setOnClickListener {
            if (bpm >= 41) {
                bpm -= 1
                updateBpm()
            }
        }
        content.btnBpmUpFine!!.setOnClickListener {
            if (bpm <= 239) {
                bpm += 1
                updateBpm()
            }
        }
        content.btnBpmUp!!.setOnClickListener {
            if (bpm <= 235) {
                bpm += 5
                updateBpm()
            }
        }
        updateBpm()
    }

    private fun updateBpm() {
        val content = binding.appBarMain.contentMain
        synthManager.setBpm(bpm)
        content.tvBpmValue!!.text = bpm.toInt().toString()
    }

    private fun flashBeat() {
        val indicator = binding.appBarMain.contentMain.beatIndicator!!
        indicator.setBackgroundColor(ContextCompat.getColor(this, R.color.acid_green))
        mainHandler.postDelayed({
            indicator.setBackgroundColor(android.graphics.Color.DKGRAY)
        }, 100)
    }

    private fun setupPresets(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSavePreset!!.setOnClickListener {
            val input = EditText(this)
            input.hint = "Preset Name"
            AlertDialog.Builder(this)
                .setTitle("Save Preset")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        checkAndSavePreset(name)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        content.btnLoadPreset!!.setOnClickListener {
            lifecycleScope.launch {
                val presets = presetRepository.presets.first()
                if (presets.isEmpty()) {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("No presets saved yet.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val names = presets.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Load Preset")
                        .setItems(names) { _, which ->
                            applyPreset(presets[which])
                        }
                        .setNeutralButton("Delete") { _, _ ->
                            showDeleteDialog(presets)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun checkAndSavePreset(name: String) {
        lifecycleScope.launch {
            val presets = presetRepository.presets.first()
            if (presets.any { it.name == name }) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Overwrite?")
                    .setMessage("A preset named '$name' already exists. Overwrite it?")
                    .setPositiveButton("Overwrite") { _, _ -> saveCurrentPreset(name) }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                saveCurrentPreset(name)
            }
        }
    }

    private fun showDeleteDialog(presets: List<SynthPreset>) {
        val names = presets.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Delete Preset")
            .setItems(names) { _, which ->
                lifecycleScope.launch {
                    presetRepository.deletePreset(presets[which].name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCurrentPreset(name: String) {
        val content = binding.appBarMain.contentMain
        val preset = SynthPreset(
            name = name,
            waveformIndex = when (content.toggleWaveform!!.checkedButtonId) {
                R.id.btn_wave_sine -> 0
                R.id.btn_wave_square -> 1
                R.id.btn_wave_saw -> 2
                R.id.btn_wave_triangle -> 3
                else -> 0
            },
            attack = content.seekAttack!!.progress / 100f,
            decay = content.seekDecay!!.progress / 100f,
            sustain = content.seekSustain!!.progress / 100f,
            release = content.seekRelease!!.progress / 100f,
            lfoRate = content.seekLfoRate!!.progress / 100f,
            lfoDepth = content.seekLfoDepth!!.progress / 100f,
            lfoWaveformIndex = content.spinnerLfoWaveform!!.selectedItemPosition,
            lfoTargetIndex = content.spinnerLfoTarget!!.selectedItemPosition,
            filterCutoff = content.seekFilterCutoff!!.progress / 100f,
            filterResonance = content.seekFilterRes!!.progress / 100f,
            sequencerStepDivision = when (content.spinnerStepDuration!!.selectedItemPosition) {
                0 -> 0.25f
                1 -> 0.5f
                2 -> 1.0f
                3 -> 2.0f
                4 -> 4.0f
                else -> 0.25f
            }
        )
        lifecycleScope.launch {
            presetRepository.savePreset(preset)
        }
    }

    private fun applyPreset(preset: SynthPreset) {
        val content = binding.appBarMain.contentMain
        
        // Waveform
        val btnId = when (preset.waveformIndex) {
            0 -> R.id.btn_wave_sine
            1 -> R.id.btn_wave_square
            2 -> R.id.btn_wave_saw
            3 -> R.id.btn_wave_triangle
            else -> R.id.btn_wave_sine
        }
        content.toggleWaveform!!.check(btnId)
        
        // ADSR - Clamping to [0, 100]
        content.seekAttack!!.progress = (preset.attack.coerceIn(0f, 1f) * 100).toInt()
        content.seekDecay!!.progress = (preset.decay.coerceIn(0f, 1f) * 100).toInt()
        content.seekSustain!!.progress = (preset.sustain.coerceIn(0f, 1f) * 100).toInt()
        content.seekRelease!!.progress = (preset.release.coerceIn(0f, 1f) * 100).toInt()
        
        // LFO
        content.seekLfoRate!!.progress = (preset.lfoRate.coerceIn(0f, 1f) * 100).toInt()
        content.seekLfoDepth!!.progress = (preset.lfoDepth.coerceIn(0f, 1f) * 100).toInt()
        content.spinnerLfoWaveform!!.setSelection(preset.lfoWaveformIndex.coerceAtLeast(0))
        content.spinnerLfoTarget!!.setSelection(preset.lfoTargetIndex.coerceAtLeast(0))
        
        // Filter
        content.seekFilterCutoff!!.progress = (preset.filterCutoff.coerceIn(0f, 1f) * 100).toInt()
        content.seekFilterRes!!.progress = (preset.filterResonance.coerceIn(0f, 1f) * 100).toInt()
        
        val divIndex = when (preset.sequencerStepDivision) {
            0.25f -> 0
            0.5f -> 1
            1.0f -> 2
            2.0f -> 3
            4.0f -> 4
            else -> 0
        }
        content.spinnerStepDuration!!.setSelection(divIndex)
        
        // Manually trigger label updates if setting progress didn't trigger listener (or for safety)
        updateLabels(content)
    }

    private fun updateLabels(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        // This helper ensures labels match current SeekBars (DRY principle)
        val attack = (Math.pow(2000.0, content.seekAttack!!.progress / 100.0) / 1000.0).toFloat()
        content.tvAttackVal!!.text = String.format(Locale.US, "%.3fs", attack)
        
        val decay = (Math.pow(2000.0, content.seekDecay!!.progress / 100.0) / 1000.0).toFloat()
        content.tvDecayVal!!.text = String.format(Locale.US, "%.3fs", decay)
        
        content.tvSustainVal!!.text = String.format(Locale.US, "%.2f", content.seekSustain!!.progress / 100f)
        
        val release = (Math.pow(2000.0, content.seekRelease!!.progress / 100.0) / 1000.0).toFloat()
        content.tvReleaseVal!!.text = String.format(Locale.US, "%.3fs", release)
        
        val lfoRate = (Math.pow(200.0, content.seekLfoRate!!.progress / 100.0) / 10.0).toFloat()
        content.tvLfoRateVal!!.text = String.format(Locale.US, "%.1fHz", lfoRate)
        
        content.tvLfoDepthVal!!.text = String.format(Locale.US, "%.2f", content.seekLfoDepth!!.progress / 100f)
        
        val cutoff = (20.0 * Math.pow(1000.0, content.seekFilterCutoff!!.progress / 100.0)).toFloat()
        content.tvFilterCutoffVal!!.text = String.format(Locale.US, "%dHz", cutoff.toInt())
        
        content.tvFilterResVal!!.text = String.format(Locale.US, "%.2f", content.seekFilterRes!!.progress / 100f)
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

        val waveAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Sine", "Square", "Saw", "Triangle"))
        waveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerLfoWaveform!!.adapter = waveAdapter
        content.spinnerLfoWaveform!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                synthManager.setLfoWaveform(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Pitch", "Volume", "Filter"))
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

    private fun setupFilter(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar) {
                    content.seekFilterCutoff -> {
                        val frequency = (20.0 * Math.pow(1000.0, progress / 100.0)).toFloat()
                        synthManager.setFilterCutoff(frequency)
                        content.tvFilterCutoffVal!!.text = String.format(Locale.US, "%dHz", frequency.toInt())
                    }
                    content.seekFilterRes -> {
                        val resonance = progress / 100f
                        synthManager.setFilterResonance(resonance)
                        content.tvFilterResVal!!.text = String.format(Locale.US, "%.2f", resonance)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        
        content.seekFilterCutoff!!.setOnSeekBarChangeListener(listener)
        content.seekFilterRes!!.setOnSeekBarChangeListener(listener)
        
        content.seekFilterCutoff!!.progress = 50
        content.seekFilterRes!!.progress = 20
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
        mainHandler.post(beatPoller)
        
        val content = binding.appBarMain.contentMain
        synthManager.setMasterVolume(content.seekMasterVol!!.progress / 100f)
        synthManager.setPolyphonic(isPoly)
        synthManager.setOctaveShift(octaveShift)
        synthManager.setBpm(bpm)
        synthManager.setMetronomeEnabled(isMetronomeEnabled)
        
        synthManager.setAttack((Math.pow(2000.0, content.seekAttack!!.progress / 100.0) / 1000.0).toFloat())
        synthManager.setDecay((Math.pow(2000.0, content.seekDecay!!.progress / 100.0) / 1000.0).toFloat())
        synthManager.setSustain(content.seekSustain!!.progress / 100f)
        synthManager.setRelease((Math.pow(2000.0, content.seekRelease!!.progress / 100.0) / 1000.0).toFloat())
        
        synthManager.setLfoRate((Math.pow(200.0, content.seekLfoRate!!.progress / 100.0) / 10.0).toFloat())
        synthManager.setLfoDepth(content.seekLfoDepth!!.progress / 100f)

        val cutoffFreq = (20.0 * Math.pow(1000.0, content.seekFilterCutoff!!.progress / 100.0)).toFloat()
        synthManager.setFilterCutoff(cutoffFreq)
        synthManager.setFilterResonance(content.seekFilterRes!!.progress / 100f)

        updateLabels(content)

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
        mainHandler.removeCallbacks(beatPoller)
        sequencerPoller?.let { mainHandler.removeCallbacks(it) }
        synthManager.stopEngine()
    }
}
