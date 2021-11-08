package com.reactnativevoisekappextension.callstate

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.reactnativevoisekappextension.notification.VoisekNotification
import com.reactnativevoisekappextension.utils.Constants

class VoisekCallStateHeadlessTaskService : HeadlessJsTaskService() {
  private val tag = "phoneStateService"

  override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
    val extras = intent.extras
    return if (extras != null) {
      if (extras.containsKey("goingBackground")) {
        Log.d("phoneStateService", "goingBackground")
        createNotificationListeningBackground();
      } else {
        if (extras.containsKey("phoneNumber")) {
          createNotificationListeningStart()
        } else {
          createNotificationListeningEnd();
        }
      }
      HeadlessJsTaskConfig(
        "AndroidCallStateTask",
        Arguments.fromBundle(extras),
        0,
        true
      )
    } else return null
  }

  private fun createNotificationListeningBackground() {
    val notification = VoisekNotification.createNotificationListeningBackground(this)
    startForeground(Constants.NOT_FOREGROUND_ID, notification)
  }

  private fun createNotificationListeningStart() {
    val notification = VoisekNotification.createNotificationListeningStart(this)
    startForeground(Constants.NOT_FOREGROUND_ID, notification)
  }

  private fun createNotificationListeningEnd() {
    val notification = VoisekNotification.createNotificationListeningEnd(this)
    startForeground(Constants.NOT_FOREGROUND_ID, notification)
  }

  override fun onDestroy() {
    val notificationManager: NotificationManagerCompat =
      VoisekNotification.createFullScreenCallNotificationChannel(this)
    notificationManager.cancelAll()
    stopForeground(true)
  }

}
