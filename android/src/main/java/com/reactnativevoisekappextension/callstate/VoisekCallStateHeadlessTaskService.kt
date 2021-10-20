package com.reactnativevoisekappextension.callstate

import android.app.Notification
import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class VoisekCallStateHeadlessTaskService : HeadlessJsTaskService() {
  private val tag = "phoneStateService"
  override fun onCreate() {
    super.onCreate()
    val notification: Notification? = VoisekCallStateHeadlessTaskServiceUtils.createBlankSetupNotification(this)
    startForeground(1, notification)
  }

  override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
    val extras = intent.extras
    Log.d(tag, "CALLING AndroidCallStateTask")
    return if (extras != null) {
      HeadlessJsTaskConfig(
        "AndroidCallStateTask",
        Arguments.fromBundle(extras),
        0,
        true
      )
    } else return null
  }
}
