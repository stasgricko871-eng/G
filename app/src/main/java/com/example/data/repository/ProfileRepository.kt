package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ControllerProfile
import com.example.data.model.VirtualButtonConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "retropad_profiles")

class ProfileRepository(private val context: Context) {

    private val profilesKey = stringPreferencesKey("saved_profiles_json")
    private val activeProfileIdKey = stringPreferencesKey("active_profile_id")

    private val _activeProfileFlow = MutableStateFlow<ControllerProfile>(ControllerProfile.createDefaultRetroRpg())
    val activeProfileFlow = _activeProfileFlow.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadInitialProfiles()
        }
    }

    val allProfilesFlow: Flow<List<ControllerProfile>> = context.profileDataStore.data.map { prefs ->
        val rawJson = prefs[profilesKey]
        if (rawJson.isNullOrBlank()) {
            val defaults = listOf(
                ControllerProfile.createDefaultRetroRpg(),
                ControllerProfile.createDefaultJoystickProfile(),
                ControllerProfile.createDefaultCustom()
            )
            defaults
        } else {
            parseProfilesJson(rawJson)
        }
    }

    private suspend fun loadInitialProfiles() {
        val prefs = context.profileDataStore.data.first()
        val rawJson = prefs[profilesKey]
        val activeId = prefs[activeProfileIdKey]

        val list = if (rawJson.isNullOrBlank()) {
            val defaults = listOf(
                ControllerProfile.createDefaultRetroRpg(),
                ControllerProfile.createDefaultJoystickProfile(),
                ControllerProfile.createDefaultCustom()
            )
            saveAllProfilesInternal(defaults)
            defaults
        } else {
            parseProfilesJson(rawJson)
        }

        val current = list.find { it.id == activeId } ?: list.firstOrNull() ?: ControllerProfile.createDefaultRetroRpg()
        _activeProfileFlow.value = current
    }

    private fun parseProfilesJson(rawJson: String): List<ControllerProfile> {
        val list = mutableListOf<ControllerProfile>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                if (obj != null) {
                    list.add(ControllerProfile.fromJsonObject(obj))
                }
            }
        } catch (_: Exception) {
            return listOf(
                ControllerProfile.createDefaultRetroRpg(),
                ControllerProfile.createDefaultJoystickProfile(),
                ControllerProfile.createDefaultCustom()
            )
        }
        return if (list.isEmpty()) {
            listOf(ControllerProfile.createDefaultRetroRpg())
        } else {
            list
        }
    }

    private suspend fun saveAllProfilesInternal(profiles: List<ControllerProfile>) {
        val array = JSONArray()
        for (p in profiles) {
            array.put(p.toJsonObject())
        }
        context.profileDataStore.edit { prefs ->
            prefs[profilesKey] = array.toString()
        }
    }

    suspend fun setActiveProfile(profileId: String) {
        val all = allProfilesFlow.first()
        val selected = all.find { it.id == profileId } ?: return
        context.profileDataStore.edit { prefs ->
            prefs[activeProfileIdKey] = profileId
        }
        _activeProfileFlow.value = selected
    }

    suspend fun saveProfile(profile: ControllerProfile) {
        val currentList = allProfilesFlow.first().toMutableList()
        val index = currentList.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            currentList[index] = profile
        } else {
            currentList.add(profile)
        }
        saveAllProfilesInternal(currentList)
        if (_activeProfileFlow.value.id == profile.id) {
            _activeProfileFlow.value = profile
        }
    }

    suspend fun updateActiveProfileButtons(buttons: List<VirtualButtonConfig>) {
        val current = _activeProfileFlow.value
        val updated = current.copy(buttons = buttons)
        saveProfile(updated)
    }

    suspend fun updateActiveProfileOpacity(opacity: Float) {
        val current = _activeProfileFlow.value
        val updated = current.copy(globalOpacity = opacity)
        saveProfile(updated)
    }

    suspend fun createProfile(name: String): ControllerProfile {
        val newProfile = ControllerProfile(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Новый профиль" },
            buttons = ControllerProfile.createDefaultRetroRpg().buttons,
            globalOpacity = 0.85f,
            isBuiltIn = false
        )
        val currentList = allProfilesFlow.first().toMutableList()
        currentList.add(newProfile)
        saveAllProfilesInternal(currentList)
        setActiveProfile(newProfile.id)
        return newProfile
    }

    suspend fun renameProfile(id: String, newName: String) {
        val currentList = allProfilesFlow.first().toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val updated = currentList[index].copy(name = newName.ifBlank { "Без названия" })
            currentList[index] = updated
            saveAllProfilesInternal(currentList)
            if (_activeProfileFlow.value.id == id) {
                _activeProfileFlow.value = updated
            }
        }
    }

    suspend fun duplicateProfile(id: String): ControllerProfile {
        val currentList = allProfilesFlow.first().toMutableList()
        val original = currentList.find { it.id == id } ?: _activeProfileFlow.value
        val duplicate = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Копия)",
            isBuiltIn = false
        )
        currentList.add(duplicate)
        saveAllProfilesInternal(currentList)
        return duplicate
    }

    suspend fun deleteProfile(id: String): Boolean {
        val currentList = allProfilesFlow.first().toMutableList()
        if (currentList.size <= 1) return false // Prevent deleting last remaining profile
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val toRemove = currentList[index]
            if (toRemove.isBuiltIn) {
                // Builtin profiles can still be kept or removed if user desires
            }
            currentList.removeAt(index)
            saveAllProfilesInternal(currentList)
            if (_activeProfileFlow.value.id == id) {
                val next = currentList.first()
                setActiveProfile(next.id)
            }
            return true
        }
        return false
    }

    suspend fun importProfileFromJson(jsonStr: String): Result<ControllerProfile> {
        val result = ControllerProfile.fromJsonString(jsonStr)
        if (result.isSuccess) {
            val profile = result.getOrThrow().copy(
                id = UUID.randomUUID().toString(),
                name = "${result.getOrThrow().name} (Импорт)",
                isBuiltIn = false
            )
            val currentList = allProfilesFlow.first().toMutableList()
            currentList.add(profile)
            saveAllProfilesInternal(currentList)
            setActiveProfile(profile.id)
            return Result.success(profile)
        }
        return Result.failure(result.exceptionOrNull() ?: IllegalArgumentException("Invalid JSON"))
    }
}
