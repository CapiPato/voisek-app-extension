package com.reactnativevoisekappextension.callstate

import android.app.Notification
import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class VoisekCallStateHeadlessTaskInit : HeadlessJsTaskService() {
  private val tag = "HSJInit"
  override fun onCreate() {
    super.onCreate()
    val notification: Notification? = VoisekCallStateHeadlessTaskServiceUtils.createBlankSetupNotificationInit(this)
    startForeground(0, notification)
    Log.d(tag,"onCreate")
  }

  override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
    val extras = intent.extras
    return if (extras != null) {
      HeadlessJsTaskConfig(
        "AndroidCallStateTaskInit",
        Arguments.fromBundle(extras),
        0,
        true
      )
    } else return null
  }
}
