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
    val lfoMatrixAmounts: FloatArray = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f),
    var arpModeIndex: Int = 0,
    var arpDivisionIndex: Int = 2, // 1/4
    var arpOctaves: Int = 1,
    var chordModeIndex: Int = 0,
    var chordInversion: Int = 0
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
    private var isQuantizeEnabled = true
    private var isOverdubEnabled = true
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
        setupPerformanceControls(content)
        setupWorkspaceRefinement(content)
        setupPatternManagement(content)
        setupProjectManagement(content)
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
            it.applyClickEffect()
            if (isDemoPlaying) return@setOnClickListener
            isPadMode = !isPadMode
            content.btnModeToggle.text = if (isPadMode) getString(R.string.btn_mode_keys) else getString(R.string.btn_mode_pads)
            content.btnModeToggle.setBackgroundColor(if (isPadMode) ContextCompat.getColor(this, R.color.electric_blue) else ContextCompat.getColor(this, R.color.matte_grey))
            content.keyboardPadView.setMode(if (isPadMode) KeyboardPadView.Mode.PAD_GRID else KeyboardPadView.Mode.KEYBOARD)
            updateWorkspaceVisibility(content)
        }
    }

    private fun setupUtilityButtons(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnPolyToggle!!.setOnClickListener {
            it.applyClickEffect()
            isPoly = !isPoly
            synthManager.setPolyphonic(isPoly)
            content.btnPolyToggle!!.text = if (isPoly) getString(R.string.btn_poly_on) else getString(R.string.btn_poly_off)
            content.btnPolyToggle!!.setBackgroundColor(if (isPoly) ContextCompat.getColor(this, R.color.acid_green) else ContextCompat.getColor(this, R.color.matte_grey))
        }
        content.btnMockRec!!.setOnClickListener {
            it.applyClickEffect()
            isMockRec = !isMockRec
            setBlinking(it, isMockRec)
            if (isMockRec) synthManager.startRecording(File(getExternalFilesDir(null), "rec.wav").absolutePath) else synthManager.stopRecording()
        }
        content.btnMockPlay!!.setOnClickListener {
            it.applyClickEffect()
            isMockPlay = !isMockPlay
            synthManager.setSequencerPlaying(isMockPlay)
            content.btnMockPlay!!.setBackgroundColor(if (isMockPlay) ContextCompat.getColor(this, R.color.electric_blue) else ContextCompat.getColor(this, R.color.matte_grey))
        }
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
        content.btnOctaveDown!!.setOnClickListener { it.applyClickEffect(); if (octaveShift > -4) { octaveShift--; updateOctave(content) } }
        content.btnOctaveUp!!.setOnClickListener { it.applyClickEffect(); if (octaveShift < 4) { octaveShift++; updateOctave(content) } }
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
        content.spinnerArpMode.setSelection(t.arpModeIndex); content.spinnerArpDiv.setSelection(t.arpDivisionIndex); content.seekArpOctaves.progress = t.arpOctaves - 1
        content.spinnerChordMode.setSelection(t.chordModeIndex); content.seekChordInversion.progress = t.chordInversion
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

    private fun setupPerformanceControls(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val arpModes = arrayOf("OFF", "UP", "DOWN", "UPDOWN", "RANDOM")
        content.spinnerArpMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arpModes)
        content.spinnerArpMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                tracks[activeTrackIndex].arpModeIndex = pos
                synthManager.setTrackArpMode(activeTrackIndex, pos)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val arpDivs = arrayOf("1/1", "1/2", "1/4", "1/8", "1/16")
        val arpDivValues = floatArrayOf(4.0f, 2.0f, 1.0f, 0.5f, 0.25f)
        content.spinnerArpDiv.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arpDivs)
        content.spinnerArpDiv.setSelection(2) // 1/4
        content.spinnerArpDiv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                tracks[activeTrackIndex].arpDivisionIndex = pos
                synthManager.setTrackArpDivision(activeTrackIndex, arpDivValues[pos])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        content.seekArpOctaves.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                tracks[activeTrackIndex].arpOctaves = p + 1
                synthManager.setTrackArpOctaves(activeTrackIndex, p + 1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        val chordModes = arrayOf("OFF", "MAJOR", "MINOR", "DIM", "AUG", "MAJ7", "MIN7", "DOM7")
        content.spinnerChordMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, chordModes)
        content.spinnerChordMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                tracks[activeTrackIndex].chordModeIndex = pos
                synthManager.setTrackChordMode(activeTrackIndex, pos)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        content.seekChordInversion.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                tracks[activeTrackIndex].chordInversion = p
                synthManager.setTrackChordInversion(activeTrackIndex, p)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun setupWorkspaceRefinement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.topHeader.btnHelpMode.setOnClickListener {
            it.applyClickEffect()
            isHelpMode = !isHelpMode
            content.topHeader.btnHelpMode.setBackgroundColor(if (isHelpMode) ContextCompat.getColor(this, R.color.acid_green) else ContextCompat.getColor(this, R.color.matte_grey))
            updateWorkspaceVisibility(content)
        }
        content.topHeader.btnDemoMode.setOnClickListener {
            it.applyClickEffect()
            if (isDemoPlaying) { isDemoPlaying = false; demoJob?.cancel(); resetEngineState() } else runIntegratedDemo()
        }
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
        content.seekAttack?.setOnSeekBarChangeListener(l); content.seekDecay?.setOnSeekBarChangeListener(l); content.seekSustain?.setOnSeekBarChangeListener(l); content.seekRelease?.setOnSeekBarChangeListener(l)
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
        content.topHeader.btnMetronomeToggle.setOnClickListener {
            it.applyClickEffect()
            isMetronomeEnabled = !isMetronomeEnabled
            synthManager.setMetronomeEnabled(isMetronomeEnabled)
            content.topHeader.btnMetronomeToggle.text = if (isMetronomeEnabled) "ON" else "OFF"
            content.topHeader.btnMetronomeToggle.setBackgroundColor(if (isMetronomeEnabled) ContextCompat.getColor(this, R.color.acid_green) else ContextCompat.getColor(this, R.color.matte_grey))
        }
        content.topHeader.btnBpmDown.setOnClickListener { it.applyClickEffect(); bpm -= 5; updateBpm() }
        content.topHeader.btnBpmDownFine.setOnClickListener { it.applyClickEffect(); bpm -= 1; updateBpm() }
        content.topHeader.btnBpmUpFine.setOnClickListener { it.applyClickEffect(); bpm += 1; updateBpm() }
        content.topHeader.btnBpmUp.setOnClickListener { it.applyClickEffect(); bpm += 5; updateBpm() }
        updateBpm()
    }

    private fun updateBpm() { 
        bpm = bpm.coerceIn(40f, 300f)
        synthManager.setBpm(bpm)
        binding.appBarMain.contentMain.topHeader.tvBpmValue.text = bpm.toInt().toString()
    }
    private fun updateLatencyStatus() { synthManager.checkAndApplyBufferSize(); val bufferSize = synthManager.getBufferSize(); val xRuns = synthManager.getXRunCount(); binding.appBarMain.contentMain.topHeader.tvLatencyStatus.text = "LATENCY: $bufferSize ($xRuns)" }
    private fun flashBeat() { val indicator = binding.appBarMain.contentMain.topHeader.beatIndicator; indicator.setBackgroundColor(ContextCompat.getColor(this, R.color.acid_green)); mainHandler.postDelayed({ indicator.setBackgroundColor(android.graphics.Color.DKGRAY) }, 100) }

    private fun setupSequencer(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnSequencerPlay!!.setOnClickListener {
            it.applyClickEffect()
            val playing = !synthManager.isSequencerPlaying()
            synthManager.setSequencerPlaying(playing)
            content.btnSequencerPlay!!.text = if (playing) "■" else "▶"
            content.btnSequencerPlay!!.setBackgroundColor(if (playing) ContextCompat.getColor(this, R.color.electric_blue) else ContextCompat.getColor(this, R.color.matte_grey))
        }
        content.toggleSequencerRec!!.setOnCheckedChangeListener { cb, isChecked ->
            isSequencerRecordMode = isChecked
            synthManager.setSequencerRecording(isChecked)
            setBlinking(cb, isChecked)
        }
        content.btnSequencerOptions!!.setOnClickListener {
            it.applyClickEffect()
            val popup = android.widget.PopupMenu(this, it)
            popup.menu.add(0, 1, 0, getString(R.string.menu_quantize)).apply { isCheckable = true; isChecked = isQuantizeEnabled }
            popup.menu.add(0, 2, 0, getString(R.string.menu_overdub)).apply { isCheckable = true; isChecked = isOverdubEnabled }
            popup.menu.add(0, 3, 0, getString(R.string.menu_clear_all))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        isQuantizeEnabled = !isQuantizeEnabled
                        synthManager.setInputQuantize(isQuantizeEnabled)
                        Toast.makeText(this, if (isQuantizeEnabled) getString(R.string.toast_quantize_on) else getString(R.string.toast_quantize_off), Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        isOverdubEnabled = !isOverdubEnabled
                        synthManager.setOverdub(isOverdubEnabled)
                        Toast.makeText(this, if (isOverdubEnabled) getString(R.string.toast_overdub_on) else getString(R.string.toast_overdub_off), Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        AlertDialog.Builder(this).setMessage("Clear all tracks?").setPositiveButton("Yes") { _, _ -> synthManager.clearSequencer(); updateSequencerToggles(content) }.setNegativeButton("No", null).show()
                    }
                }
                true
            }
            popup.show()
        }
        content.btnSequencerClear!!.setOnClickListener { it.applyClickEffect(); synthManager.clearSequencerTrack(activeTrackIndex); updateSequencerToggles(content) }
        content.btnSequencerExport!!.setOnClickListener { it.applyClickEffect(); triggerExportSequence() }
        content.spinnerStepDuration!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("1/16", "1/8", "1/4", "1/2", "1/1"))
        content.spinnerStepDuration!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { synthManager.setSequencerStepDuration(floatArrayOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f)[position]) }; override fun onNothingSelected(parent: AdapterView<*>?) {} }
        content.spinnerLoopLength!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("16 Steps", "32 Steps", "48 Steps", "64 Steps"))
        content.spinnerLoopLength!!.setSelection(0)
        content.spinnerLoopLength!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                numSteps = (position + 1) * 16
                synthManager.setSequencerNumSteps(numSteps)
                updateStepPageUI(content)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        stepButtonIds.forEachIndexed { i, id -> content.root.findViewById<android.widget.ToggleButton>(id)?.setOnCheckedChangeListener { _, isChecked -> val step = stepPageIndex * 16 + i; synthManager.setSequencerNote(activeTrackIndex, step, 60, isChecked) } }
        
        content.togglePadSampling?.setOnCheckedChangeListener { cb, isChecked -> isPadSamplingMode = isChecked; setBlinking(cb, isChecked) }
        content.toggleStepRec?.setOnCheckedChangeListener { cb, isChecked ->
            isStepRecordMode = isChecked
            setBlinking(cb, isChecked)
            val vis = if (isChecked) View.VISIBLE else View.GONE
            content.btnStepRest.visibility = vis
            content.btnStepHold.visibility = vis
            content.btnStepBack.visibility = vis
        }

        content.btnStepRest.setOnClickListener { it.applyClickEffect(); synthManager.stepRecordRest(activeTrackIndex); updateStepPageUI(content) }
        content.btnStepHold.setOnClickListener { it.applyClickEffect(); synthManager.stepRecordHold(activeTrackIndex); updateStepPageUI(content) }
        content.btnStepBack.setOnClickListener { it.applyClickEffect(); synthManager.stepRecordBack(); updateStepPageUI(content) }
        content.btnStepPagePrev.setOnClickListener { it.applyClickEffect(); if (stepPageIndex > 0) { stepPageIndex--; updateStepPageUI(content) } }
        content.btnStepPageNext.setOnClickListener { it.applyClickEffect(); if (stepPageIndex < 3) { stepPageIndex++; updateStepPageUI(content) } }

        sequencerPoller = object : Runnable { private var lastStep = -1; override fun run() { val currentStep = synthManager.getSequencerCurrentStep(); if (currentStep != lastStep) { updateSequencerUI(content, currentStep, lastStep); lastStep = currentStep }; if (isPollingEnabled) mainHandler.postDelayed(this, 16) } }
        mainHandler.post(sequencerPoller!!)
    }

    private fun updateStepPageUI(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) { 
        content.tvStepPageValue.text = "${stepPageIndex + 1} / 4"
        updateSequencerToggles(content) 
    }
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

    private fun triggerExportSequence() {
        val path = File(getExternalFilesDir(null), "pattern_export.wav").absolutePath
        synthManager.renderPatternToFile(path)
        Toast.makeText(this, "Exported to pattern_export.wav", Toast.LENGTH_LONG).show()
    }

    private fun setupPadCustomization(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.togglePadEdit.setOnCheckedChangeListener { _, isChecked ->
            content.keyboardPadView.isConfigMode = isChecked
            content.togglePadEdit.setBackgroundColor(if (isChecked) ContextCompat.getColor(this, R.color.vibrant_red) else ContextCompat.getColor(this, R.color.matte_grey))
        }
        content.btnColsDown.setOnClickListener { it.applyClickEffect(); if (content.keyboardPadView.gridColumns > 1) { content.keyboardPadView.setGridDimensions(content.keyboardPadView.gridColumns - 1, content.keyboardPadView.gridRows); content.tvColsValue.text = content.keyboardPadView.gridColumns.toString() } }
        content.btnColsUp.setOnClickListener { it.applyClickEffect(); if (content.keyboardPadView.gridColumns < 8) { content.keyboardPadView.setGridDimensions(content.keyboardPadView.gridColumns + 1, content.keyboardPadView.gridRows); content.tvColsValue.text = content.keyboardPadView.gridColumns.toString() } }
        content.btnRowsDown.setOnClickListener { it.applyClickEffect(); if (content.keyboardPadView.gridRows > 1) { content.keyboardPadView.setGridDimensions(content.keyboardPadView.gridColumns, content.keyboardPadView.gridRows - 1); content.tvRowsValue.text = content.keyboardPadView.gridRows.toString() } }
        content.btnRowsUp.setOnClickListener { it.applyClickEffect(); if (content.keyboardPadView.gridRows < 8) { content.keyboardPadView.setGridDimensions(content.keyboardPadView.gridColumns, content.keyboardPadView.gridRows + 1); content.tvRowsValue.text = content.keyboardPadView.gridRows.toString() } }
    }
    private fun setupBankManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnBankDown.setOnClickListener { if (bankIndex > 0) { bankIndex--; updateBank(content) } }
        content.btnBankUp.setOnClickListener { if (bankIndex < 7) { bankIndex++; updateBank(content) } }
        updateBank(content)
    }

    private fun updateBank(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.tvBankValue.text = (bankIndex + 1).toString()
        // Pads start at note 60 (C3). Bank 1: 60-75, Bank 2: 76-91, etc.
        val baseNote = 60 + (bankIndex * 16)
        content.keyboardPadView.setPadBaseNote(baseNote)
    }

    private fun highlightView(view: View?) {
        if (view == null) return
        val originalAlpha = view.alpha
        lifecycleScope.launch {
            repeat(3) {
                view.alpha = 0.3f
                delay(150)
                view.alpha = 1.0f
                delay(150)
            }
            view.alpha = originalAlpha
        }
    }

    private fun View.applyClickEffect() {
        this.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                this.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    private val blinkingViews = mutableMapOf<View, kotlinx.coroutines.Job>()
    private fun setBlinking(view: View, enabled: Boolean) {
        blinkingViews[view]?.cancel()
        if (enabled) {
            blinkingViews[view] = lifecycleScope.launch {
                while (true) {
                    view.alpha = 0.2f
                    delay(400)
                    view.alpha = 1.0f
                    delay(400)
                }
            }
        } else {
            view.alpha = 1.0f
        }
    }
    private fun setupProjectManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        content.btnProjects.setOnClickListener {
            android.util.Log.d("MiniSynth", "Project button clicked")
            ProjectBrowserFragment(synthManager) {
                syncUIWithTrack(content)
            }.show(supportFragmentManager, "ProjectBrowser")
        }
    }

    private fun setupEffects(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        val l = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, f: Boolean) {
                val v = p / 100f
                when (sb) {
                    content.seekDelayTime -> synthManager.setDelayTime(v)
                    content.seekDelayFeedback -> synthManager.setDelayFeedback(v)
                    content.seekDelayMix -> synthManager.setDelayMix(v)
                    content.seekReverbSize -> synthManager.setReverbSize(v)
                    content.seekReverbDamping -> synthManager.setReverbDamping(v)
                    content.seekReverbMix -> synthManager.setReverbMix(v)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
        content.seekDelayTime.setOnSeekBarChangeListener(l)
        content.seekDelayFeedback.setOnSeekBarChangeListener(l)
        content.seekDelayMix.setOnSeekBarChangeListener(l)
        content.seekReverbSize.setOnSeekBarChangeListener(l)
        content.seekReverbDamping.setOnSeekBarChangeListener(l)
        content.seekReverbMix.setOnSeekBarChangeListener(l)
    }

    private fun setupPatternManagement(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {}
    private fun refreshUiFromEngine() {}
    private fun updateOctave(content: ch.schmidlins.mini_synth.databinding.ContentMainBinding) {
        synthManager.setOctaveShift(octaveShift)
        content.tvOctaveValue.text = octaveShift.toString()
    }

    private fun runIntegratedDemo() {
        demoJob = lifecycleScope.launch {
            try {
                isDemoPlaying = true
                val content = binding.appBarMain.contentMain
                showDemoToast("Welcome to Mini-Synth Ultra Tour!")
                delay(2000)

                // Stage 1: Synthesis Engines
                showDemoToast("Stage 1: The Engines. T1 Morphing Sine to Square.")
                highlightView(content.toggleWaveform)
                synthManager.setTrackWaveform(0, 0)
                synthManager.noteOn(60, 0.8f, 0)
                for (i in 0..10) {
                    synthManager.setTrackMorph(0, i / 10f * 3f)
                    delay(200)
                }
                synthManager.noteOff(60)

                // Stage 2: Sound Sculpting
                showDemoToast("Stage 2: Sculpting. Performing a Resonant Filter Sweep.")
                highlightView(content.seekFilterCutoff)
                synthManager.setTrackWaveform(0, 2) // Saw
                synthManager.setTrackFilterResonance(0, 0.8f)
                synthManager.noteOn(48, 0.9f, 0)
                for (i in 0..20) {
                    val cutoff = (20.0 * Math.pow(1000.0, i / 20.0)).toFloat()
                    synthManager.setTrackFilterCutoff(0, cutoff)
                    delay(100)
                }
                synthManager.noteOff(48)

                // Stage 3: Advanced Modulation
                showDemoToast("Stage 3: Modulation. Lock LFO to BPM.")
                highlightView(content.toggleLfoSync)
                synthManager.setTrackLfoSync(0, true, 1.0f) // 1 beat
                synthManager.setTrackLfoMatrixAmount(0, 2, 0.9f) // Filter wobble
                synthManager.noteOn(60, 0.8f, 0)
                delay(2000)
                synthManager.noteOff(60)

                // Stage 4: Workstation & Banks
                showDemoToast("Stage 4: Workstation. Switching Banks and Mapping Pads.")
                highlightView(content.btnBankUp)
                bankIndex = 1
                updateBank(content)
                delay(1000)
                showDemoToast("Mapping Track 1 sound to Pad 1...")
                synthManager.startAutomatedSampling(16, 2.0f)
                synthManager.noteOn(72, 0.8f, 0)
                delay(2200)
                synthManager.noteOff(72)
                showDemoToast("Pad 17 (Bank 2) now contains your lead sound!")
                delay(1500)

                // Stage 5: Recording
                showDemoToast("Stage 5: Recording. Record MIDI to Sequencer or Audio to WAV.")
                highlightView(content.toggleSequencerRec)
                synthManager.setSequencerRecording(true)
                synthManager.setSequencerNote(1, 0, 36, true)
                synthManager.setSequencerNote(1, 4, 36, true)
                synthManager.setSequencerPlaying(true)
                delay(2000)
                
                showDemoToast("Capturing high-quality audio recording...")
                highlightView(content.btnMockRec)
                synthManager.startRecording(File(getExternalFilesDir(null), "demo_out.wav").absolutePath)
                delay(2000)
                synthManager.stopRecording()

                // Stage 6: Projects
                showDemoToast("Stage 6: Performance. Arpeggiator and Chord Mode.")
                highlightView(content.performanceSection)
                synthManager.setTrackWaveform(0, 2) // Saw
                synthManager.setTrackArpMode(0, 1) // UP
                synthManager.setTrackArpDivision(0, 0.125f) // 1/32 for speed
                synthManager.setTrackChordMode(0, 1) // MAJOR
                synthManager.noteOn(60, 0.8f, 0)
                delay(3000)
                synthManager.noteOff(60)
                synthManager.setTrackArpMode(0, 0)
                synthManager.setTrackChordMode(0, 0)

                // Final Stage: Projects
                showDemoToast("Final Stage: Saving your masterpiece to a Project.")
                highlightView(content.btnProjects)
                synthManager.saveProject(File(getExternalFilesDir(null), "DemoProject").absolutePath)
                delay(2000)

                synthManager.setSequencerPlaying(false)
                bankIndex = 0
                updateBank(content)
                showDemoToast("Ultra Tour Complete! Now it's your turn.")
            } finally { isDemoPlaying = false; resetEngineState() }
        }
    }

    private fun showDemoToast(m: String) { runOnUiThread { demoToast?.cancel(); demoToast = Toast.makeText(this, m, Toast.LENGTH_SHORT); demoToast?.show() } }

    private fun resetEngineState() {
        for (t in 0..3) {
            synthManager.setTrackWaveform(t, 0)
            synthManager.clearSequencerTrack(t)
            synthManager.setTrackArpMode(t, 0)
            synthManager.setTrackChordMode(t, 0)
        }
        syncUIWithTrack(binding.appBarMain.contentMain)
    }

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
            synthManager.setTrackArpMode(i, t.arpModeIndex)
            synthManager.setTrackArpDivision(i, floatArrayOf(4.0f, 2.0f, 1.0f, 0.5f, 0.25f)[t.arpDivisionIndex])
            synthManager.setTrackArpOctaves(i, t.arpOctaves)
            synthManager.setTrackChordMode(i, t.chordModeIndex)
            synthManager.setTrackChordInversion(i, t.chordInversion)
        }
        syncUIWithTrack(binding.appBarMain.contentMain)
    }
    override fun onStop() { super.onStop(); mainHandler.removeCallbacks(beatPoller); midiDeviceManager.stop(); synthManager.stopEngine() }

    private fun showPadColorPicker(idx: Int) {}
    private fun showFactorySamplePicker(idx: Int) {}
    private fun showWavetableSelector() {}
}
