package com.reactnativevoisekappextension.callstate

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.reactnativevoisekappextension.notification.VoisekNotification
import com.reactnativevoisekappextension.utils.Constants

class VoisekCallStateHeadlessTaskService : HeadlessJsTaskService() {
  private val tag = "phoneStateService"
  override fun onCreate() {
    super.onCreate()
    createNotificationBlank()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    createNotificationBlank()
    return START_STICKY_COMPATIBILITY
  }

  override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
    val extras = intent.extras
    return if (extras != null) {
      HeadlessJsTaskConfig(
        "AndroidCallStateTask",
        Arguments.fromBundle(extras),
        500,
        true
      )
    } else return null
  }

  private fun createNotificationBlank() {
    val notification = VoisekNotification.createNotificationBlank(this)
    startForeground(Constants.NOT_FOREGROUND_ID, notification)
  }

}
