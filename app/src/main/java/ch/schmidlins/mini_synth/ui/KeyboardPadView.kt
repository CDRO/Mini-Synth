package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ch.schmidlins.mini_synth.R
import java.util.concurrent.ConcurrentHashMap

class KeyboardPadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnNoteEventListener {
        fun onNoteOn(midi: Int, velocity: Float)
        fun onNoteOff(midi: Int)
    }

    enum class Mode { KEYBOARD, PAD_GRID }
    enum class Backlight(val bit: Int) { 
        TOUCH(1), RECORD(2), PLAY(4) 
    }

    private var mode = Mode.KEYBOARD
    private var baseNote = 60
    var listener: OnNoteEventListener? = null
    
    // UI state - Thread safe bitmask storage
    private val noteStates = ConcurrentHashMap<Int, Int>()
    private val pointerToNote = mutableMapOf<Int, Int>()
    
    // Paints
    private val whiteKeyPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.surface_bright); style = Paint.Style.FILL }
    private val blackKeyPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.surface_dark); style = Paint.Style.FILL }
    private val borderPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.border_dim); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val textPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.off_white); textSize = 24f; textAlign = Paint.Align.CENTER }
    
    private val backlightTouchPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.acid_green); style = Paint.Style.FILL; alpha = 128 }
    private val backlightRecordPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.vibrant_red); style = Paint.Style.FILL; alpha = 128 }
    private val backlightPlayPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.electric_blue); style = Paint.Style.FILL; alpha = 128 }

    // Cached Layouts
    private val whiteKeyRects = mutableListOf<RectF>()
    private val blackKeyRects = mutableMapOf<Int, RectF>()
    private val padRects = mutableListOf<RectF>()
    
    fun setMode(newMode: Mode) {
        mode = newMode
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val keyWidth = w / 8f
        val keyHeight = h.toFloat()
        
        whiteKeyRects.clear()
        for (i in 0 until 8) {
            whiteKeyRects.add(RectF(i * keyWidth, 0f, (i + 1) * keyWidth, keyHeight))
        }
        
        blackKeyRects.clear()
        val blackKeyWidth = keyWidth * 0.6f
        val blackKeyHeight = keyHeight * 0.6f
        for (i in 0 until 7) {
            if (i == 2 || i == 6) continue
            val midi = baseNote + getMidiOffsetForBlackKey(i)
            blackKeyRects[midi] = RectF(
                (i + 1) * keyWidth - blackKeyWidth / 2f, 0f,
                (i + 1) * keyWidth + blackKeyWidth / 2f, blackKeyHeight
            )
        }
        
        padRects.clear()
        val padSizeW = w / 4f
        val padSizeH = h / 4f
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                padRects.add(RectF(col * padSizeW, row * padSizeH, (col + 1) * padSizeW, (row + 1) * padSizeH))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mode == Mode.KEYBOARD) {
            drawKeyboard(canvas)
        } else {
            drawPadGrid(canvas)
        }
    }

    private fun drawKeyboard(canvas: Canvas) {
        for (i in 0 until 8) {
            val rect = whiteKeyRects[i]
            canvas.drawRect(rect, whiteKeyPaint)
            canvas.drawRect(rect, borderPaint)
            drawBacklight(canvas, rect, baseNote + getMidiOffsetForWhiteKey(i))
        }
        for ((midi, rect) in blackKeyRects) {
            canvas.drawRect(rect, blackKeyPaint)
            drawBacklight(canvas, rect, midi)
        }
    }

    private fun drawPadGrid(canvas: Canvas) {
        for (i in 0 until 16) {
            val rect = padRects[i]
            canvas.drawRect(rect, whiteKeyPaint)
            canvas.drawRect(rect, borderPaint)
            val midi = baseNote + i
            drawBacklight(canvas, rect, midi)
            canvas.drawText("P$i", rect.centerX(), rect.centerY() + 8f, textPaint)
        }
    }

    private fun drawBacklight(canvas: Canvas, rect: RectF, midi: Int) {
        val state = noteStates[midi] ?: 0
        if (state == 0) return
        
        val paint = when {
            (state and Backlight.TOUCH.bit) != 0 -> backlightTouchPaint
            (state and Backlight.RECORD.bit) != 0 -> backlightRecordPaint
            (state and Backlight.PLAY.bit) != 0 -> backlightPlayPaint
            else -> null
        }
        
        paint?.let {
            val inset = 8f
            val backlightRect = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
            canvas.drawRect(backlightRect, it)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val pId = event.getPointerId(actionIndex)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                performClick()
                val midi = getMidiAt(event.getX(actionIndex), event.getY(actionIndex))
                if (midi != -1) noteOn(pId, midi)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                noteOff(pId)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val newMidi = getMidiAt(event.getX(i), event.getY(i))
                    val oldMidi = pointerToNote[pid]
                    if (newMidi != oldMidi) {
                        noteOff(pid)
                        if (newMidi != -1) noteOn(pid, newMidi)
                    }
                }
            }
        }
        return true
    }

    private fun noteOn(pointerId: Int, midi: Int) {
        pointerToNote[pointerId] = midi
        updateNoteState(midi, Backlight.TOUCH.bit, true)
        listener?.onNoteOn(midi, 0.8f)
        invalidate()
    }

    private fun noteOff(pointerId: Int) {
        pointerToNote.remove(pointerId)?.let { midi ->
            if (!pointerToNote.values.contains(midi)) {
                updateNoteState(midi, Backlight.TOUCH.bit, false)
                listener?.onNoteOff(midi)
            }
        }
        invalidate()
    }

    private fun updateNoteState(midi: Int, bit: Int, active: Boolean) {
        noteStates.compute(midi) { _, current ->
            val old = current ?: 0
            if (active) old or bit else old and bit.inv()
        }
    }

    override fun performClick(): Boolean { return super.performClick() }

    private fun getMidiAt(x: Float, y: Float): Int {
        if (mode == Mode.KEYBOARD) {
            for ((midi, rect) in blackKeyRects) if (rect.contains(x, y)) return midi
            for (i in 0 until 8) if (whiteKeyRects[i].contains(x, y)) return baseNote + getMidiOffsetForWhiteKey(i)
        } else {
            // Optimized grid-based lookup for pads
            val col = (x / (width / 4f)).toInt().coerceIn(0, 3)
            val row = (y / (height / 4f)).toInt().coerceIn(0, 3)
            return baseNote + (row * 4 + col)
        }
        return -1
    }

    private fun getMidiOffsetForWhiteKey(i: Int) = when (i) {
        0 -> 0; 1 -> 2; 2 -> 4; 3 -> 5; 4 -> 7; 5 -> 9; 6 -> 11; 7 -> 12; else -> 0
    }
    private fun getMidiOffsetForBlackKey(i: Int) = when (i) {
        0 -> 1; 1 -> 3; 3 -> 6; 4 -> 8; 5 -> 10; else -> 0
    }
    
    fun setNoteBacklight(midi: Int, type: Backlight, active: Boolean) {
        updateNoteState(midi, type.bit, active)
        postInvalidate()
    }
}
