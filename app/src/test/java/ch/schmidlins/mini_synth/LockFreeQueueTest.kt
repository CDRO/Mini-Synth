package ch.schmidlins.mini_synth

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

// Note: This is a placeholder since we can't test C++ classes directly in JUnit easily without JNI tests.
// But we can verify the Kotlin side of the visualizer logic if we mock SynthManager.

class VisualizerLogicTest {
    @Test
    fun `test buffer copy logic`() {
        val buffer = FloatArray(10) { it.toFloat() }
        val drawBuffer = FloatArray(10)
        
        System.arraycopy(buffer, 0, drawBuffer, 0, 10)
        
        assertEquals(5f, drawBuffer[5])
    }
}
