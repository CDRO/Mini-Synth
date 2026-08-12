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
        fun onGesture(pitchBend: Float, modulation: Float)
        fun onAftertouch(midi: Int, amount: Float)
    }

    enum class Mode { KEYBOARD, PAD_GRID }
    enum class Backlight(val bit: Int) { 
        TOUCH(1), RECORD(2), PLAY(4) 
    }

    private var mode = Mode.KEYBOARD
    private var baseNote = 60
    private var padOffset = 0
    private var lastPb = 0.0f
    var gridColumns = 4
    var gridRows = 4
    var listener: OnNoteEventListener? = null
    var isConfigMode = false
    
    // UI state - Thread safe bitmask storage
    private val noteStates = ConcurrentHashMap<Int, Int>()
    private val pointerToNote = mutableMapOf<Int, Int>()
    private val pointerAftertouch = mutableMapOf<Int, Float>() // pointerId -> aftertouch amount
    private val pointerStartPositionsX = mutableMapOf<Int, Float>() // pointerId -> startX
    private val pointerStartPositionsY = mutableMapOf<Int, Float>() // pointerId -> startY
    private val heldMidiNotes = ConcurrentHashMap.newKeySet<Int>()
    private val gesturePads = mutableMapOf<Int, MutableList<Int>>() // pointerId -> list of midis triggered in this swipe
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
    private val backlightBendPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.electric_blue); style = Paint.Style.FILL; alpha = 100 }
    private val gestureLinePaint = Paint().apply { color = ContextCompat.getColor(context, R.color.acid_green); style = Paint.Style.STROKE; strokeWidth = 10f; strokeCap = Paint.Cap.ROUND; alpha = 180 }
    private val customPadPaint = Paint().apply { style = Paint.Style.FILL }
    private val holdTextPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.acid_green); textSize = 32f; textAlign = Paint.Align.RIGHT; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    private val textFontMetrics = textPaint.fontMetrics

    // Cached Layouts
    private val whiteKeyRects = mutableListOf<RectF>()
    private val blackKeyRects = mutableMapOf<Int, RectF>()
    private val padRects = mutableListOf<RectF>()
    
    fun setMode(newMode: Mode) {
        if (mode != newMode) {
            clearHeldNotes()
        }
        mode = newMode
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mode == Mode.PAD_GRID) {
            val w = MeasureSpec.getSize(widthMeasureSpec)
            val hSize = MeasureSpec.getSize(heightMeasureSpec)
            val hMode = MeasureSpec.getMode(heightMeasureSpec)
            
            // Fixed base height per pad row if in ScrollView, otherwise use available height
            val rowHeight = if (hMode == MeasureSpec.UNSPECIFIED) 150 else hSize / gridRows
            val totalHeight = rowHeight * gridRows
            setMeasuredDimension(w, totalHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
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
            drawGestureLines(canvas)
        }
    }

    private fun drawGestureLines(canvas: Canvas) {
        if (gesturePads.isEmpty()) return
        
        gesturePads.forEach { (_, midis) ->
            var lastRect: RectF? = null
            midis.forEach { midi ->
                val padIndexOnScreen = midi - baseNote
                if (padIndexOnScreen in padRects.indices) {
                    val rect = padRects[padIndexOnScreen]
                    if (lastRect != null) {
                        canvas.drawLine(lastRect!!.centerX(), lastRect!!.centerY(), rect.centerX(), rect.centerY(), gestureLinePaint)
                    }
                    lastRect = rect
                }
            }
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
            val padIndex = padOffset + i
            val midi = baseNote + i // UI still uses baseNote for state tracking per screen
            drawBacklight(canvas, rect, midi)
            val textY = rect.centerY() - (textFontMetrics.ascent + textFontMetrics.descent) / 2f
            canvas.drawText("P$padIndex", rect.centerX(), textY, textPaint)
        }
    }

    private fun drawBacklight(canvas: Canvas, rect: RectF, midi: Int) {
        val state = noteStates[midi] ?: 0
        val isHeld = heldMidiNotes.contains(midi)
        
        // Find aftertouch for this midi note (max of all fingers on this note)
        var atAmount = 0.0f
        pointerToNote.forEach { (pid, note) ->
            if (note == midi) {
                atAmount = Math.max(atAmount, pointerAftertouch[pid] ?: 0.0f)
            }
        }

        if (state == 0 && !isHeld) return
        
        val paint = when {
            (state and Backlight.TOUCH.bit) != 0 -> {
                val p = if (Math.abs(lastPb) > 0.1f) backlightBendPaint else backlightTouchPaint
                // Modulate alpha based on aftertouch
                p.alpha = (128 + (atAmount * 127)).toInt()
                p
            }
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
            // Draw 'H' in the bottom right
            val textSize = if (mode == Mode.PAD_GRID) 20f else 32f
            holdTextPaint.textSize = textSize
            canvas.drawText("H", rect.right - (textSize/2f), rect.bottom - (textSize/2f), holdTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val pId = event.getPointerId(actionIndex)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                performClick()
                val x = event.getX(actionIndex)
                val y = event.getY(actionIndex)
                pointerStartPositionsX[pId] = x
                pointerStartPositionsY[pId] = y
                val midi = getMidiAt(x, y)
                if (midi != -1) {
                    if (mode == Mode.PAD_GRID && isConfigMode) {
                        listener?.onPadLongPress(midi - baseNote)
                    } else {
                        noteOn(pId, midi)
                        if (mode == Mode.PAD_GRID) {
                            gesturePads.getOrPut(pId) { mutableListOf() }.add(midi)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                longPressRunnables.remove(pId)?.let { handler.removeCallbacks(it) }
                pointerStartPositionsX.remove(pId)
                pointerAftertouch.remove(pId)
                val startY = pointerStartPositionsY.remove(pId) ?: event.getY(actionIndex)
                val currentY = event.getY(actionIndex)
                val midi = pointerToNote[pId]
                
                if (midi != null) {
                    val deltaY = startY - currentY // Positive if sliding UP
                    // Threshold for holding: sliding up significantly
                    if (deltaY > height * 0.4f) {
                        heldMidiNotes.add(midi)
                    } else if (deltaY < -height * 0.2f && heldMidiNotes.contains(midi)) {
                        heldMidiNotes.remove(midi)
                    }
                }
                
                // Release all pads triggered during this swipe
                gesturePads.remove(pId)?.forEach { m ->
                    if (!pointerToNote.values.contains(m) && !heldMidiNotes.contains(m)) {
                        updateNoteState(m, Backlight.TOUCH.bit, false)
                        val triggerIndex = if (mode == Mode.PAD_GRID) baseNote + padOffset + (m - baseNote) else m
                        listener?.onNoteOff(triggerIndex)
                    }
                }

                noteOff(pId)
                
                // Reset gesture on release
                lastPb = 0.0f
                listener?.onGesture(0.0f, 0.0f)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.PAD_GRID && isConfigMode) return true
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val currentX = event.getX(i)
                    val currentY = event.getY(i)
                    val oldMidi = pointerToNote[pid] ?: -1
                    
                    // Gesture detection (Horizontal slide for pitch bend, vertical for modulation)
                    val startX = pointerStartPositionsX[pid] ?: currentX
                    val startY = pointerStartPositionsY[pid] ?: currentY
                    
                    val keyWidth = width / 8f
                    val padWidth = width / gridColumns.toFloat()
                    val deltaX = currentX - startX
                    val deltaY = startY - currentY
                    
                    val pb = (deltaX / (if (mode == Mode.PAD_GRID) padWidth else keyWidth)) * 2.0f
                    val mod = (deltaY / height).coerceIn(0f, 1f)
                    
                    if (oldMidi != -1) {
                        lastPb = pb
                        listener?.onGesture(pb, mod)
                        
                        // Per-pointer aftertouch based on Y position within the view height (0 at bottom, 1 at top)
                        val atAmount = (1.0f - (currentY / height)).coerceIn(0f, 1f)
                        pointerAftertouch[pid] = atAmount
                        
                        // Use triggerIndex for aftertouch
                        val triggerIndex = if (mode == Mode.PAD_GRID) baseNote + padOffset + (oldMidi - baseNote) else oldMidi
                        listener?.onAftertouch(triggerIndex, atAmount)
                        
                        invalidate()
                    }

                    val newMidi = getMidiAt(currentX, currentY)
                    
                    if (newMidi != oldMidi && newMidi != -1) {
                        longPressRunnables.remove(pid)?.let { handler.removeCallbacks(it) }
                        
                        // In Pad Mode, swiping into a new pad keeps the old one ACTIVE 
                        // (forming a group for the duration of the touch)
                        if (mode == Mode.PAD_GRID) {
                            pointerToNote[pid] = newMidi
                            updateNoteState(newMidi, Backlight.TOUCH.bit, true)
                            val triggerIndex = baseNote + padOffset + (newMidi - baseNote)
                            listener?.onNoteOn(triggerIndex, 0.8f)
                            
                            val list = gesturePads.getOrPut(pid) { mutableListOf() }
                            if (list.lastOrNull() != newMidi) {
                                list.add(newMidi)
                            }
                            invalidate()
                        } else {
                            noteOff(pid)
                            noteOn(pid, newMidi)
                            pointerStartPositionsX[pid] = currentX
                            pointerStartPositionsY[pid] = currentY
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
        
        val triggerIndex = if (mode == Mode.PAD_GRID) baseNote + padOffset + (midi - baseNote) else midi
        listener?.onNoteOn(triggerIndex, 0.8f)
        
        if (isFirstTouch && mode == Mode.PAD_GRID) {
            listener?.onGridTouchStart(midi)
        }
        invalidate()
    }

    private fun noteOff(pointerId: Int) {
        pointerToNote.remove(pointerId)?.let { midi ->
            if (!pointerToNote.values.contains(midi) && !heldMidiNotes.contains(midi)) {
                updateNoteState(midi, Backlight.TOUCH.bit, false)
                val triggerIndex = if (mode == Mode.PAD_GRID) baseNote + padOffset + (midi - baseNote) else midi
                listener?.onNoteOff(triggerIndex)
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

    fun setPadOffset(offset: Int) {
        padOffset = offset
        invalidate()
    }
    fun getPadOffset() = padOffset

    fun clearHeldNotes() {
        heldMidiNotes.forEach { midi ->
            listener?.onNoteOff(midi)
        }
        heldMidiNotes.clear()
        noteStates.clear()
        invalidate()
    }
}
