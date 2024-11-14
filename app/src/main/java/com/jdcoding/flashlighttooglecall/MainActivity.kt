package com.jdcoding.flashlighttooglecall

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchFlashlight = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchFlashlight)
        val disableBatteryOptimizationsButton = findViewById<Button>(R.id.disableBatteryOptimizationsButton)

        // Check and request permissions if not already granted
        if (!hasPermissions()) {
            requestPermissions()
        }

        // Restore switch state from SharedPreferences
        val sharedPreferences = getSharedPreferences("FlashlightPrefs", Context.MODE_PRIVATE)
        switchFlashlight.isChecked = sharedPreferences.getBoolean("flashlight_enabled", false)

        // Handle switch toggle event
        switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("flashlight_enabled", isChecked)
            editor.apply()
        }

        // Disable battery optimization prompt
        disableBatteryOptimizationsButton.setOnClickListener {
            val intent = Intent()
            val packageName = packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun hasPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        return cameraPermission == PackageManager.PERMISSION_GRANTED && phoneStatePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, you can perform the required actions
            } else {
                // Permissions denied, inform the user that the app won't function properly
                Toast.makeText(this, "Permissions are required for the app to function", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
