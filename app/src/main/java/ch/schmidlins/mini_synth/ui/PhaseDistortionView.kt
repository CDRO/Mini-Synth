package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import ch.schmidlins.mini_synth.R

class PhaseDistortionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.acid_green)
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.border_dim)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var phaseDistortion: Float = 0f
    private var isPathDirty = true

    fun setPhaseDistortion(value: Float) {
        if (Math.abs(this.phaseDistortion - value) > 0.001f) {
            this.phaseDistortion = value
            isPathDirty = true
            invalidate()
        }
    }

    private fun updatePath() {
        path.reset()
        val w = width.toFloat()
        val h = height.toFloat()
        val resolution = 100

        for (i in 0..resolution) {
            val t = i.toFloat() / resolution
            val inputPhase = t * 2f * Math.PI.toFloat()
            val distorted = inputPhase - (phaseDistortion * Math.sin(inputPhase.toDouble()).toFloat())
            val normalizedDistorted = distorted / (2f * Math.PI.toFloat())

            val x = t * w
            val y = h - (normalizedDistorted * h)

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        isPathDirty = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (isPathDirty && w > 0 && h > 0) {
            updatePath()
        }

        // Draw Center Grid
        canvas.drawLine(w / 2f, 0f, w / 2f, h, gridPaint)
        canvas.drawLine(0f, h / 2f, w, h / 2f, gridPaint)

        // Diagonal reference line (linear phase)
        canvas.drawLine(0f, h, w, 0f, gridPaint)

        canvas.drawPath(path, linePaint)
    }
}
