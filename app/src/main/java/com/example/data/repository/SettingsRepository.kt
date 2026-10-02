package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "retropad_settings")

class SettingsRepository(private val context: Context) {

    private val hapticFeedbackKey = booleanPreferencesKey("haptic_feedback")
    private val volumeToggleKey = booleanPreferencesKey("volume_toggle")
    private val showTouchEffectKey = booleanPreferencesKey("show_touch_effects")

    // In-memory runtime state for instant overlay response
    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning = _isOverlayRunning.asStateFlow()

    private val _isOverlayVisible = MutableStateFlow(true)
    val isOverlayVisible = _isOverlayVisible.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    val hapticFeedbackFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[hapticFeedbackKey] ?: true
    }

    val volumeToggleFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[volumeToggleKey] ?: true
    }

    val showTouchEffectFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[showTouchEffectKey] ?: true
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[hapticFeedbackKey] = enabled
        }
    }

    suspend fun setVolumeToggle(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[volumeToggleKey] = enabled
        }
    }

    suspend fun setShowTouchEffect(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[showTouchEffectKey] = enabled
        }
    }

    fun setOverlayRunning(running: Boolean) {
        _isOverlayRunning.value = running
    }

    fun setOverlayVisible(visible: Boolean) {
        _isOverlayVisible.value = visible
    }

    fun toggleOverlayVisible(): Boolean {
        val next = !_isOverlayVisible.value
        _isOverlayVisible.value = next
        return next
    }

    fun setEditMode(active: Boolean) {
        _isEditMode.value = active
    }

    fun toggleEditMode(): Boolean {
        val next = !_isEditMode.value
        _isEditMode.value = next
        return next
    }
}
