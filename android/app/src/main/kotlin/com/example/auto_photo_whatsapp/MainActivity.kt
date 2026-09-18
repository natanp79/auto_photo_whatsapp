package com.example.auto_photo_whatsapp

import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "auto_photo_whatsapp/native"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelName
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "openAccessibilitySettings" -> {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                    result.success(true)
                }

                "startAutomation" -> {
                    AutomationState.running = true
                    result.success(true)
                }

                "stopAutomation" -> {
                    AutomationState.running = false
                    result.success(true)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}

object AutomationState {
    @Volatile
    var running: Boolean = false
}
