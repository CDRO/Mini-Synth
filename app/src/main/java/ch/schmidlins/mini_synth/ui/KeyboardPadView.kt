package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class KeyboardPadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnNoteEventListener {
        fun onNoteOn(midi: Int, velocity: Float)
        fun onNoteOff(midi: Int)
    }

    enum class Mode { KEYBOARD, PAD_GRID }
    enum class Backlight { NONE, TOUCH, RECORD, PLAY }

    private var mode = Mode.KEYBOARD
    var listener: OnNoteEventListener? = null
    
    // UI state
    private val activeNotes = mutableMapOf<Int, Backlight>()
    private val pointerToNote = mutableMapOf<Int, Int>()
    
    // Paints
    private val whiteKeyPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    private val blackKeyPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
    private val borderPaint = Paint().apply { color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 2f }
    
    private val backlightTouchPaint = Paint().apply { color = Color.YELLOW; style = Paint.Style.FILL; alpha = 128 }
    private val backlightRecordPaint = Paint().apply { color = Color.RED; style = Paint.Style.FILL; alpha = 128 }
    private val backlightPlayPaint = Paint().apply { color = Color.BLUE; style = Paint.Style.FILL; alpha = 128 }

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
        
        // Cache White Keys
        whiteKeyRects.clear()
        for (i in 0 until 8) {
            whiteKeyRects.add(RectF(i * keyWidth, 0f, (i + 1) * keyWidth, keyHeight))
        }
        
        // Cache Black Keys
        blackKeyRects.clear()
        val blackKeyWidth = keyWidth * 0.6f
        val blackKeyHeight = keyHeight * 0.6f
        for (i in 0 until 7) {
            if (i == 2 || i == 6) continue
            val midi = 60 + getMidiOffsetForBlackKey(i)
            blackKeyRects[midi] = RectF(
                (i + 1) * keyWidth - blackKeyWidth / 2f,
                0f,
                (i + 1) * keyWidth + blackKeyWidth / 2f,
                blackKeyHeight
            )
        }
        
        // Cache Pads
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
        // White keys
        for (i in 0 until 8) {
            val rect = whiteKeyRects[i]
            canvas.drawRect(rect, whiteKeyPaint)
            canvas.drawRect(rect, borderPaint)
            
            val midi = 60 + getMidiOffsetForWhiteKey(i)
            drawBacklight(canvas, rect, midi)
        }
        
        // Black keys
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
            
            val midi = 60 + i
            drawBacklight(canvas, rect, midi)
        }
    }

    private fun drawBacklight(canvas: Canvas, rect: RectF, midi: Int) {
        activeNotes[midi]?.let { type ->
            val paint = when (type) {
                Backlight.TOUCH -> backlightTouchPaint
                Backlight.RECORD -> backlightRecordPaint
                Backlight.PLAY -> backlightPlayPaint
                else -> null
            }
            paint?.let { canvas.drawRect(rect, it) }
        }
    }

    private fun getMidiOffsetForWhiteKey(i: Int): Int {
        return when (i) {
            0 -> 0; 1 -> 2; 2 -> 4; 3 -> 5; 4 -> 7; 5 -> 9; 6 -> 11; 7 -> 12; else -> 0
        }
    }

    private fun getMidiOffsetForBlackKey(i: Int): Int {
        return when (i) {
            0 -> 1; 1 -> 3; 3 -> 6; 4 -> 8; 5 -> 10; else -> 0
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex
        val pointerId = event.getPointerId(actionIndex)
        val x = event.getX(actionIndex)
        val y = event.getY(actionIndex)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                performClick()
                val midi = getMidiAt(x, y)
                if (midi != -1) {
                    noteOn(pointerId, midi)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                noteOff(pointerId)
            }
            MotionEvent.ACTION_MOVE -> {
                // Update all pointers
                for (i in 0 until event.pointerCount) {
                    val pId = event.getPointerId(i)
                    val px = event.getX(i)
                    val py = event.getY(i)
                    val newMidi = getMidiAt(px, py)
                    
                    val currentMidi = pointerToNote[pId]
                    if (newMidi != currentMidi) {
                        noteOff(pId)
                        if (newMidi != -1) noteOn(pId, newMidi)
                    }
                }
            }
        }
        return true
    }

    private fun noteOn(pointerId: Int, midi: Int) {
        pointerToNote[pointerId] = midi
        activeNotes[midi] = Backlight.TOUCH
        listener?.onNoteOn(midi, 0.8f)
        invalidate()
    }

    private fun noteOff(pointerId: Int) {
        pointerToNote.remove(pointerId)?.let { midi ->
            // Only release synth note if NO OTHER pointers are on this midi note
            if (!pointerToNote.values.contains(midi)) {
                activeNotes.remove(midi)
                listener?.onNoteOff(midi)
            }
        }
        invalidate()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun getMidiAt(x: Float, y: Float): Int {
        if (mode == Mode.KEYBOARD) {
            // Check black keys first
            for ((midi, rect) in blackKeyRects) {
                if (rect.contains(x, y)) return midi
            }
            // Check white keys
            for (i in 0 until 8) {
                if (whiteKeyRects[i].contains(x, y)) return 60 + getMidiOffsetForWhiteKey(i)
            }
        } else {
            for (i in 0 until 16) {
                if (padRects[i].contains(x, y)) return 60 + i
            }
        }
        return -1
    }
    
    fun setNoteBacklight(midi: Int, type: Backlight) {
        if (type == Backlight.NONE) {
            activeNotes.remove(midi)
        } else {
            activeNotes[midi] = type
        }
        postInvalidate()
    }
}
