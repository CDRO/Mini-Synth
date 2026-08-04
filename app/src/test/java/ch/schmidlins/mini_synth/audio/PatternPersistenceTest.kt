package ch.schmidlins.mini_synth.audio

import androidx.test.core.app.ApplicationProvider
import ch.schmidlins.mini_synth.shadows.ShadowSynthManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [ShadowSynthManager::class])
class PatternPersistenceTest {

    @Test
    fun testPatternRepositorySerialization() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = PatternRepository(context)
        
        val testPattern = SynthPattern(
            name = "Test Unit Pattern",
            grid = listOf(
                listOf(60, 64),
                emptyList(),
                listOf(67)
            ) + List(13) { emptyList<Int>() },
            stepDivision = 0.25f
        )
        
        // Save
        repo.savePattern(testPattern)
        
        // Load
        val savedPatterns = repo.patterns.first()
        val loaded = savedPatterns.find { it.name == "Test Unit Pattern" }
        
        assertNotNull("Pattern should be found", loaded)
        assertEquals(testPattern.stepDivision, loaded!!.stepDivision)
        assertEquals(testPattern.grid[0], loaded.grid[0])
        
        // Cleanup
        repo.deletePattern("Test Unit Pattern")
        val finalPatterns = repo.patterns.first()
        assertNull(finalPatterns.find { it.name == "Test Unit Pattern" })
    }
}
