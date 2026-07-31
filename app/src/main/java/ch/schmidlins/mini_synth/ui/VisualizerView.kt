package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import ch.schmidlins.mini_synth.audio.SynthManager

class VisualizerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = 0xFFC0FF00.toInt() // Acid Green
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
        
        // Simple visualization: copy available data, or clear if none
        if (count > 0) {
            System.arraycopy(buffer, 0, drawBuffer, 0, count.coerceAtMost(drawBuffer.size))
        }

        path.reset()
        val centerY = height / 2f
        val stepX = width.toFloat() / drawBuffer.size

        path.moveTo(0f, centerY)
        for (i in drawBuffer.indices) {
            val x = i * stepX
            val y = centerY - (drawBuffer[i] * centerY * 0.8f)
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
        
        // Lowpass the visualizer a bit to avoid jitter, or just request next frame
        invalidate()
    }
}
