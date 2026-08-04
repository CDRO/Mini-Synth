package ch.schmidlins.mini_synth.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class ExportConsistencyTest {

    @Test
    fun testWavExportFidelity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = SynthManager()
        manager.startEngine()
        
        val exportFile = File(context.cacheDir, "test_export.wav")
        if (exportFile.exists()) exportFile.delete()
        
        // Create a simple pattern
        manager.clearSequencer()
        manager.setSequencerNote(0, 60, true)
        
        // Render
        manager.renderPatternToFile(exportFile.absolutePath)
        
        assertTrue("Exported file should exist", exportFile.exists())
        assertTrue("Exported file should not be empty", exportFile.length() > 44)
        
        // Verify RIFF Header
        val fis = FileInputStream(exportFile)
        val header = ByteArray(44)
        fis.read(header)
        fis.close()
        
        val riff = String(header.sliceArray(0..3))
        val wave = String(header.sliceArray(8..11))
        val fmt = String(header.sliceArray(12..15))
        
        assertEquals("RIFF", riff)
        assertEquals("WAVE", wave)
        assertEquals("fmt ", fmt)
        
        // Verify 16-bit PCM (audioFormat = 1)
        val format = ByteBuffer.wrap(header.sliceArray(20..21)).order(ByteOrder.LITTLE_ENDIAN).short
        assertEquals("Should be PCM format (1)", 1.toShort(), format)
        
        manager.stopEngine()
    }

    @Test
    fun testMultiNoteJniBridge() {
        val manager = SynthManager()
        manager.startEngine()
        
        manager.clearSequencer()
        manager.setSequencerNote(0, 60, true)
        manager.setSequencerNote(0, 64, true)
        manager.setSequencerNote(0, 67, true)
        
        val activeNotes = manager.getSequencerActiveNotes(0)
        assertNotNull(activeNotes)
        assertEquals(3, activeNotes!!.size)
        assertTrue(activeNotes.contains(60))
        assertTrue(activeNotes.contains(64))
        assertTrue(activeNotes.contains(67))
        
        manager.stopEngine()
    }
}
