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
        fun onGridTouchStart(midi: Int)
        fun onGridTouchEnd()
        fun onPadLongPress(padIndex: Int)
    }

    enum class Mode { KEYBOARD, PAD_GRID }
    enum class Backlight(val bit: Int) { 
        TOUCH(1), RECORD(2), PLAY(4) 
    }

    private var mode = Mode.KEYBOARD
    private var baseNote = 60
    var gridColumns = 4
    var gridRows = 4
    var listener: OnNoteEventListener? = null
    
    // UI state - Thread safe bitmask storage
    private val noteStates = ConcurrentHashMap<Int, Int>()
    private val pointerToNote = mutableMapOf<Int, Int>()
    private val pointerStartPositions = mutableMapOf<Int, Float>() // pointerId -> startY
    private val heldMidiNotes = ConcurrentHashMap.newKeySet<Int>()
    private val padColors = ConcurrentHashMap<Int, Int>() // padIndex -> color
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val longPressRunnables = mutableMapOf<Int, Runnable>() // pointerId -> Runnable
    
    // Paints
    private val whiteKeyPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.surface_bright); style = Paint.Style.FILL }
    private val blackKeyPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.surface_dark); style = Paint.Style.FILL }
    private val borderPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.border_dim); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val textPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.off_white); textSize = 24f; textAlign = Paint.Align.CENTER }
    
    private val backlightTouchPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.acid_green); style = Paint.Style.FILL; alpha = 128 }
    private val backlightRecordPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.vibrant_red); style = Paint.Style.FILL; alpha = 128 }
    private val backlightPlayPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.electric_blue); style = Paint.Style.FILL; alpha = 128 }
    private val backlightHoldPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.acid_green); style = Paint.Style.STROKE; strokeWidth = 8f; alpha = 200 }
    private val customPadPaint = Paint().apply { style = Paint.Style.FILL }
    private val textFontMetrics = textPaint.fontMetrics

    // Cached Layouts
    private val whiteKeyRects = mutableListOf<RectF>()
    private val blackKeyRects = mutableMapOf<Int, RectF>()
    private val padRects = mutableListOf<RectF>()
    
    fun setMode(newMode: Mode) {
        if (mode != newMode) {
            heldMidiNotes.clear()
            noteStates.clear()
        }
        mode = newMode
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateLayouts(w, h)
    }

    private fun calculateLayouts(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

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
        val padSizeW = w / gridColumns.toFloat()
        val padSizeH = h / gridRows.toFloat()
        for (row in 0 until gridRows) {
            for (col in 0 until gridColumns) {
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
        if (whiteKeyRects.isEmpty()) return
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
        if (padRects.isEmpty()) return
        val totalPads = gridColumns * gridRows
        for (i in 0 until totalPads) {
            val rect = padRects[i]
            val customColor = padColors[i]
            if (customColor != null) {
                customPadPaint.color = customColor
                canvas.drawRect(rect, customPadPaint)
            } else {
                canvas.drawRect(rect, whiteKeyPaint)
            }
            canvas.drawRect(rect, borderPaint)
            val midi = baseNote + i
            drawBacklight(canvas, rect, midi)
            val textY = rect.centerY() - (textFontMetrics.ascent + textFontMetrics.descent) / 2f
            canvas.drawText("P$i", rect.centerX(), textY, textPaint)
        }
    }

    private fun drawBacklight(canvas: Canvas, rect: RectF, midi: Int) {
        val state = noteStates[midi] ?: 0
        val isHeld = heldMidiNotes.contains(midi)
        
        if (state == 0 && !isHeld) return
        
        val paint = when {
            (state and Backlight.TOUCH.bit) != 0 -> backlightTouchPaint
            (state and Backlight.RECORD.bit) != 0 -> backlightRecordPaint
            (state and Backlight.PLAY.bit) != 0 -> backlightPlayPaint
            else -> null
        }
        
        val inset = 8f
        val backlightRect = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        
        paint?.let {
            canvas.drawRect(backlightRect, it)
        }
        
        if (isHeld) {
            canvas.drawRect(backlightRect, backlightHoldPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val pId = event.getPointerId(actionIndex)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                performClick()
                val y = event.getY(actionIndex)
                pointerStartPositions[pId] = y
                val midi = getMidiAt(event.getX(actionIndex), y)
                if (midi != -1) {
                    noteOn(pId, midi)
                    if (mode == Mode.PAD_GRID) {
                        val padIndex = midi - baseNote
                        val runnable = Runnable { listener?.onPadLongPress(padIndex) }
                        longPressRunnables[pId] = runnable
                        handler.postDelayed(runnable, 500)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                longPressRunnables.remove(pId)?.let { handler.removeCallbacks(it) }
                val startY = pointerStartPositions.remove(pId) ?: event.getY(actionIndex)
                val currentY = event.getY(actionIndex)
                val midi = pointerToNote[pId]
                
                if (midi != null && mode == Mode.KEYBOARD) {
                    val deltaY = startY - currentY // Positive if sliding UP
                    if (deltaY > height * 0.5f) {
                        // HOLD triggered
                        heldMidiNotes.add(midi)
                    } else if (deltaY < -height * 0.2f && heldMidiNotes.contains(midi)) {
                        // RELEASE HOLD triggered by sliding down
                        heldMidiNotes.remove(midi)
                    }
                }
                
                noteOff(pId)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val currentY = event.getY(i)
                    val newMidi = getMidiAt(event.getX(i), currentY)
                    val oldMidi = pointerToNote[pid]
                    
                    if (newMidi != oldMidi) {
                        longPressRunnables.remove(pid)?.let { handler.removeCallbacks(it) }
                        
                        // Check for hold trigger on move too? 
                        // User said "when pressing and sliding up ... the key is hold, even when no longer pressed"
                        // This usually implies detection on release, but let's check.
                        
                        noteOff(pid)
                        if (newMidi != -1) {
                            noteOn(pid, newMidi)
                            pointerStartPositions[pid] = currentY // Reset start pos for new key
                        }
                    }
                }
            }
        }
        return true
    }

    private fun noteOn(pointerId: Int, midi: Int) {
        val isFirstTouch = pointerToNote.isEmpty()
        pointerToNote[pointerId] = midi
        updateNoteState(midi, Backlight.TOUCH.bit, true)
        
        // If it was held, we are re-triggering it or keeping it active
        // But if it was held and we touch it again, we might want to "un-hold" it? 
        // User said "hold is lost when sliding down".
        
        listener?.onNoteOn(midi, 0.8f)
        if (isFirstTouch && mode == Mode.PAD_GRID) {
            listener?.onGridTouchStart(midi)
        }
        invalidate()
    }

    private fun noteOff(pointerId: Int) {
        pointerToNote.remove(pointerId)?.let { midi ->
            if (!pointerToNote.values.contains(midi) && !heldMidiNotes.contains(midi)) {
                updateNoteState(midi, Backlight.TOUCH.bit, false)
                listener?.onNoteOff(midi)
            }
        }
        if (pointerToNote.isEmpty() && mode == Mode.PAD_GRID) {
            listener?.onGridTouchEnd()
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
            if (blackKeyRects.isEmpty() || whiteKeyRects.isEmpty()) return -1
            for ((midi, rect) in blackKeyRects) if (rect.contains(x, y)) return midi
            for (i in 0 until 8) if (whiteKeyRects[i].contains(x, y)) return baseNote + getMidiOffsetForWhiteKey(i)
        } else {
            if (padRects.isEmpty()) return -1
            // Optimized grid-based lookup for pads
            val col = (x / (width / gridColumns.toFloat())).toInt().coerceIn(0, gridColumns - 1)
            val row = (y / (height / gridRows.toFloat())).toInt().coerceIn(0, gridRows - 1)
            return baseNote + (row * gridColumns + col)
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

    fun setPadColor(padIndex: Int, color: Int?) {
        if (color == null) {
            padColors.remove(padIndex)
        } else {
            padColors[padIndex] = color
        }
        invalidate()
    }

    fun setGridDimensions(cols: Int, rows: Int) {
        gridColumns = cols
        gridRows = rows
        calculateLayouts(width, height)
        invalidate()
    }

    fun clearHeldNotes() {
        heldMidiNotes.forEach { midi ->
            listener?.onNoteOff(midi)
        }
        heldMidiNotes.clear()
        noteStates.clear()
        invalidate()
    }
}
