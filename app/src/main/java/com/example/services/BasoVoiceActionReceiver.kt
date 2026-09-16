package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.domain.model.AndroidAction

class BasoVoiceActionReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_TRIGGER_LISTEN = "com.example.action.TRIGGER_LISTEN"
    const val ACTION_TOGGLE_FLASHLIGHT = "com.example.action.TOGGLE_FLASHLIGHT"
    const val ACTION_TOGGLE_WIFI = "com.example.action.TOGGLE_WIFI"
    const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    Log.d("BasoVoiceReceiver", "Received broadcast action: $action")

    val actionHandler = AndroidActionHandler(context.applicationContext)

    when (action) {
      ACTION_STOP_SERVICE -> {
        BasoVoiceBackgroundService.stopService(context.applicationContext)
      }
      ACTION_TOGGLE_FLASHLIGHT -> {
        val isCurrentlyOn = actionHandler.isFlashlightOn.value
        actionHandler.executeAction(AndroidAction.ToggleFlashlight(enabled = !isCurrentlyOn))
      }
      ACTION_TOGGLE_WIFI -> {
        actionHandler.executeAction(AndroidAction.ToggleWifi(openPanel = true))
      }
      ACTION_TRIGGER_LISTEN -> {
        BasoVoiceBackgroundService.triggerDirectListen(context.applicationContext)
      }
    }
  }
}
