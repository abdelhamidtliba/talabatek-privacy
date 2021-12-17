package com.example.food_ordering_talabatek_app_delivery_boy


import io.flutter.embedding.android.FlutterActivity

import android.content.Intent
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    private val channel ="delivery/channel"
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger,channel).setMethodCallHandler {
            call, result ->
            if(call.method == "playLocation")
            {
                Intent(this,LocationUpdateService ::class.java).also { intent ->
                    startService(intent)
                }

            }else if(call.method == "stopLocation")
            {
                Intent(this,LocationUpdateService ::class.java).also {intent ->
                    stopService(intent)
                }
            }else
            {
                result.notImplemented()
            }
        }
    }
}
