package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ch.schmidlins.mini_synth.R
import kotlin.math.abs

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var samples: FloatArray? = null
    private var peaks: FloatArray? = null
    private var startPercent = 0.0f
    private var endPercent = 1.0f

    private val wavePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.electric_blue)
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val markerPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.acid_green)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val dimmedPaint = Paint().apply {
        color = Color.BLACK
        alpha = 150
    }

    private var activeMarker: Int = 0 // 0 none, 1 start, 2 end

    var onBoundsChanged: ((Float, Float) -> Unit)? = null

    fun setSamples(data: FloatArray) {
        samples = data
        generatePeaks()
        invalidate()
    }

    private fun generatePeaks() {
        val s = samples ?: return
        if (s.isEmpty()) return

        val numPeaks = 500
        peaks = FloatArray(numPeaks)
        val step = s.size / numPeaks
        for (i in 0 until numPeaks) {
            var maxVal = 0.0f
            val startIdx = i * step
            val endIdx = (i + 1) * step
            for (j in startIdx until endIdx.coerceAtMost(s.size)) {
                val absVal = abs(s[j])
                if (absVal > maxVal) maxVal = absVal
            }
            peaks!![i] = maxVal
        }
    }

    fun setBounds(start: Float, end: Float) {
        startPercent = start.coerceIn(0.0f, 1.0f)
        endPercent = end.coerceIn(startPercent, 1.0f)
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pks = peaks ?: return
        if (pks.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f

        // Draw Waveform using peaks
        for (i in pks.indices) {
            val x = (i.toFloat() / pks.size) * w
            val yTop = centerY - (pks[i] * centerY * 0.9f)
            val yBottom = centerY + (pks[i] * centerY * 0.9f)
            canvas.drawLine(x, yTop, x, yBottom, wavePaint)
        }

        // Draw Dimmed Areas
        canvas.drawRect(0f, 0f, startPercent * w, h, dimmedPaint)
        canvas.drawRect(endPercent * w, 0f, w, h, dimmedPaint)

        // Draw Markers
        canvas.drawLine(startPercent * w, 0f, startPercent * w, h, markerPaint)
        canvas.drawLine(endPercent * w, 0f, endPercent * w, h, markerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val w = width.toFloat()
        val p = (x / w).coerceIn(0.0f, 1.0f)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dStart = abs(p - startPercent)
                val dEnd = abs(p - endPercent)
                activeMarker = when {
                    dStart < 0.1f && dStart < dEnd -> 1
                    dEnd < 0.1f -> 2
                    else -> 0
                }
                if (activeMarker == 0) performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeMarker == 1) {
                    startPercent = p.coerceAtMost(endPercent - 0.01f)
                    invalidate()
                } else if (activeMarker == 2) {
                    endPercent = p.coerceAtLeast(startPercent + 0.01f)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (activeMarker != 0) {
                    onBoundsChanged?.invoke(startPercent, endPercent)
                }
                activeMarker = 0
            }
        }
        return activeMarker != 0 || super.onTouchEvent(event)
    }
}
