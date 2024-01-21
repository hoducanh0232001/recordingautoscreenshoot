package com.example.recordingautoscreenshoot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.screen_capture"
    private val receiver = ScreenCaptureReceiver()

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method == "startScreenCapture") {
                    // Gửi broadcast để thông báo ScreenCaptureReceiver bắt đầu chụp màn hình
                    val intent = Intent("com.example.screen_capture.ACTION_CAPTURE_SCREEN")
                    sendBroadcast(intent)
                    result.success(null)
                } else {
                    result.notImplemented()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(receiver, IntentFilter("com.example.screen_capture.ACTION_CAPTURE_SCREEN"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

