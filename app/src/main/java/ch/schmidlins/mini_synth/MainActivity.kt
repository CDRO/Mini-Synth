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

data class TrackState(
    var waveformIndex: Int = 0,
    var attackProgress: Int = 10,
    var decayProgress: Int = 10,
    var sustainProgress: Int = 80,
    var releaseProgress: Int = 10,
    var lfoRateProgress: Int = 20,
    var lfoDepthProgress: Int = 0,
    var lfoWaveformIndex: Int = 0,
    var lfoTargetIndex: Int = 0,
    var filterCutoffProgress: Int = 50,
    var filterResProgress: Int = 20,
    var morphProgress: Int = 0,
    var pdProgress: Int = 0,
    var panningProgress: Int = 50,
    var volumeProgress: Int = 80,
    var unisonCount: Int = 1,
    var unisonDetuneProgress: Int = 0,
    var lfoSync: Boolean = false,
    var lfoSyncDivisionIndex: Int = 2, // Default 1/4
    val lfoMatrixAmounts: FloatArray = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TrackState
        if (!lfoMatrixAmounts.contentEquals(other.lfoMatrixAmounts)) return false
        return true
    }
    override fun hashCode(): Int = lfoMatrixAmounts.contentHashCode()
}

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
    private var isStepRecordMode = false
    private var isPadSamplingMode = false
    private var isPadMode = false
    private var isZenMode = false
    private var isFullscreenPads = false
    private var isKeyboardHidden = false
    private var isHelpMode = false
    private var isDemoPlaying = false
    private var activeTrackIndex = 0
    private val tracks = Array(4) { TrackState() }
    
    private var demoJob: kotlinx.coroutines.Job? = null
    private var demoToast: Toast? = null
    var isPollingEnabled = true
    private val padLinks = mutableMapOf<Int, MutableSet<Int>>()
    private val padTriggerModes = mutableMapOf<Int, Boolean>()
    private val padMappings = mutableMapOf<Int, String>()
    private var mappingSampleId: Int? = null
    private val padSamplePaths = mutableMapOf<Int, String>()
    private val padPannings = mutableMapOf<Int, Float>()
    private val lastAftertouch = mutableMapOf<Int, Float>()
    
    private var bpm = 120f
    private var bankIndex = 0
    private var stepPageIndex = 0
    private var numSteps = 16
    private var statusPollCounter = 0
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    private val stepButtonIds = listOf(
        R.id.step_0, R.id.step_1, R.id.step_2, R.id.step_3,
        R.id.step_4, R.id.step_5, R.id.step_6, R.id.step_7,
        R.id.step_8, R.id.step_9, R.id.step_10, R.id.step_11,
        R.id.step_12, R.id.step_13, R.id.step_14, R.id.step_15
    )

    private val beatPoller = object : Runnable {
        override fun run() {
            if (synthManager.isBeatStarted()) flashBeat()
            if (statusPollCounter % 30 == 0) updateLatencyStatus()
            statusPollCounter++
            if (isPollingEnabled) mainHandler.postDelayed(this, 16)
        }
    }
    private var sequencerPoller: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presetRepository = PresetRepository(this)
        patternRepository = PatternRepository(this)
        midiDeviceManager = MidiDeviceManager(this, synthManager).apply {
            onStatusChanged = { connected ->
                runOnUiThread {
                    binding.appBarMain.contentMain.topHeader.midiStatusIndicator.setBackgroundColor(
                        if (connected) ContextCompat.getColor(this@MainActivity, R.color.acid_green)
                        else android.graphics.Color.DKGRAY
                    )
                }
            }
        }

        val content = binding.appBarMain.contentMain
        content.topHeader.visualizerView.setSynthManager(synthManager)
        content.topHeader.visualizerView.setOnClickListener { if (isHelpMode) showHelp(getString(R.string.help_visualizer)) }
        content.topHeader.pdVisualizerView.setOnClickListener { if (isHelpMode) showHelp(getString(R.string.help_pd_visualizer)) }
        
        setupSynthViewListener(content)
        setupModeToggle(content)
        setupUtilityButtons(content)
        setupWaveformControls(content)
        setupSliders(content)
        setupAdsr(content)
        setupLfo(content)
        setupLfoMatrix(content)
        setupFilter(content)
        setupPresets(content)
        setupMetronome(content)
        setupSequencer(content)
        setupUnison(content)
        setupPadCustomization(content)
        setupBankManagement(content)
        setupEffects(content)
        setupWorkspaceRefinement(content)
        setupPatternManagement(content)
        setupTrackSelection(content)
        
        content.topHeader.toggleAutoLatency.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) { showHelp(getString(R.string.help_auto_latency)); content.topHeader.toggleAutoLatency.isChecked = !isChecked; return@setOnCheckedChangeListener }
            synthManager.setAutoLatencyEnabled(isChecked)
        }
    }

    private fun setupSynthViewListener(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.keyboardPadView!!.listener = object : KeyboardPadView.OnNoteEventListener {
            override fun onNoteOn(midi: Int, velocity: Float) {
                if (isPadMode) {
                    if (mappingSampleId != null) {
                        val sampleNames = arrayOf(getString(R.string.sample_kick_808), getString(R.string.sample_snare_909), getString(R.string.sample_hat_closed), getString(R.string.sample_hat_open), getString(R.string.sample_clap), getString(R.string.sample_rim))
                        synthManager.loadFactorySample(midi - 60, mappingSampleId!!)
                        padMappings[midi - 60] = sampleNames[mappingSampleId!!]
                        mappingSampleId = null
                        content.tvMappingStatus.text = getString(R.string.toast_sample_mapped)
                        updateMappingList(content)
                    } else if (isPadSamplingMode) {
                        synthManager.startPadSampling(midi - 60)
                    } else {
                        synthManager.padNoteOn(midi - 60, velocity)
                        padLinks[midi - 60]?.forEach { synthManager.padNoteOn(it, velocity) }
                    }
                } else {
                    if (isStepRecordMode) { synthManager.stepRecordNote(activeTrackIndex, midi); updateSequencerToggles(content) }
                    else if (isSequencerRecordMode) { if (synthManager.isSequencerPlaying()) synthManager.handleRealTimeNoteOn(activeTrackIndex, midi) else synthManager.recordSequencerNote(activeTrackIndex, midi); updateSequencerToggles(content) }
                    synthManager.noteOn(midi, velocity, activeTrackIndex)
                }
            }
            override fun onNoteOff(midi: Int) {
                if (isPadMode) {
                    if (isPadSamplingMode) synthManager.stopPadSampling()
                    else if (padTriggerModes[midi - 60] != true) { synthManager.padNoteOff(midi - 60); padLinks[midi - 60]?.forEach { if (padTriggerModes[it] != true) synthManager.padNoteOff(it) } }
                } else synthManager.noteOff(midi)
            }
            override fun onGridTouchStart(midi: Int) {}
            override fun onGridTouchEnd() {}
            override fun onPadLongPress(idx: Int) { if (mappingSampleId == null) showPadColorPicker(idx) }
            override fun onGesture(pb: Float, mod: Float) { synthManager.setPitchBend(pb); synthManager.setModulation(mod) }
            override fun onAftertouch(m: Int, a: Float) { synthManager.setAftertouch(m, a) }
        }
    }

    private fun setupModeToggle(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnModeToggle.setOnClickListener {
            if (isDemoPlaying) return@setOnClickListener
            isPadMode = !isPadMode
            content.btnModeToggle.text = if (isPadMode) getString(R.string.btn_mode_keys) else getString(R.string.btn_mode_pads)
            content.keyboardPadView.setMode(if (isPadMode) KeyboardPadView.Mode.PAD_GRID else KeyboardPadView.Mode.KEYBOARD)
            updateWorkspaceVisibility(content)
        }
    }

    private fun setupUtilityButtons(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnPolyToggle!!.setOnClickListener { isPoly = !isPoly; synthManager.setPolyphonic(isPoly); content.btnPolyToggle!!.text = if (isPoly) getString(R.string.btn_poly_on) else getString(R.string.btn_poly_off) }
        content.btnMockRec!!.setOnClickListener { isMockRec = !isMockRec; if (isMockRec) synthManager.startRecording(File(getExternalFilesDir(null), "rec.wav").absolutePath) else synthManager.stopRecording() }
    }

    private fun setupWaveformControls(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.toggleWaveform!!.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) {
                val idx = when (id) { R.id.btn_wave_sine -> 0; R.id.btn_wave_square -> 1; R.id.btn_wave_saw -> 2; R.id.btn_wave_triangle -> 3; R.id.btn_wave_morph -> 4; R.id.btn_wave_wt -> 5; else -> 0 }
                tracks[activeTrackIndex].waveformIndex = idx; synthManager.setTrackWaveform(activeTrackIndex, idx)
                content.seekMorph.visibility = if (id == R.id.btn_wave_morph) View.VISIBLE else View.GONE
                if (id == R.id.btn_wave_wt) showWavetableSelector()
            }
        }
    }

    private fun setupSliders(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.seekMorph.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { tracks[activeTrackIndex].morphProgress = p; synthManager.setTrackMorph(activeTrackIndex, p / 100f); content.tvMorphVal.text = String.format(Locale.US, "%.2f", p / 100f) }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        content.seekPd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { tracks[activeTrackIndex].pdProgress = p; synthManager.setTrackPhaseDistortion(activeTrackIndex, p / 100f); content.topHeader.pdVisualizerView.setPhaseDistortion(p / 100f); content.tvPdVal.text = "$p%" }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        content.seekPanning!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { tracks[activeTrackIndex].panningProgress = p; synthManager.setTrackPanning(activeTrackIndex, (p - 50) / 50f); val label = when { p < 45 -> "L${Math.abs(p - 50)}"; p > 55 -> "R${p - 50}"; else -> "C" }; content.tvPanVal!!.text = label }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        content.seekMasterVol!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { synthManager.setMasterVolume(p / 100f); content.tvMasterVolVal!!.text = String.format(Locale.US, "%d%%", p) }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        content.btnOctaveDown!!.setOnClickListener { if (octaveShift > -4) { octaveShift--; updateOctave() } }
        content.btnOctaveUp!!.setOnClickListener { if (octaveShift < 4) { octaveShift++; updateOctave() } }
    }

    private fun setupTrackSelection(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.toggleTrackSelection.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) {
                activeTrackIndex = when (id) { R.id.btn_track_0 -> 0; R.id.btn_track_1 -> 1; R.id.btn_track_2 -> 2; R.id.btn_track_3 -> 3; else -> 0 }
                syncUIWithTrack(content)
            }
        }
    }

    private fun syncUIWithTrack(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val t = tracks[activeTrackIndex]
        isPollingEnabled = false
        val btnId = when (t.waveformIndex) { 0 -> R.id.btn_wave_sine; 1 -> R.id.btn_wave_square; 2 -> R.id.btn_wave_saw; 3 -> R.id.btn_wave_triangle; 4 -> R.id.btn_wave_morph; 5 -> R.id.btn_wave_wt; else -> R.id.btn_wave_sine }
        content.toggleWaveform.check(btnId)
        content.seekAttack.progress = t.attackProgress; content.seekDecay.progress = t.decayProgress; content.seekSustain.progress = t.sustainProgress; content.seekRelease.progress = t.releaseProgress
        content.seekLfoRate.progress = t.lfoRateProgress; content.seekLfoDepth.progress = t.lfoDepthProgress
        content.toggleLfoSync.isChecked = t.lfoSync; content.spinnerLfoDiv.setSelection(t.lfoSyncDivisionIndex); content.spinnerLfoDiv.visibility = if (t.lfoSync) View.VISIBLE else View.GONE; content.seekLfoRate.isEnabled = !t.lfoSync
        content.seekFilterCutoff.progress = t.filterCutoffProgress; content.seekFilterRes.progress = t.filterResProgress
        content.seekMorph.progress = t.morphProgress; content.seekPd.progress = t.pdProgress; content.seekPanning.progress = t.panningProgress
        content.spinnerUnison.setSelection(when(t.unisonCount) { 2 -> 1; 4 -> 2; 8 -> 3; else -> 0 }); content.seekDetune.progress = t.unisonDetuneProgress
        updateLabels(content); updateSequencerToggles(content); isPollingEnabled = true
        Toast.makeText(this, "Editing Track ${activeTrackIndex + 1}", Toast.LENGTH_SHORT).show()
    }

    private fun showHelp(m: String) { AlertDialog.Builder(this).setTitle("Help").setMessage(m).setPositiveButton("OK", null).show() }

    private fun updateWorkspaceVisibility(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val root = content.root as androidx.constraintlayout.widget.ConstraintLayout
        if (isPollingEnabled) TransitionManager.beginDelayedTransition(root)
        val set = ConstraintSet(); set.clone(root)
        val kbVisible = !(isKeyboardHidden || isHelpMode)
        
        set.setVisibility(R.id.keyboard_pad_scroll_view, if (kbVisible) View.VISIBLE else View.GONE)
        content.keyboardPadView.visibility = if (kbVisible) View.VISIBLE else View.GONE
        content.togglePadsFullscreen.visibility = if (isPadMode) View.VISIBLE else View.GONE
        
        if (isPadMode && isFullscreenPads) {
            set.setVisibility(R.id.top_header, View.GONE)
            set.setVisibility(R.id.workspace_layout, View.GONE)
        } else {
            set.setVisibility(R.id.top_header, View.VISIBLE)
            set.setVisibility(R.id.workspace_layout, View.VISIBLE)
        }
        
        set.applyTo(root)
    }

    private fun updateMappingList(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.mappingContainer.removeAllViews()
        padMappings.forEach { (idx, name) -> content.mappingContainer.addView(android.widget.TextView(this).apply { text = "P$idx: $name" }) }
    }

    private fun setupWorkspaceRefinement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.topHeader.btnHelpMode.setOnClickListener { isHelpMode = !isHelpMode; updateWorkspaceVisibility(content) }
        content.topHeader.btnDemoMode.setOnClickListener { if (isDemoPlaying) { isDemoPlaying = false; demoJob?.cancel(); resetEngineState() } else runIntegratedDemo() }
        content.toggleZenMode.setOnCheckedChangeListener { _, isChecked -> isZenMode = isChecked; content.parameterContainer.visibility = if (isChecked) View.GONE else View.VISIBLE }
        content.toggleBrowser.setOnCheckedChangeListener { _, isChecked -> content.sidebarBrowser.visibility = if (isChecked) View.VISIBLE else View.GONE }
        content.toggleConfig.setOnCheckedChangeListener { _, isChecked -> content.configWorkspace.visibility = if (isChecked) View.VISIBLE else View.GONE }
        content.togglePadsFullscreen.setOnCheckedChangeListener { _, isChecked -> isFullscreenPads = isChecked; updateWorkspaceVisibility(content) }
        content.toggleKeyboard.setOnCheckedChangeListener { _, isChecked -> isKeyboardHidden = isChecked; updateWorkspaceVisibility(content) }
    }

    private fun updateLabels(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val t = tracks[activeTrackIndex]
        content.tvAttackVal!!.text = String.format(Locale.US, "%.3fs", (Math.pow(2000.0, t.attackProgress / 100.0) / 1000.0).toFloat())
        content.tvDecayVal!!.text = String.format(Locale.US, "%.3fs", (Math.pow(2000.0, t.decayProgress / 100.0) / 1000.0).toFloat())
        content.tvSustainVal!!.text = String.format(Locale.US, "%.2f", t.sustainProgress / 100f)
        content.tvReleaseVal!!.text = String.format(Locale.US, "%.3fs", (Math.pow(2000.0, t.releaseProgress / 100.0) / 1000.0).toFloat())
    }

    private fun setupAdsr(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val l = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                val t = tracks[activeTrackIndex]; val v = (Math.pow(2000.0, p / 100.0) / 1000.0).toFloat()
                when (sb) {
                    content.seekAttack -> { t.attackProgress = p; synthManager.setTrackAttack(activeTrackIndex, v) }
                    content.seekDecay -> { t.decayProgress = p; synthManager.setTrackDecay(activeTrackIndex, v) }
                    content.seekSustain -> { t.sustainProgress = p; synthManager.setTrackSustain(activeTrackIndex, p / 100f) }
                    content.seekRelease -> { t.releaseProgress = p; synthManager.setTrackRelease(activeTrackIndex, v) }
                }
                updateLabels(content)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
        content.seekAttack!!.setOnSeekBarChangeListener(l); content.seekDecay!!.setOnSeekBarChangeListener(l); content.seekSustain!!.setOnSeekBarChangeListener(l); content.seekRelease!!.setOnSeekBarChangeListener(l)
    }

    private fun setupLfo(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val l = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                val t = tracks[activeTrackIndex]
                if (sb == content.seekLfoRate) { t.lfoRateProgress = p; synthManager.setTrackLfoRate(activeTrackIndex, (Math.pow(200.0, p / 100.0) / 10.0).toFloat()) }
                else { t.lfoDepthProgress = p; synthManager.setTrackLfoDepth(activeTrackIndex, p / 100f) }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
        content.seekLfoRate!!.setOnSeekBarChangeListener(l); content.seekLfoDepth!!.setOnSeekBarChangeListener(l)
        val waveAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(getString(R.string.lfo_wave_sine), getString(R.string.lfo_wave_square), getString(R.string.lfo_wave_saw), getString(R.string.lfo_wave_triangle), getString(R.string.lfo_wave_random)))
        waveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        content.spinnerLfoWaveform!!.adapter = waveAdapter
        content.spinnerLfoWaveform!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { tracks[activeTrackIndex].lfoWaveformIndex = position; synthManager.setTrackLfoWaveform(activeTrackIndex, position) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val divOptions = arrayOf("1/1", "1/2", "1/4", "1/8", "1/16"); val divValues = floatArrayOf(4.0f, 2.0f, 1.0f, 0.5f, 0.25f)
        content.spinnerLfoDiv.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, divOptions)
        content.toggleLfoSync.setOnCheckedChangeListener { _, isChecked ->
            if (isHelpMode) {
                showHelp(getString(R.string.help_lfo_sync))
                content.toggleLfoSync.isChecked = !isChecked
                return@setOnCheckedChangeListener
            }
            val t = tracks[activeTrackIndex]; t.lfoSync = isChecked; content.spinnerLfoDiv.visibility = if (isChecked) View.VISIBLE else View.GONE
            content.seekLfoRate.isEnabled = !isChecked; synthManager.setTrackLfoSync(activeTrackIndex, isChecked, divValues[content.spinnerLfoDiv.selectedItemPosition])
        }
        content.spinnerLfoDiv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (isHelpMode) { showHelp(getString(R.string.help_lfo_div)); return }
                val t = tracks[activeTrackIndex]; t.lfoSyncDivisionIndex = pos; if (t.lfoSync) synthManager.setTrackLfoSync(activeTrackIndex, true, divValues[pos])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val targetOptions = listOf(getString(R.string.lfo_target_pitch), getString(R.string.lfo_target_volume), getString(R.string.lfo_target_filter), getString(R.string.lfo_target_phase_dist))
        content.spinnerAftertouchTarget!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, targetOptions)
        content.spinnerAftertouchTarget!!.setSelection(2)
        content.spinnerAftertouchTarget!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { synthManager.setAftertouchTarget(position) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupLfoMatrix(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnLfoMatrix.setOnClickListener {
            if (isHelpMode) { showHelp(getString(R.string.help_lfo_matrix)); return@setOnClickListener }
            val t = tracks[activeTrackIndex]; val targets = arrayOf("Pitch", "Volume", "Filter", "Phase Dist")
            val layout = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.VERTICAL; setPadding(48, 40, 48, 40) }
            
            val lfoPreview = ch.schmidlins.mini_synth.ui.MiniLfoView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 120)
                setWaveform(t.lfoWaveformIndex)
            }
            layout.addView(lfoPreview)

            for (i in 0 until 4) {
                val row = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; setPadding(0, 16, 0, 16) }
                val label = android.widget.TextView(this).apply { text = targets[i]; layoutParams = android.widget.LinearLayout.LayoutParams(160, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT) }
                val valText = android.widget.TextView(this).apply { text = String.format(Locale.US, "%.0f%%", t.lfoMatrixAmounts[i] * 100); layoutParams = android.widget.LinearLayout.LayoutParams(100, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT) }
                val seek = android.widget.SeekBar(this).apply {
                    max = 100; progress = (t.lfoMatrixAmounts[i] * 100).toInt()
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                            t.lfoMatrixAmounts[i] = p / 100f
                            valText.text = "$p%"
                            synthManager.setTrackLfoMatrixAmount(activeTrackIndex, i, t.lfoMatrixAmounts[i])
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                }
                row.addView(label); row.addView(seek); row.addView(valText)
                layout.addView(row)
            }
            AlertDialog.Builder(this).setTitle("LFO Modulation Matrix (T${activeTrackIndex + 1})").setView(layout).setPositiveButton("OK", null).show()
        }
    }

    private fun setupFilter(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val l = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                val t = tracks[activeTrackIndex]
                if (sb == content.seekFilterCutoff) { t.filterCutoffProgress = p; synthManager.setTrackFilterCutoff(activeTrackIndex, (20.0 * Math.pow(1000.0, p / 100.0)).toFloat()) }
                else { t.filterResProgress = p; synthManager.setTrackFilterResonance(activeTrackIndex, p / 100f) }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
        content.seekFilterCutoff!!.setOnSeekBarChangeListener(l); content.seekFilterRes!!.setOnSeekBarChangeListener(l)
    }

    private fun setupPresets(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSavePreset!!.setOnClickListener { val input = EditText(this); input.hint = getString(R.string.dialog_preset_name_hint); AlertDialog.Builder(this).setTitle(getString(R.string.dialog_save_preset_title)).setView(input).setPositiveButton(getString(R.string.btn_preset_save)) { _, _ -> saveCurrentPreset(input.text.toString().trim()) }.show() }
        content.btnLoadPreset!!.setOnClickListener { lifecycleScope.launch { val presets = presetRepository.presets.first(); if (presets.isNotEmpty()) { val names = presets.map { it.name }.toTypedArray(); AlertDialog.Builder(this@MainActivity).setTitle(getString(R.string.dialog_load_preset_title)).setItems(names) { _, which -> applyPreset(presets[which]) }.show() } } }
    }

    private fun saveCurrentPreset(name: String) {
        val t = tracks[activeTrackIndex]
        val p = SynthPreset(name = name, waveformIndex = t.waveformIndex, attack = (Math.pow(2000.0, t.attackProgress / 100.0) / 1000.0).toFloat(), decay = (Math.pow(2000.0, t.decayProgress / 100.0) / 1000.0).toFloat(), sustain = t.sustainProgress / 100f, release = (Math.pow(2000.0, t.releaseProgress / 100.0) / 1000.0).toFloat(), lfoRate = (Math.pow(200.0, t.lfoRateProgress / 100.0) / 10.0).toFloat(), lfoDepth = t.lfoDepthProgress / 100f, lfoWaveformIndex = t.lfoWaveformIndex, lfoTargetIndex = t.lfoTargetIndex, filterCutoff = (20.0 * Math.pow(1000.0, t.filterCutoffProgress / 100.0)).toFloat(), filterResonance = t.filterResProgress / 100f, panning = (t.panningProgress - 50) / 50f, morph = t.morphProgress / 100f, phaseDistortion = t.pdProgress / 100f, unisonCount = t.unisonCount, unisonDetune = t.unisonDetuneProgress.toFloat())
        lifecycleScope.launch { presetRepository.savePreset(p); Toast.makeText(this@MainActivity, "Preset '$name' saved", Toast.LENGTH_SHORT).show() }
    }

    private fun applyPreset(preset: SynthPreset) {
        val t = tracks[activeTrackIndex]
        t.waveformIndex = preset.waveformIndex; t.attackProgress = (preset.attack * 100).toInt(); t.decayProgress = (preset.decay * 100).toInt(); t.sustainProgress = (preset.sustain * 100).toInt(); t.releaseProgress = (preset.release * 100).toInt(); t.unisonCount = preset.unisonCount; t.unisonDetuneProgress = preset.unisonDetune.toInt()
        syncUIWithTrack(binding.appBarMain.contentMain)
    }

    private fun setupMetronome(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.topHeader.btnMetronomeToggle.setOnClickListener { isMetronomeEnabled = !isMetronomeEnabled; synthManager.setMetronomeEnabled(isMetronomeEnabled); content.topHeader.btnMetronomeToggle.text = if (isMetronomeEnabled) "ON" else "OFF" }
        content.topHeader.btnBpmDown.setOnClickListener { bpm -= 5; updateBpm() }
        content.topHeader.btnBpmUp.setOnClickListener { bpm += 5; updateBpm() }
        updateBpm()
    }

    private fun updateBpm() { synthManager.setBpm(bpm); binding.appBarMain.contentMain.topHeader.tvBpmValue.text = "BPM: ${bpm.toInt()}" }
    private fun updateLatencyStatus() { synthManager.checkAndApplyBufferSize(); val bufferSize = synthManager.getBufferSize(); val xRuns = synthManager.getXRunCount(); binding.appBarMain.contentMain.topHeader.tvLatencyStatus.text = "LATENCY: $bufferSize ($xRuns)" }
    private fun flashBeat() { val indicator = binding.appBarMain.contentMain.topHeader.beatIndicator; indicator.setBackgroundColor(ContextCompat.getColor(this, R.color.acid_green)); mainHandler.postDelayed({ indicator.setBackgroundColor(android.graphics.Color.DKGRAY) }, 100) }

    private fun setupSequencer(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSequencerPlay!!.setOnClickListener { val playing = !synthManager.isSequencerPlaying(); synthManager.setSequencerPlaying(playing); content.btnSequencerPlay!!.text = if (playing) "■" else "▶" }
        content.toggleSequencerRec!!.setOnCheckedChangeListener { _, isChecked -> isSequencerRecordMode = isChecked; synthManager.setSequencerRecording(isChecked) }
        content.btnSequencerClear!!.setOnClickListener { synthManager.clearSequencerTrack(activeTrackIndex); updateSequencerToggles(content) }
        content.btnSequencerExport!!.setOnClickListener { triggerExportSequence() }
        content.spinnerStepDuration!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("1/16", "1/8", "1/4", "1/2", "1/1"))
        content.spinnerStepDuration!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { synthManager.setSequencerStepDuration(floatArrayOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f)[position]) }; override fun onNothingSelected(parent: AdapterView<*>?) {} }
        stepButtonIds.forEachIndexed { i, id -> content.root.findViewById<android.widget.ToggleButton>(id)?.setOnCheckedChangeListener { _, isChecked -> val step = stepPageIndex * 16 + i; synthManager.setSequencerNote(activeTrackIndex, step, 60, isChecked) } }
        sequencerPoller = object : Runnable { private var lastStep = -1; override fun run() { val currentStep = synthManager.getSequencerCurrentStep(); if (currentStep != lastStep) { updateSequencerUI(content, currentStep, lastStep); lastStep = currentStep }; if (isPollingEnabled) mainHandler.postDelayed(this, 16) } }
        mainHandler.post(sequencerPoller!!)
    }

    private fun updateStepPageUI(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) { updateSequencerToggles(content) }
    private fun updateSequencerUI(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding, current: Int, last: Int) {
        if (last != -1 && last / 16 == stepPageIndex) content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[last % 16])?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        if (current / 16 == stepPageIndex) content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[current % 16])?.setBackgroundColor(ContextCompat.getColor(this, R.color.acid_green))
    }
    private fun updateSequencerToggles(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        for (i in 0 until 16) { val step = stepPageIndex * 16 + i; val activeNotes = synthManager.getSequencerActiveNotes(activeTrackIndex, step); val toggle = content.root.findViewById<android.widget.ToggleButton>(stepButtonIds[i]); toggle?.setOnCheckedChangeListener(null); toggle?.isChecked = activeNotes != null && activeNotes.isNotEmpty(); toggle?.setOnCheckedChangeListener { _, isChecked -> synthManager.setSequencerNote(activeTrackIndex, step, 60, isChecked) } }
    }

    private fun setupUnison(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val values = intArrayOf(1, 2, 4, 8)
        content.spinnerUnison.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("OFF", "2x", "4x", "8x"))
        content.spinnerUnison.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) { val t = tracks[activeTrackIndex]; t.unisonCount = values[pos]; synthManager.setTrackUnison(activeTrackIndex, t.unisonCount, t.unisonDetuneProgress.toFloat(), 1.0f) }; override fun onNothingSelected(parent: AdapterView<*>?) {} }
        content.seekDetune.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) { val t = tracks[activeTrackIndex]; t.unisonDetuneProgress = p; content.tvDetuneVal.text = p.toString(); synthManager.setTrackUnison(activeTrackIndex, t.unisonCount, p.toFloat(), 1.0f) }; override fun onStartTrackingTouch(sb: SeekBar?) {}; override fun onStopTrackingTouch(sb: SeekBar?) {} })
    }

    private fun setupPadCustomization(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {}
    private fun setupBankManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {}
    private fun setupEffects(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {}
    private fun setupPatternManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {}
    private fun refreshUiFromEngine() {}
    private fun triggerExportSequence() {}
    private fun updateOctave() {}

    private fun runIntegratedDemo() {
        demoJob = lifecycleScope.launch {
            try {
                isDemoPlaying = true
                showDemoToast(getString(R.string.demo_initializing))
                
                // Track 1: Morphing Lead
                synthManager.setTrackWaveform(0, 4) // Morph
                synthManager.setTrackLfoSync(0, true, 2.0f) // 2 beats
                
                // Route LFO to Pitch (Index 0) and Filter (Index 2)
                synthManager.setTrackLfoMatrixAmount(0, 0, 0.2f) // Vibrato
                synthManager.setTrackLfoMatrixAmount(0, 2, 0.8f) // Filter wobble
                
                showDemoToast("Stage 1: Modulation Matrix. Routing LFO to Pitch & Filter.")
                
                synthManager.noteOn(60, 0.8f, 0)
                for (i in 0..20) {
                    if (!isDemoPlaying) break
                    val m = (i / 20f) * 3.0f
                    synthManager.setTrackMorph(0, m)
                    if (i == 10) showDemoToast("Notice T1 morphing with perfectly synced LFO modulation.")
                    delay(200)
                }
                synthManager.noteOff(60)
                showDemoToast(getString(R.string.demo_complete))
            } finally { isDemoPlaying = false; resetEngineState() }
        }
    }

    private fun showDemoToast(m: String) { runOnUiThread { demoToast?.cancel(); demoToast = Toast.makeText(this, m, Toast.LENGTH_SHORT); demoToast?.show() } }

    private fun resetEngineState() { for (t in 0..3) { synthManager.setTrackWaveform(t, 0); synthManager.clearSequencerTrack(t) }; syncUIWithTrack(binding.appBarMain.contentMain) }

    override fun onStart() {
        super.onStart()
        synthManager.startEngine()
        midiDeviceManager.start()
        mainHandler.post(beatPoller)
        
        tracks.forEachIndexed { i, t ->
            synthManager.setTrackWaveform(i, t.waveformIndex)
            synthManager.setTrackLfoSync(i, t.lfoSync, floatArrayOf(4.0f, 2.0f, 1.0f, 0.5f, 0.25f)[t.lfoSyncDivisionIndex])
            for (target in 0 until 4) {
                synthManager.setTrackLfoMatrixAmount(i, target, t.lfoMatrixAmounts[target])
            }
            synthManager.setTrackAttack(i, (Math.pow(2000.0, t.attackProgress / 100.0) / 1000.0).toFloat())
            synthManager.setTrackDecay(i, (Math.pow(2000.0, t.decayProgress / 100.0) / 1000.0).toFloat())
            synthManager.setTrackSustain(i, t.sustainProgress / 100f)
            synthManager.setTrackRelease(i, (Math.pow(2000.0, t.releaseProgress / 100.0) / 1000.0).toFloat())
            synthManager.setTrackFilterCutoff(i, (20.0 * Math.pow(1000.0, t.filterCutoffProgress / 100.0)).toFloat())
            synthManager.setTrackFilterResonance(i, t.filterResProgress / 100f)
            synthManager.setTrackMorph(i, t.morphProgress / 100f)
            synthManager.setTrackPhaseDistortion(i, t.pdProgress / 100f)
            synthManager.setTrackPanning(i, (t.panningProgress - 50) / 50f)
        }
        syncUIWithTrack(binding.appBarMain.contentMain)
    }
    override fun onStop() { super.onStop(); mainHandler.removeCallbacks(beatPoller); midiDeviceManager.stop(); synthManager.stopEngine() }

    private fun showPadColorPicker(idx: Int) {}
    private fun showFactorySamplePicker(idx: Int) {}
    private fun showWavetableSelector() {}
}
