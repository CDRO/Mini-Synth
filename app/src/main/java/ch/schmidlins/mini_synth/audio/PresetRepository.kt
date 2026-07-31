package ch.schmidlins.mini_synth.audio

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "presets_store")

object StorageConstants {
    val PRESETS_KEY = stringPreferencesKey("presets_json")
}

class PresetRepository(private val context: Context) {
    val presets: Flow<List<SynthPreset>> = context.dataStore.data.map { preferences ->
        val json = preferences[StorageConstants.PRESETS_KEY] ?: "[]"
        try {
            Json.decodeFromString<List<SynthPreset>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun savePreset(preset: SynthPreset) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[StorageConstants.PRESETS_KEY] ?: "[]"
            val currentList = try {
                Json.decodeFromString<List<SynthPreset>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            val existingIndex = currentList.indexOfFirst { it.name == preset.name }
            if (existingIndex != -1) {
                currentList[existingIndex] = preset
            } else {
                currentList.add(preset)
            }
            
            preferences[StorageConstants.PRESETS_KEY] = Json.encodeToString(currentList)
        }
    }

    suspend fun deletePreset(name: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[StorageConstants.PRESETS_KEY] ?: "[]"
            val currentList = try {
                Json.decodeFromString<List<SynthPreset>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            currentList.removeAll { it.name == name }
            preferences[StorageConstants.PRESETS_KEY] = Json.encodeToString(currentList)
        }
    }
}
