package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import ch.schmidlins.mini_synth.R

class MiniLfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.electric_blue)
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var waveformIndex: Int = 0
    private val path = Path()

    fun setWaveform(index: Int) {
        this.waveformIndex = index
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val midY = h / 2f

        path.reset()
        val resolution = 100
        for (i in 0..resolution) {
            val t = i.toFloat() / resolution
            val phase = t * 2f * Math.PI.toFloat()
            val yNorm = when (waveformIndex) {
                0 -> Math.sin(phase.toDouble()).toFloat() // Sine
                1 -> if (phase < Math.PI) 1f else -1f // Square
                2 -> (phase / Math.PI.toFloat()) - 1f // Saw
                3 -> 2f * Math.abs((phase / Math.PI.toFloat()) - 1f) - 1f // Triangle
                else -> (Math.random() * 2 - 1).toFloat() // Random (not truly temporal here)
            }
            val x = t * w
            val y = midY - (yNorm * midY * 0.8f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)
    }
}
