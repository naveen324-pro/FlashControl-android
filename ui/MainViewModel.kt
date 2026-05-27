package com.example.flashcontrol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcontrol.controller.FlashlightController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val flashlightController = FlashlightController(application)

    private val _isOn = MutableStateFlow(false)
    val isOn: StateFlow<Boolean> = _isOn.asStateFlow()

    private val _isBlinking = MutableStateFlow(false)
    val isBlinking: StateFlow<Boolean> = _isBlinking.asStateFlow()

    private val _blinkInterval = MutableStateFlow(500f) // milliseconds
    val blinkInterval: StateFlow<Float> = _blinkInterval.asStateFlow()

    private val _brightness = MutableStateFlow(flashlightController.maxBrightnessLevel.toFloat())
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    val maxBrightness: Int = flashlightController.maxBrightnessLevel

    private var blinkJob: Job? = null
    
    // Tracks whether app is in foreground
    private var isAppInForeground = true

    fun toggleFlashlight() {
        val newState = !_isOn.value
        _isOn.value = newState
        
        if (newState) {
            applyFlashlightState()
            if (_isBlinking.value) {
                startBlinking()
            }
        } else {
            stopBlinking()
            flashlightController.turnOff()
        }
    }

    fun setBlinkMode(enabled: Boolean) {
        _isBlinking.value = enabled
        if (enabled && _isOn.value && isAppInForeground) {
            startBlinking()
        } else {
            stopBlinking()
            if (_isOn.value && isAppInForeground) {
                applyFlashlightState()
            }
        }
    }

    fun setBlinkInterval(intervalMs: Float) {
        _blinkInterval.value = intervalMs
    }

    fun setBrightness(level: Float) {
        _brightness.value = level
        if (_isOn.value && !_isBlinking.value && isAppInForeground) {
            applyFlashlightState()
        }
    }

    private fun startBlinking() {
        blinkJob?.cancel()
        blinkJob = viewModelScope.launch {
            var currentLightState = true
            while (isActive && isAppInForeground) {
                if (currentLightState) {
                    applyFlashlightState()
                } else {
                    flashlightController.turnOff()
                }
                currentLightState = !currentLightState
                delay(_blinkInterval.value.toLong())
            }
        }
    }

    private fun stopBlinking() {
        blinkJob?.cancel()
        blinkJob = null
    }

    private fun applyFlashlightState() {
        if (maxBrightness > 1) {
            flashlightController.turnOnWithBrightness(_brightness.value.toInt())
        } else {
            flashlightController.turnOn()
        }
    }

    fun onAppForegrounded() {
        isAppInForeground = true
        if (_isOn.value) {
            if (_isBlinking.value) {
                startBlinking()
            } else {
                applyFlashlightState()
            }
        }
    }

    fun onAppBackgrounded() {
        isAppInForeground = false
        stopBlinking()
        flashlightController.turnOff()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopBlinking()
        flashlightController.turnOff()
    }
}
