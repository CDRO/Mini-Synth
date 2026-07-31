package ch.schmidlins.mini_synth.ui

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import android.graphics.Bitmap

@RunWith(AndroidJUnit4::class)
class KeyboardLifecycleTest {

    @Test
    fun testDrawBeforeLayoutDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = KeyboardPadView(context)
        
        // Simulate drawing before onSizeChanged has been called (w=0, h=0)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
    }

    @Test
    fun testTouchBeforeLayoutDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = KeyboardPadView(context)
        
        // Simulate touch event before layout
        val event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 50f, 50f, 0)
        view.dispatchTouchEvent(event)
        event.recycle()
    }
}
