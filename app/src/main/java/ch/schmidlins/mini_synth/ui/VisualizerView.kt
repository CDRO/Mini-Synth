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
            val numBuckets = 64
            val bucketWidth = width.toFloat() / numBuckets
            val bottomY = height.toFloat()
            val maxBarHeight = height / 2f
            
            val minFreq = 20.0
            val maxFreq = 20000.0
            val logMin = Math.log10(minFreq)
            val logMax = Math.log10(maxFreq)
            
            for (i in 0 until numBuckets) {
                // Find frequency range for this bucket
                val fStart = Math.pow(10.0, logMin + (i.toDouble() / numBuckets) * (logMax - logMin))
                val fEnd = Math.pow(10.0, logMin + ((i + 1).toDouble() / numBuckets) * (logMax - logMin))
                
                // Map frequency to FFT bin
                // Freq = bin * (sampleRate / FFT_SIZE)
                val sampleRate = 48000.0
                val binStart = (fStart * 1024.0 / sampleRate).toInt().coerceIn(0, fftCount - 1)
                val binEnd = (fEnd * 1024.0 / sampleRate).toInt().coerceIn(binStart + 1, fftCount)
                
                var maxMag = 0.0f
                for (b in binStart until binEnd) {
                    maxMag = Math.max(maxMag, fftBuffer[b])
                }
                
                val magnitude = maxMag * 15f 
                val barHeight = (magnitude * maxBarHeight).coerceIn(0f, maxBarHeight)
                
                val left = i * bucketWidth
                canvas.drawRect(left, bottomY - barHeight, left + bucketWidth - 1f, bottomY, barPaint)
            }
        }
        
        postInvalidateDelayed(16)
    }
}
