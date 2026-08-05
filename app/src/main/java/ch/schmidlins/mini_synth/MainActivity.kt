package ch.schmidlins.mini_synth

import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ch.schmidlins.mini_synth.audio.MidiDeviceManager
import ch.schmidlins.mini_synth.audio.PatternRepository
import ch.schmidlins.mini_synth.audio.PresetRepository
import ch.schmidlins.mini_synth.audio.SynthManager
import ch.schmidlins.mini_synth.audio.SynthPattern
import ch.schmidlins.mini_synth.audio.SynthPreset
import ch.schmidlins.mini_synth.databinding.ActivityMainBinding
import ch.schmidlins.mini_synth.ui.KeyboardPadView
import ch.schmidlins.mini_synth.ui.ProjectBrowserFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val synthManager = SynthManager()
    private lateinit var midiDeviceManager: MidiDeviceManager
    private lateinit var presetRepository: PresetRepository
    private lateinit var patternRepository: PatternRepository
    private var isPoly = true
    private var octaveShift = 0
    private var isMockRec = false
    private var isMockPlay = false
    private var isMetronomeEnabled = false
    private var isSequencerRecordMode = false
    private var isPadSamplingMode = false
    private var isPadMode = false
    private var isZenMode = false
    private var isFullscreenPads = false
    private var isKeyboardHidden = false
    private var isHelpMode = false
    private var isDemoPlaying = false
    private var demoJob: kotlinx.coroutines.Job? = null
    var isPollingEnabled = true // Exposed for tests
    private var mappingSampleId: Int? = null // if not null, we are in mapping mode
    private val padSamplePaths = mutableMapOf<Int, String>()
    
    companion object {
        fun getSampleFileName(padIndex: Int) = "pad_$padIndex.bin"
    }

    private var bpm = 120f
    private var bankIndex = 0
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val beatPoller = object : Runnable {
        override fun run() {
            if (synthManager.isBeatStarted()) {
                flashBeat()
            }
            if (isPollingEnabled) {
                mainHandler.postDelayed(this, 16)
            }
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
        patternRepository = PatternRepository(this)
        midiDeviceManager = MidiDeviceManager(this, synthManager).apply {
            onStatusChanged = { connected ->
                runOnUiThread {
                    binding.appBarMain.contentMain.midiStatusIndicator.setBackgroundColor(
                        if (connected) ContextCompat.getColor(this@MainActivity, R.color.acid_green)
                        else android.graphics.Color.DKGRAY
                    )
                }
            }
        }

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
                        content.sidebarBrowser.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.surface_dark))
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
                        val padIndex = midi - 60
                        val sampleFile = File(filesDir, getSampleFileName(padIndex))
                        val samplePath = sampleFile.absolutePath
                        synthManager.savePadSample(padIndex, samplePath)
                        padSamplePaths[padIndex] = samplePath
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
                if (mappingSampleId == null) {
                    showPadColorPicker(padIndex)
                }
            }
            override fun onGesture(pitchBend: Float, modulation: Float) {
                synthManager.setPitchBend(pitchBend)
                synthManager.setModulation(modulation)
            }
        }
        
        // Mode toggle
        content.btnModeToggle.setOnClickListener {
            if (isHelpMode) {
                showHelp("Switch between a 13-key keyboard and a grid of customizable pads.")
                return@setOnClickListener
            }
            if (isPadMode) {
                content.btnModeToggle.text = "Pads"
                isPadMode = false
                isFullscreenPads = false
                content.togglePadsFullscreen.isChecked = false
                mappingSampleId = null
            } else {
                content.btnModeToggle.text = "Keys"
                isPadMode = true
            }
            synthView.setMode(if (isPadMode) KeyboardPadView.Mode.PAD_GRID else KeyboardPadView.Mode.KEYBOARD)
            updateWorkspaceVisibility(content)
        }

        // Poly toggle
        content.btnPolyToggle!!.setOnClickListener {
            if (isHelpMode) {
                showHelp("Toggle Polyphony. ON allows playing multiple notes. OFF limits to one note (Monophonic).")
                return@setOnClickListener
            }
            isPoly = !isPoly
            synthManager.setPolyphonic(isPoly)
            content.btnPolyToggle!!.text = if (isPoly) "Poly: ON" else "Poly: OFF"
        }

        // Mock Rec/Play
        content.btnMockRec!!.setOnClickListener {
            if (isHelpMode) {
                showHelp("Record your session directly to an MP3 file on your device.")
                return@setOnClickListener
            }
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
            if (isHelpMode) {
                showHelp("Toggle test playback state (Visual only in mock mode).")
                return@setOnClickListener
            }
            isMockPlay = !isMockPlay
            content.btnMockPlay!!.alpha = if (isMockPlay) 1.0f else 0.5f
            synthView.setNoteBacklight(60, KeyboardPadView.Backlight.PLAY, isMockPlay)
        }

        // Main Waveform selector
        content.toggleWaveform!!.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isHelpMode && isChecked) {
                showHelp("Select the base oscillator waveform: Sine, Square, Saw, or Triangle.")
                return@addOnButtonCheckedListener
            }
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
            if (isHelpMode) { showHelp("Shift the keyboard range down by one octave."); return@setOnClickListener }
            if (octaveShift > -4) {
                octaveShift--
                updateOctave()
            }
        }
        content.btnOctaveUp!!.setOnClickListener {
            if (isHelpMode) { showHelp("Shift the keyboard range up by one octave."); return@setOnClickListener }
            if (octaveShift < 4) {
                octaveShift++
                updateOctave()
            }
        }
        updateOctave()

        // Master Volume
        content.seekMasterVol!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isHelpMode && fromUser) { showHelp("Adjust the overall output volume of the synthesizer."); return }
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
        setupBankManagement(content)
        setupEffects(content)
        setupWorkspaceRefinement(content)
        setupPatternManagement(content)
        
        content.btnProjects.setOnClickListener {
            if (isHelpMode) {
                showHelp("Project Browser: Manage your music projects (Save, Load, Create).")
                return@setOnClickListener
            }
            ProjectBrowserFragment(synthManager) {
                refreshUiFromEngine()
            }.show(supportFragmentManager, "projects")
        }
    }

    private fun updateOctave() {
        val content = binding.appBarMain.contentMain
        synthManager.setOctaveShift(octaveShift)
        content.tvOctaveValue!!.text = octaveShift.toString()
        content.btnOctaveDown!!.isEnabled = octaveShift > -4
        content.btnOctaveUp!!.isEnabled = octaveShift < 4
    }

    private fun showHelp(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Discovery Mode")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateWorkspaceVisibility(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val root = content.root as androidx.constraintlayout.widget.ConstraintLayout
        if (isPollingEnabled) {
            TransitionManager.beginDelayedTransition(root)
        }
        val set = ConstraintSet()
        set.clone(root)

        content.keyboardPadView.isEnabled = !isHelpMode

        val keyboardId = content.keyboardPadView.id
        val toggleKbId = content.toggleKeyboard.id
        val headerId = content.topHeader.id
        val workspaceId = content.workspaceLayout.id
        val fullToggleId = content.togglePadsFullscreen.id

        // Direct children visibility via ConstraintSet
        if (isKeyboardHidden || isHelpMode) {
            set.setVisibility(keyboardId, View.GONE)
            set.clear(toggleKbId, ConstraintSet.BOTTOM)
            set.connect(toggleKbId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        } else {
            set.setVisibility(keyboardId, View.VISIBLE)
            set.clear(toggleKbId, ConstraintSet.BOTTOM)
            set.connect(toggleKbId, ConstraintSet.BOTTOM, keyboardId, ConstraintSet.TOP)
        }

        if (isPadMode) {
            set.setVisibility(fullToggleId, View.VISIBLE)
            
            if (isFullscreenPads) {
                set.setVisibility(headerId, View.GONE)
                set.setVisibility(workspaceId, View.GONE)
                set.connect(keyboardId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                set.constrainPercentHeight(keyboardId, 1.0f)
                set.setVisibility(toggleKbId, View.GONE)
            } else {
                set.setVisibility(toggleKbId, View.VISIBLE)
                set.setVisibility(headerId, View.VISIBLE)
                set.setVisibility(workspaceId, View.VISIBLE)
                
                // Nested views managed directly
                content.parameterContainer.visibility = View.GONE
                content.sequencerSection.visibility = View.GONE
                content.padCustomizationSection.visibility = View.GONE
                content.btnPolyToggle.visibility = View.GONE
                content.btnOctaveDown.visibility = View.GONE
                content.btnOctaveUp.visibility = View.GONE
                content.tvOctaveValue.visibility = View.GONE
                content.toggleZenMode.visibility = View.GONE
                
                set.connect(keyboardId, ConstraintSet.TOP, workspaceId, ConstraintSet.BOTTOM)
                if (!isKeyboardHidden) set.constrainPercentHeight(keyboardId, 0.3f)
            }
        } else {
            set.setVisibility(toggleKbId, if (isHelpMode) View.GONE else View.VISIBLE)
            set.setVisibility(fullToggleId, View.GONE)
            set.setVisibility(headerId, View.VISIBLE)
            set.setVisibility(workspaceId, View.VISIBLE)
            
            // Nested views managed directly
            content.parameterContainer.visibility = if (isZenMode) View.GONE else View.VISIBLE
            content.sequencerSection.visibility = View.VISIBLE
            content.padCustomizationSection.visibility = View.VISIBLE
            content.btnPolyToggle.visibility = View.VISIBLE
            content.btnOctaveDown.visibility = View.VISIBLE
            content.btnOctaveUp.visibility = View.VISIBLE
            content.tvOctaveValue.visibility = View.VISIBLE
            content.toggleZenMode.visibility = View.VISIBLE
            
            set.connect(keyboardId, ConstraintSet.TOP, workspaceId, ConstraintSet.BOTTOM)
            if (!isKeyboardHidden) set.constrainPercentHeight(keyboardId, 0.3f)
        }
        set.applyTo(root)
    }

    private fun setupWorkspaceRefinement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnHelpMode.setOnClickListener {
            isHelpMode = !isHelpMode
            content.btnHelpMode.alpha = if (isHelpMode) 1.0f else 0.5f
            updateWorkspaceVisibility(content)
            if (isHelpMode) Toast.makeText(this, "Discovery Mode Active. Click any control for info.", Toast.LENGTH_LONG).show()
        }

        content.btnDemoMode.setOnClickListener {
            if (isDemoPlaying) {
                isDemoPlaying = false
                demoJob?.cancel()
                content.btnDemoMode.text = "DEMO"
            } else {
                isDemoPlaying = true
                content.btnDemoMode.text = "STOP"
                playDemoSong()
            }
        }

        content.toggleZenMode.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) { showHelp("Zen Mode hides the complex parameter controls to focus on the performance area."); return@setOnCheckedChangeListener }
            isZenMode = isChecked
            content.parameterContainer.visibility = if (isZenMode) View.GONE else View.VISIBLE
        }

        content.toggleBrowser.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) { showHelp("Toggle the Sample Browser to load factory or recorded sounds onto pads."); return@setOnCheckedChangeListener }
            content.sidebarBrowser.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        content.togglePadsFullscreen.setOnCheckedChangeListener { _, isChecked ->
            isFullscreenPads = isChecked
            content.keyboardPadView.clearHeldNotes() // Fixes #41
            updateWorkspaceVisibility(content)
        }

        content.toggleKeyboard.setOnCheckedChangeListener { _, isChecked ->
            isKeyboardHidden = isChecked
            updateWorkspaceVisibility(content)
        }

        val samples = arrayOf("Kick 808", "Snare 909", "Hat Closed", "Hat Open", "Clap", "Rim")
        for (i in samples.indices) {
            val tv = android.widget.TextView(this).apply {
                text = samples[i]
                setPadding(16, 16, 16, 16)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.off_white))
                textSize = 10f
                setOnClickListener {
                    if (isHelpMode) { showHelp("Sample: ${samples[i]}. Click to enter mapping mode, then touch a pad to assign."); return@setOnClickListener }
                    mappingSampleId = i
                    content.sidebarBrowser.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.border_dim))
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.acid_green))
                    postDelayed({ setBackgroundColor(android.graphics.Color.TRANSPARENT) }, 200)
                }
            }
            content.sampleContainer.addView(tv)
        }
    }

    private fun playDemoSong() {
        demoJob?.cancel()
        demoJob = lifecycleScope.launch {
            // Reset for demo - Fixes #42
            synthManager.setWaveform(2) // Saw
            synthManager.setAttack(0.01f)
            synthManager.setDecay(0.1f)
            synthManager.setSustain(0.7f)
            synthManager.setRelease(0.3f)
            synthManager.setFilterCutoff(2000f)
            synthManager.setFilterResonance(0.2f)
            
            val notes = listOf(60, 63, 67, 72, 67, 63)
            var i = 0
            while (isDemoPlaying) {
                val note = notes[i % notes.size]
                synthManager.noteOn(note, 0.8f)
                delay(300)
                synthManager.noteOff(note)
                delay(100)
                i++
                if (i == notes.size) {
                    synthManager.setFilterCutoff(if (i % 12 == 0) 2000f else 500f)
                }
            }
        }
    }

    private fun setupPadCustomization(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val synthView = content.keyboardPadView!!
        content.btnColsDown!!.setOnClickListener {
            if (isHelpMode) { showHelp("Decrease the number of columns in the pad grid."); return@setOnClickListener }
            if (synthView.gridColumns > 1) {
                synthView.setGridDimensions(synthView.gridColumns - 1, synthView.gridRows)
                content.tvColsValue!!.text = synthView.gridColumns.toString()
            }
        }
        content.btnColsUp!!.setOnClickListener {
            if (isHelpMode) { showHelp("Increase the number of columns in the pad grid."); return@setOnClickListener }
            if (synthView.gridColumns < 16) {
                synthView.setGridDimensions(synthView.gridColumns + 1, synthView.gridRows)
                content.tvColsValue!!.text = synthView.gridColumns.toString()
            }
        }
        content.btnRowsDown!!.setOnClickListener {
            if (isHelpMode) { showHelp("Decrease the number of rows in the pad grid."); return@setOnClickListener }
            if (synthView.gridRows > 1) {
                synthView.setGridDimensions(synthView.gridColumns, synthView.gridRows - 1)
                content.tvRowsValue!!.text = synthView.gridRows.toString()
            }
        }
        content.btnRowsUp!!.setOnClickListener {
            if (isHelpMode) { showHelp("Increase the number of rows in the pad grid."); return@setOnClickListener }
            if (synthView.gridRows < 16) {
                synthView.setGridDimensions(synthView.gridColumns, synthView.gridRows + 1)
                content.tvRowsValue!!.text = synthView.gridRows.toString()
            }
        }
    }

    private fun setupBankManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val synthView = content.keyboardPadView!!
        content.btnBankDown!!.setOnClickListener {
            if (isHelpMode) { showHelp("Switch to the previous bank of pads."); return@setOnClickListener }
            if (bankIndex > 0) {
                bankIndex--
                updateBank(content)
            }
        }
        content.btnBankUp!!.setOnClickListener {
            if (isHelpMode) { showHelp("Switch to the next bank of pads."); return@setOnClickListener }
            val maxBank = 255 / (synthView.gridColumns * synthView.gridRows)
            if (bankIndex < maxBank) {
                bankIndex++
                updateBank(content)
            }
        }
    }

    private fun updateBank(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val synthView = content.keyboardPadView!!
        val offset = bankIndex * (synthView.gridColumns * synthView.gridRows)
        synthView.setPadOffset(offset)
        content.tvBankValue!!.text = (bankIndex + 1).toString()
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
                AlertDialog.Builder(this).setTitle("Select Source").setItems(options) { _, _ -> }.show()
            }
            .show()
    }

    private fun setupSequencer(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSequencerPlay!!.setOnClickListener {
            if (isHelpMode) { showHelp("Start or stop the 16-step MIDI sequencer."); return@setOnClickListener }
            val playing = !synthManager.isSequencerPlaying()
            synthManager.setSequencerPlaying(playing)
            content.btnSequencerPlay!!.text = if (playing) "STOP" else "PLAY"
        }
        content.toggleSequencerRec!!.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) { showHelp("Enable Step Recording. Play notes on the keyboard to map them to the next sequencer step."); return@setOnCheckedChangeListener }
            isSequencerRecordMode = isChecked
        }
        content.togglePadSampling!!.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) { showHelp("Enable Pad Sampling. While active, touching a pad will record the current engine output into that pad."); return@setOnCheckedChangeListener }
            isPadSamplingMode = isChecked
        }
        content.btnSequencerClear!!.setOnClickListener {
            if (isHelpMode) { showHelp("Clear all notes from the sequencer."); return@setOnClickListener }
            synthManager.clearSequencer()
            for (id in stepButtonIds) content.root.findViewById<android.widget.ToggleButton>(id)?.isChecked = false
        }

        content.btnSequencerExport!!.setOnClickListener {
            if (isHelpMode) { showHelp("Export the current pattern to a high-quality file and share it."); return@setOnClickListener }
            
            val dir = getExternalFilesDir(null) ?: filesDir
            val file = java.io.File(dir, "pattern_export_${System.currentTimeMillis()}.wav")
            
            lifecycleScope.launch {
                if (isFinishing || isDestroyed) return@launch
                val progress = AlertDialog.Builder(this@MainActivity)
                    .setMessage("Exporting...")
                    .setCancelable(false)
                    .show()
                
                try {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        synthManager.renderPatternToFile(file.absolutePath)
                    }
                } finally {
                    if (!isFinishing && !isDestroyed) progress.dismiss()
                }
                
                if (!isFinishing && !isDestroyed) shareFile(file)
            }
        }
        val durations = arrayOf("1/16", "1/8", "1/4", "1/2", "1/1")
        val divisions = floatArrayOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerStepDuration!!.adapter = adapter
        content.spinnerStepDuration!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isHelpMode) return
                synthManager.setSequencerStepDuration(divisions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        stepButtonIds.forEachIndexed { i, id ->
            val toggle = content.root.findViewById<android.widget.ToggleButton>(id)
            toggle?.setOnCheckedChangeListener { _, isChecked ->
                if (isHelpMode) { showHelp("Toggle note at step ${i+1}."); return@setOnCheckedChangeListener }
                synthManager.setSequencerNote(i, 60, isChecked)
            }
        }
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
                if (isPollingEnabled) {
                    mainHandler.postDelayed(this, 16)
                }
            }
        }
        mainHandler.post(sequencerPoller!!)
    }

    private fun shareFile(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(intent, "Share Exported Pattern"))
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSequencerUI(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding, current: Int, last: Int) {
        val synthView = content.keyboardPadView!!
        if (last != -1 && last < stepButtonIds.size) {
            content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[last])?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            for (note in 60..72) synthView.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, false)
        }
        if (current < stepButtonIds.size) {
            val toggle = content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[current])
            val activeNotes = synthManager.getSequencerActiveNotes(current)
            val isMulti = (activeNotes?.size ?: 0) > 1
            toggle?.setBackgroundColor(ContextCompat.getColor(this, if (isMulti) R.color.electric_blue else R.color.acid_green))
        }
        for (note in 60..72) if (synthManager.isSequencerNoteActive(current, note)) synthView.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, true)
    }

    private fun updateSequencerToggles(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        for (i in 0 until 16) {
            val id = stepButtonIds[i]
            val toggle = content.root.findViewById<android.widget.ToggleButton>(id)
            val activeNotes = synthManager.getSequencerActiveNotes(i)
            toggle?.isChecked = activeNotes != null && activeNotes.isNotEmpty()
            if (activeNotes != null && activeNotes.size > 1) {
                toggle?.setBackgroundColor(ContextCompat.getColor(this, R.color.electric_blue))
            } else {
                toggle?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }

    private fun clearSequencerVisuals(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        for (id in stepButtonIds) content.root.findViewById<android.widget.ToggleButton>(id)?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        for (note in 60..72) content.keyboardPadView!!.setNoteBacklight(note, KeyboardPadView.Backlight.PLAY, false)
    }

    private fun setupEffects(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isHelpMode && fromUser) {
                    val desc = when (seekBar) {
                        content.seekDelayTime -> "Delay Time: Duration of the echo effect (up to 2 seconds)."
                        content.seekDelayFeedback -> "Delay Feedback: Number of repeats."
                        content.seekDelayMix -> "Delay Mix: Blend between dry and echo sound."
                        content.seekReverbSize -> "Reverb Size: Simulated room size."
                        content.seekReverbDamping -> "Reverb Damping: High frequency attenuation in space."
                        content.seekReverbMix -> "Reverb Mix: Blend between dry and spatial sound."
                        else -> ""
                    }
                    showHelp(desc)
                    return
                }
                val value = progress / 100f
                when (seekBar) {
                    content.seekDelayTime -> synthManager.setDelayTime(value * 2.0f)
                    content.seekDelayFeedback -> synthManager.setDelayFeedback(value * 0.95f)
                    content.seekDelayMix -> synthManager.setDelayMix(value)
                    content.seekReverbSize -> synthManager.setReverbSize(value)
                    content.seekReverbDamping -> synthManager.setReverbDamping(value)
                    content.seekReverbMix -> synthManager.setReverbMix(value)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        content.seekDelayTime!!.setOnSeekBarChangeListener(listener)
        content.seekDelayFeedback!!.setOnSeekBarChangeListener(listener)
        content.seekDelayMix!!.setOnSeekBarChangeListener(listener)
        content.seekReverbSize!!.setOnSeekBarChangeListener(listener)
        content.seekReverbDamping!!.setOnSeekBarChangeListener(listener)
        content.seekReverbMix!!.setOnSeekBarChangeListener(listener)
    }

    private fun setupMetronome(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnMetronomeToggle!!.setOnClickListener {
            if (isHelpMode) { showHelp("Toggle the Metronome. Heard as a click synced to the BPM."); return@setOnClickListener }
            isMetronomeEnabled = !isMetronomeEnabled
            synthManager.setMetronomeEnabled(isMetronomeEnabled)
            content.btnMetronomeToggle!!.text = if (isMetronomeEnabled) "MET: ON" else "MET: OFF"
        }
        content.btnBpmDown!!.setOnClickListener { if (bpm >= 45) { bpm -= 5; updateBpm() } }
        content.btnBpmDownFine!!.setOnClickListener { if (bpm >= 41) { bpm -= 1; updateBpm() } }
        content.btnBpmUpFine!!.setOnClickListener { if (bpm <= 239) { bpm += 1; updateBpm() } }
        content.btnBpmUp!!.setOnClickListener { if (bpm <= 235) { bpm += 5; updateBpm() } }
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
        mainHandler.postDelayed({ indicator.setBackgroundColor(android.graphics.Color.DKGRAY) }, 100)
    }

    private fun setupPresets(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSavePreset!!.setOnClickListener {
            if (isHelpMode) { showHelp("Save the current synthesizer parameters (OSC, ADSR, LFO, Filter) as a named preset."); return@setOnClickListener }
            val input = EditText(this)
            input.hint = "Preset Name"
            AlertDialog.Builder(this).setTitle("Save Preset").setView(input).setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) checkAndSavePreset(name)
            }.setNegativeButton("Cancel", null).show()
        }
        content.btnLoadPreset!!.setOnClickListener {
            if (isHelpMode) { showHelp("Load a previously saved synthesizer preset."); return@setOnClickListener }
            lifecycleScope.launch {
                val presets = presetRepository.presets.first()
                if (presets.isEmpty()) AlertDialog.Builder(this@MainActivity).setMessage("No presets saved yet.").setPositiveButton("OK", null).show()
                else {
                    val names = presets.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this@MainActivity).setTitle("Load Preset").setItems(names) { _, which -> applyPreset(presets[which]) }
                        .setNeutralButton("Delete") { _, _ -> showDeleteDialog(presets) }
                        .setNegativeButton("Cancel", null).show()
                }
            }
        }
    }

    private fun checkAndSavePreset(name: String) {
        lifecycleScope.launch {
            val presets = presetRepository.presets.first()
            if (presets.any { it.name == name }) {
                AlertDialog.Builder(this@MainActivity).setTitle("Overwrite?").setMessage("A preset named '$name' already exists. Overwrite it?")
                    .setPositiveButton("Overwrite") { _, _ -> saveCurrentPreset(name) }.setNegativeButton("Cancel", null).show()
            } else saveCurrentPreset(name)
        }
    }

    private fun showDeleteDialog(presets: List<SynthPreset>) {
        val names = presets.map { it.name }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Delete Preset").setItems(names) { _, which ->
            lifecycleScope.launch { presetRepository.deletePreset(presets[which].name) }
        }.setNegativeButton("Cancel", null).show()
    }

    private fun saveCurrentPreset(name: String) {
        val content = binding.appBarMain.contentMain
        val preset = SynthPreset(
            name = name,
            waveformIndex = when (content.toggleWaveform!!.checkedButtonId) {
                R.id.btn_wave_sine -> 0; R.id.btn_wave_square -> 1; R.id.btn_wave_saw -> 2; R.id.btn_wave_triangle -> 3; else -> 0
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
                0 -> 0.25f; 1 -> 0.5f; 2 -> 1.0f; 3 -> 2.0f; 4 -> 4.0f; else -> 0.25f
            },
            padSamplePaths = padSamplePaths.toMap()
        )
        lifecycleScope.launch { presetRepository.savePreset(preset) }
    }

    private fun applyPreset(preset: SynthPreset) {
        val content = binding.appBarMain.contentMain
        val btnId = when (preset.waveformIndex) {
            0 -> R.id.btn_wave_sine; 1 -> R.id.btn_wave_square; 2 -> R.id.btn_wave_saw; 3 -> R.id.btn_wave_triangle; else -> R.id.btn_wave_sine
        }
        content.toggleWaveform!!.check(btnId)
        content.seekAttack!!.progress = (preset.attack.coerceIn(0f, 1f) * 100).toInt()
        content.seekDecay!!.progress = (preset.decay.coerceIn(0f, 1f) * 100).toInt()
        content.seekSustain!!.progress = (preset.sustain.coerceIn(0f, 1f) * 100).toInt()
        content.seekRelease!!.progress = (preset.release.coerceIn(0f, 1f) * 100).toInt()
        content.seekLfoRate!!.progress = (preset.lfoRate.coerceIn(0f, 1f) * 100).toInt()
        content.seekLfoDepth!!.progress = (preset.lfoDepth.coerceIn(0f, 1f) * 100).toInt()
        content.spinnerLfoWaveform!!.setSelection(preset.lfoWaveformIndex.coerceAtLeast(0))
        content.spinnerLfoTarget!!.setSelection(preset.lfoTargetIndex.coerceAtLeast(0))
        content.seekFilterCutoff!!.progress = (preset.filterCutoff.coerceIn(0f, 1f) * 100).toInt()
        content.seekFilterRes!!.progress = (preset.filterResonance.coerceIn(0f, 1f) * 100).toInt()
        val divIndex = when (preset.sequencerStepDivision) {
            0.25f -> 0; 0.5f -> 1; 1.0f -> 2; 2.0f -> 3; 4.0f -> 4; else -> 0
        }
        content.spinnerStepDuration!!.setSelection(divIndex)
        padSamplePaths.clear()
        preset.padSamplePaths.forEach { (idx, path) -> if (File(path).exists()) { synthManager.loadPadSample(idx, path); padSamplePaths[idx] = path } }
        updateLabels(content)
    }

    private fun setupPatternManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSavePattern!!.setOnClickListener {
            if (isHelpMode) { showHelp("Save the current 16-step pattern."); return@setOnClickListener }
            val input = EditText(this)
            input.hint = "Pattern Name"
            AlertDialog.Builder(this).setTitle("Save Pattern").setView(input).setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) saveCurrentPattern(name)
            }.setNegativeButton("Cancel", null).show()
        }

        content.btnLoadPattern!!.setOnClickListener {
            if (isHelpMode) { showHelp("Load a saved sequence pattern."); return@setOnClickListener }
            lifecycleScope.launch {
                val patterns = patternRepository.patterns.first()
                if (patterns.isEmpty()) Toast.makeText(this@MainActivity, "No patterns saved.", Toast.LENGTH_SHORT).show()
                else {
                    val names = patterns.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this@MainActivity).setTitle("Load Pattern").setItems(names) { _, which ->
                        applyPattern(patterns[which])
                    }
                    .setNeutralButton("Delete") { _, _ ->
                        showDeletePatternDialog(patterns)
                    }
                    .show()
                }
            }
        }
    }

    private fun showDeletePatternDialog(patterns: List<SynthPattern>) {
        val names = patterns.map { it.name }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Delete Pattern").setItems(names) { _, which ->
            lifecycleScope.launch { patternRepository.deletePattern(patterns[which].name) }
        }.setNegativeButton("Cancel", null).show()
    }

    private fun saveCurrentPattern(name: String) {
        val grid = mutableListOf<List<Int>>()
        for (step in 0 until 16) {
            val notes = synthManager.getSequencerActiveNotes(step)?.toList() ?: emptyList()
            grid.add(notes)
        }
        val division = when (binding.appBarMain.contentMain.spinnerStepDuration!!.selectedItemPosition) {
            0 -> 0.25f; 1 -> 0.5f; 2 -> 1.0f; 3 -> 2.0f; 4 -> 4.0f; else -> 0.25f
        }
        val pattern = SynthPattern(name, grid, division)
        lifecycleScope.launch { patternRepository.savePattern(pattern) }
    }

    private fun applyPattern(pattern: SynthPattern) {
        val content = binding.appBarMain.contentMain
        synthManager.clearSequencer()
        pattern.grid.forEachIndexed { step, notes ->
            notes.forEach { note ->
                synthManager.setSequencerNote(step, note, true)
            }
        }
        val divIndex = when (pattern.stepDivision) {
            0.25f -> 0; 0.5f -> 1; 1.0f -> 2; 2.0f -> 3; 4.0f -> 4; else -> 0
        }
        content.spinnerStepDuration!!.setSelection(divIndex)
        updateSequencerToggles(content)
    }

    private fun updateLabels(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
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
                if (isHelpMode && fromUser) {
                    val desc = when(seekBar) {
                        content.seekAttack -> "Attack: Time taken for the sound to reach peak volume after trigger."
                        content.seekDecay -> "Decay: Time taken to drop from peak to sustain level."
                        content.seekSustain -> "Sustain: The constant volume level while a key is held."
                        content.seekRelease -> "Release: Time taken for the sound to fade to silence after key release."
                        else -> ""
                    }
                    showHelp(desc)
                    return
                }
                val timeValue = (Math.pow(2000.0, progress / 100.0) / 1000.0).toFloat()
                val sustainValue = progress / 100f
                val formattedTime = String.format(Locale.US, "%.3fs", timeValue)
                when (seekBar) {
                    content.seekAttack -> { synthManager.setAttack(timeValue); content.tvAttackVal!!.text = formattedTime }
                    content.seekDecay -> { synthManager.setDecay(timeValue); content.tvDecayVal!!.text = formattedTime }
                    content.seekSustain -> { synthManager.setSustain(sustainValue); content.tvSustainVal!!.text = String.format(Locale.US, "%.2f", sustainValue) }
                    content.seekRelease -> { synthManager.setRelease(timeValue); content.tvReleaseVal!!.text = formattedTime }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        content.seekAttack!!.setOnSeekBarChangeListener(listener)
        content.seekDecay!!.setOnSeekBarChangeListener(listener)
        content.seekSustain!!.setOnSeekBarChangeListener(listener)
        content.seekRelease!!.setOnSeekBarChangeListener(listener)
    }

    private fun setupLfo(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isHelpMode && fromUser) {
                    showHelp(if (seekBar == content.seekLfoRate) "LFO Rate: Speed of the oscillation (0.1Hz to 20Hz)." else "LFO Depth: Intensity of the modulation effect.")
                    return
                }
                when (seekBar) {
                    content.seekLfoRate -> {
                        val rate = (Math.pow(200.0, progress / 100.0) / 10.0).toFloat()
                        synthManager.setLfoRate(rate); content.tvLfoRateVal!!.text = String.format(Locale.US, "%.1fHz", rate)
                    }
                    content.seekLfoDepth -> {
                        val depth = progress / 100f
                        synthManager.setLfoDepth(depth); content.tvLfoDepthVal!!.text = String.format(Locale.US, "%.2f", depth)
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
                if (isHelpMode) return
                synthManager.setLfoWaveform(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Pitch", "Volume", "Filter"))
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerLfoTarget!!.adapter = targetAdapter
        content.spinnerLfoTarget!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isHelpMode) return
                synthManager.setLfoTarget(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFilter(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isHelpMode && fromUser) {
                    showHelp(if (seekBar == content.seekFilterCutoff) "Cutoff: Frequency above which sounds are attenuated." else "Resonance: Boosts frequencies around the cutoff point for a sharper sound.")
                    return
                }
                when (seekBar) {
                    content.seekFilterCutoff -> {
                        val frequency = (20.0 * Math.pow(1000.0, progress / 100.0)).toFloat()
                        synthManager.setFilterCutoff(frequency); content.tvFilterCutoffVal!!.text = String.format(Locale.US, "%dHz", frequency.toInt())
                    }
                    content.seekFilterRes -> {
                        val resonance = progress / 100f
                        synthManager.setFilterResonance(resonance); content.tvFilterResVal!!.text = String.format(Locale.US, "%.2f", resonance)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        content.seekFilterCutoff!!.setOnSeekBarChangeListener(listener)
        content.seekFilterRes!!.setOnSeekBarChangeListener(listener)
    }

    private fun refreshUiFromEngine() {
        // TODO: Implement full UI refresh from engine state
        Toast.makeText(this, "Project Loaded", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        synthManager.startEngine()
        midiDeviceManager.start()
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
            R.id.btn_wave_sine -> 0; R.id.btn_wave_square -> 1; R.id.btn_wave_saw -> 2; R.id.btn_wave_triangle -> 3; else -> 0
        }
        synthManager.setWaveform(index)
        updateWorkspaceVisibility(content)
    }

    override fun onStop() {
        super.onStop()
        mainHandler.removeCallbacks(beatPoller)
        sequencerPoller?.let { mainHandler.removeCallbacks(it) }
        binding.appBarMain.contentMain.keyboardPadView!!.clearHeldNotes() // Fixes #40
        midiDeviceManager.stop()
        synthManager.stopEngine()
    }
}
