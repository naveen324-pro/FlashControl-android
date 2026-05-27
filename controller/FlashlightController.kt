package com.example.flashcontrol.controller

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

class FlashlightController(context: Context) {

    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val cameraId: String? = try {
        cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            val isBackFacing = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            hasFlash && isBackFacing
        } ?: cameraManager.cameraIdList.firstOrNull { id ->
            // Fallback to any camera with flash if back-facing with flash isn't found
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
        }
    } catch (e: Exception) {
        null
    }

    val maxBrightnessLevel: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && cameraId != null) {
                try {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    return characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                } catch (e: Exception) {
                    return 1
                }
            }
            return 1
        }

    fun turnOn() {
        if (cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun turnOff() {
        if (cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun turnOnWithBrightness(level: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && cameraId != null) {
            try {
                // The level should be between 1 and maxBrightnessLevel
                val safeLevel = level.coerceIn(1, maxBrightnessLevel)
                cameraManager.turnOnTorchWithStrengthLevel(cameraId, safeLevel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            turnOn()
        }
    }
}
