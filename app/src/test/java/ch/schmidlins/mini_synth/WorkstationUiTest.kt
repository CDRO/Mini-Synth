package ch.schmidlins.mini_synth

import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLooper
import android.widget.Button
import org.junit.Before

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class WorkstationUiTest {

    @Before
    fun setUp() {
        ShadowSynthManager.reset()
    }

    @Test
    fun testOctaveButtonsInvokeEngine() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val btnUp = activity.findViewById<Button>(R.id.btn_octave_up)
        
        btnUp.performClick()
        ShadowLooper.idleMainLooper()
        
        assertEquals(1, ShadowSynthManager.lastOctaveShift)
    }

    @Test
    fun testPolyButtonToggle() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val btnPoly = activity.findViewById<Button>(R.id.btn_poly_toggle)
        
        val onText = activity.getString(R.string.btn_poly_on)
        val offText = activity.getString(R.string.btn_poly_off)
        
        assertEquals(onText, btnPoly.text.toString())
        btnPoly.performClick()
        ShadowLooper.idleMainLooper()
        assertEquals(offText, btnPoly.text.toString())
        
        assertFalse(ShadowSynthManager.lastPolyphonic)
    }

    @Test
    fun testMetronomeToggle() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val btnMetro = activity.findViewById<Button>(R.id.btn_metronome_toggle)
        
        assertEquals("MET", btnMetro.text.toString())
        btnMetro.performClick()
        ShadowLooper.idleMainLooper()
        assertEquals("ON", btnMetro.text.toString())
    }
    
    @Test
    fun testBpmAdjustment() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val btnUp = activity.findViewById<Button>(R.id.btn_bpm_up)
        
        btnUp.performClick() 
        ShadowLooper.idleMainLooper()
        
        assertEquals(125f, ShadowSynthManager.lastBpm, 0.1f)
    }
}
