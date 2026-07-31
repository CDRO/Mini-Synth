package ch.schmidlins.mini_synth.ui.transform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransformViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testTextsInitialization() {
        val viewModel = TransformViewModel()
        val texts = viewModel.texts.value
        
        // Ensure we have the expected 16 items
        assertEquals(16, texts?.size)
        assertTrue(texts?.get(0)?.contains("item # 1") == true)
        assertTrue(texts?.get(15)?.contains("item # 16") == true)
    }
}
