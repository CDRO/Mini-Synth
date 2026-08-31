package ch.schmidlins.mini_synth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.audio.SynthManager

class WaveformEditorDialog(
    private val padIndex: Int,
    private val synthManager: SynthManager,
    private val initialStart: Float = 0.0f,
    private val initialEnd: Float = 1.0f,
    private val initialReverse: Boolean = false,
    private val onSave: (Float, Float, Boolean) -> Unit
) : DialogFragment() {

    private var currentStart = initialStart
    private var currentEnd = initialEnd

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_waveform_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val waveformView = view.findViewById<WaveformView>(R.id.waveform_view)
        val toggleReverse = view.findViewById<ToggleButton>(R.id.toggle_reverse)
        val btnNormalize = view.findViewById<Button>(R.id.btn_normalize)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        val samples = synthManager.getPadSample(padIndex)
        if (samples != null) {
            waveformView.setSamples(samples)
        }
        waveformView.setBounds(initialStart, initialEnd)
        waveformView.onBoundsChanged = { start, end ->
            currentStart = start
            currentEnd = end
        }

        toggleReverse.isChecked = initialReverse

        btnNormalize.setOnClickListener {
            synthManager.normalizePad(padIndex)
            // Re-fetch samples if normalized (though gain is in metadata, 
            // the actual normalization might be destructive in engine or just gain based).
            // My implementation is gain based.
        }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val samples = synthManager.getPadSample(padIndex)
            if (samples != null) {
                val startS = synthManager.snapToZeroCrossing(padIndex, (currentStart * samples.size).toInt())
                val endS = synthManager.snapToZeroCrossing(padIndex, (currentEnd * samples.size).toInt())
                onSave(startS.toFloat() / samples.size, endS.toFloat() / samples.size, toggleReverse.isChecked)
            } else {
                onSave(currentStart, currentEnd, toggleReverse.isChecked)
            }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
