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
    private val smoothedMagnitudes = FloatArray(64)
    private var lastFftTime = 0L
    private val barPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var gradient: android.graphics.LinearGradient? = null
    private val dividerPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.border_dim)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    fun setSynthManager(manager: SynthManager) {
        this.synthManager = manager
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val acidGreen = ContextCompat.getColor(context, R.color.acid_green)
            val electricBlue = ContextCompat.getColor(context, R.color.electric_blue)
            val vibrantRed = ContextCompat.getColor(context, R.color.vibrant_red)
            
            // Vertical gradient for bars, coverage for both split and full modes
            // Refined stops to bring back red at high amplitudes (above 70%)
            gradient = android.graphics.LinearGradient(
                0f, h.toFloat(), 0f, 0f,
                intArrayOf(acidGreen, electricBlue, vibrantRed),
                floatArrayOf(0f, 0.3f, 0.6f),
                android.graphics.Shader.TileMode.CLAMP
            )
            barPaint.shader = gradient
        }
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
        val centerY = height / 4f 
        val stepX = width.toFloat() / drawBuffer.size

        // Horizontal divider between Waveform and Spectrum
        canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, dividerPaint)

        path.moveTo(0f, centerY)
        for (i in drawBuffer.indices) {
            val x = i * stepX
            val sample = drawBuffer[i].coerceIn(-1f, 1f)
            val y = centerY - (sample * centerY * 0.8f)
            path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)

        // Update FFT magnitudes at 30fps
        val now = System.currentTimeMillis()
        if (now - lastFftTime > 32) {
            val fftCount = manager.getFftData(fftBuffer)
            if (fftCount > 0) {
                lastFftTime = now
                val numBuckets = 64
                val minFreq = 20.0
                val maxFreq = 20000.0
                val logMin = Math.log10(minFreq)
                val logMax = Math.log10(maxFreq)
                val sampleRate = 48000.0

                for (i in 0 until numBuckets) {
                    val fStart = Math.pow(10.0, logMin + (i.toDouble() / numBuckets) * (logMax - logMin))
                    val fEnd = Math.pow(10.0, logMin + ((i + 1).toDouble() / numBuckets) * (logMax - logMin))
                    
                    val binStart = (fStart * 1024.0 / sampleRate).toInt().coerceIn(0, fftCount - 1)
                    val binEnd = (fEnd * 1024.0 / sampleRate).toInt().coerceIn(binStart + 1, fftCount)
                    
                    var maxMag = 0.0f
                    for (b in binStart until binEnd) {
                        maxMag = Math.max(maxMag, fftBuffer[b])
                    }
                    
                    val targetMag = maxMag * 15f
                    smoothedMagnitudes[i] = (smoothedMagnitudes[i] * 0.7f) + (targetMag * 0.3f)
                }
            }
        }

        // Always draw the spectrum using current smoothed values
        val numBuckets = 64
        val bucketWidth = width.toFloat() / numBuckets
        val bottomY = height.toFloat()
        val maxBarHeight = height / 2f
        
        for (i in 0 until numBuckets) {
            val barHeight = (smoothedMagnitudes[i] * maxBarHeight).coerceIn(0f, maxBarHeight)
            val left = i * bucketWidth
            canvas.drawRect(left, bottomY - barHeight, left + bucketWidth - 1f, bottomY, barPaint)
        }
        
        postInvalidateDelayed(16)
    }
}
