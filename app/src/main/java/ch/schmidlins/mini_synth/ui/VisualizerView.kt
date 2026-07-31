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

    fun setSynthManager(manager: SynthManager) {
        this.synthManager = manager
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val manager = synthManager ?: return
        val count = manager.getVisualizerData(buffer)
        
        if (count > 0) {
            // Shift existing data to the left and append new data
            // This creates a scrolling effect or just refreshes if count is large
            System.arraycopy(drawBuffer, count, drawBuffer, 0, drawBuffer.size - count)
            System.arraycopy(buffer, 0, drawBuffer, drawBuffer.size - count, count)
        }

        path.reset()
        val centerY = height / 2f
        val stepX = width.toFloat() / drawBuffer.size

        path.moveTo(0f, centerY)
        for (i in drawBuffer.indices) {
            val x = i * stepX
            // Clamp and scale: normalize to roughly +/- 0.5 then scale to 80% view height
            val sample = drawBuffer[i].coerceIn(-1f, 1f)
            val y = centerY - (sample * centerY * 0.8f)
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
        
        // Throttled invalidate to ~60fps
        postInvalidateDelayed(16)
    }
}
