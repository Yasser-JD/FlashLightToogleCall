package com.jdcoding.flashlighttooglecall.adapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    private var isBlinking = false
    private val handler = Handler()
    private val blinkInterval = 500L // 500 milliseconds interval for blinking
    private var blinkCount = 0 // Counter to track the number of blinks

    override fun onReceive(context: Context, intent: Intent) {
        // Check if the intent action matches the expected value
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val sharedPreferences = context.getSharedPreferences("FlashlightPrefs", Context.MODE_PRIVATE)
            if (!sharedPreferences.getBoolean("flashlight_enabled", false)) {
                return
            }

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Start blinking the flashlight when phone is ringing
                    startBlinking(context)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Stop blinking the flashlight when call is answered or ends
                    stopBlinking(context)
                }
            }
        }
    }

    private fun startBlinking(context: Context) {
        if (isBlinking) return // Prevent starting if already blinking
        isBlinking = true
        blinkCount = 0 // Reset blink count when starting blinking
        handler.post(object : Runnable {
            override fun run() {
                if (isBlinking && blinkCount < 7) { // Stop after 7 blinks
                    toggleFlashlight(context, true) // Turn on the flashlight
                    handler.postDelayed({
                        if (isBlinking && blinkCount < 7) { // Stop after 7 blinks
                            toggleFlashlight(context, false) // Turn off the flashlight
                            blinkCount++ // Increment the blink count
                            handler.postDelayed(this, blinkInterval) // Repeat the toggle
                        } else {
                            stopBlinking(context) // Stop blinking after 7 blinks
                        }
                    }, blinkInterval / 2)
                }
            }
        })
    }

    private fun stopBlinking(context: Context) {
        if (!isBlinking) return // If not blinking, no need to stop
        isBlinking = false
        handler.removeCallbacksAndMessages(null) // Remove all pending tasks
        // Ensure flashlight is turned off
        turnOffFlashlight(context)
    }

    private fun toggleFlashlight(context: Context, turnOn: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, turnOn) // Toggle the flashlight
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false) // Turn off the flashlight
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}
