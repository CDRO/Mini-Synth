package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import ch.schmidlins.mini_synth.R
import ch.schmidlins.mini_synth.audio.SynthManager

class VisualizerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.acid_green)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val path = Path()
    private var synthManager: SynthManager? = null
    private val buffer = FloatArray(1024)
    private val drawBuffer = FloatArray(1024)
    private val fftBuffer = FloatArray(512)
    private val barPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.acid_green)
        style = Paint.Style.FILL
        alpha = 180
    }

    fun setSynthManager(manager: SynthManager) {
        this.synthManager = manager
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val manager = synthManager ?: return
        
        // Draw Waveform (Top Half)
        val count = manager.getVisualizerData(buffer)
        if (count > 0) {
            System.arraycopy(drawBuffer, count, drawBuffer, 0, drawBuffer.size - count)
            System.arraycopy(buffer, 0, drawBuffer, drawBuffer.size - count, count)
        }

        path.reset()
        val centerY = height / 4f // Center of top half
        val stepX = width.toFloat() / drawBuffer.size

        path.moveTo(0f, centerY)
        for (i in drawBuffer.indices) {
            val x = i * stepX
            val sample = drawBuffer[i].coerceIn(-1f, 1f)
            val y = centerY - (sample * centerY * 0.8f)
            path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)

        // Draw FFT Spectrum (Bottom Half)
        val fftCount = manager.getFftData(fftBuffer)
        if (fftCount > 0) {
            val barWidth = width.toFloat() / fftCount
            val bottomY = height.toFloat()
            val maxBarHeight = height / 2f
            
            for (i in 0 until fftCount) {
                // Magnitude scaling (very rough for now)
                val magnitude = fftBuffer[i] * 10f 
                val barHeight = (magnitude * maxBarHeight).coerceIn(0f, maxBarHeight)
                
                val left = i * barWidth
                canvas.drawRect(left, bottomY - barHeight, left + barWidth - 1f, bottomY, barPaint)
            }
        }
        
        postInvalidateDelayed(16)
    }
}
