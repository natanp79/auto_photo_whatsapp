package com.example.auto_photo_whatsapp

import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "auto_photo_whatsapp/native"

    override fun configureFlutterEngine(
        flutterEngine: FlutterEngine
    ) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelName
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "openAccessibilitySettings" -> {
                    val intent =
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

                    startActivity(intent)

                    result.success(true)
                }

                "startAutomation" -> {
                    startAutomationService()
                    result.success(true)
                }

                "stopAutomation" -> {
                    stopAutomationService()
                    result.success(true)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun startAutomationService() {

        AutomationState.running = true

        val serviceIntent = Intent(
            this,
            AutomationForegroundService::class.java
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopAutomationService() {

        AutomationState.running = false

        val serviceIntent = Intent(
            this,
            AutomationForegroundService::class.java
        )

        stopService(serviceIntent)
    }
}

object AutomationState {

    @Volatile
    var running: Boolean = false
}