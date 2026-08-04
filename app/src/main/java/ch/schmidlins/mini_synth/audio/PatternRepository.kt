package ch.schmidlins.mini_synth.audio

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Serializable
data class SynthPattern(
    val name: String,
    val grid: List<List<Int>>, 
    val stepDivision: Float
)

private val Context.patternDataStore by preferencesDataStore(name = "patterns_store")

class PatternRepository(private val context: Context) {
    private val PATTERNS_KEY = stringPreferencesKey("patterns_json")

    val patterns: Flow<List<SynthPattern>> = context.patternDataStore.data.map { prefs ->
        val json: String = prefs[PATTERNS_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<SynthPattern>>(json)
        } catch (e: Exception) {
            emptyList<SynthPattern>()
        }
    }

    suspend fun savePattern(pattern: SynthPattern) {
        context.patternDataStore.edit { prefs: MutablePreferences ->
            val currentJson: String = prefs[PATTERNS_KEY] ?: "[]"
            val currentList: MutableList<SynthPattern> = try {
                Json.decodeFromString<List<SynthPattern>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf<SynthPattern>()
            }
            
            val existingIndex = currentList.indexOfFirst { it.name == pattern.name }
            if (existingIndex != -1) {
                currentList[existingIndex] = pattern
            } else {
                currentList.add(pattern)
            }
            prefs[PATTERNS_KEY] = Json.encodeToString(currentList)
        }
    }

    suspend fun deletePattern(name: String) {
        context.patternDataStore.edit { prefs: MutablePreferences ->
            val currentJson: String = prefs[PATTERNS_KEY] ?: "[]"
            val currentList: MutableList<SynthPattern> = try {
                Json.decodeFromString<List<SynthPattern>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf<SynthPattern>()
            }
            currentList.removeAll { it.name == name }
            prefs[PATTERNS_KEY] = Json.encodeToString(currentList)
        }
    }
}
