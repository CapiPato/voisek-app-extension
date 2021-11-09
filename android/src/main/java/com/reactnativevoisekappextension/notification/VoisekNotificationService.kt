package com.reactnativevoisekappextension.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.reactnativevoisekappextension.utils.Constants
import java.util.Timer
import kotlin.concurrent.schedule


class VoisekNotificationService : Service() {
  override fun onBind(intent: Intent?): IBinder? {
    Log.d(VoisekNotification.TAG, "onBind")
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(VoisekNotification.TAG, "onStartCommand")
    val action = intent!!.action
    if (action != null) {
      if (action == Constants.NOT_ACTION_FOREGROUND_SHOW_NOT) {
        val extras = intent.extras
        if (extras != null) {
          if (extras.containsKey("title") && extras.containsKey("desc")) {
            val title = extras.getString("title")
            val desc = extras.getString("desc")
            if (title != null && desc != null) {
              val notificationManager: NotificationManagerCompat =
                VoisekNotification.createFullScreenCallNotificationChannel(this)
              val notification = VoisekNotification.showAFullScreenNotification(this, title, desc)
              notificationManager.cancelAll()
              stopForeground(true)
              if (notification != null) {
                notificationManager.notify(Constants.NOT_ID, notification)
                startForeground(
                  Constants.NOT_ID,
                  notification
                )
              }
            }
          }
        }
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    val notificationManager: NotificationManagerCompat =
      VoisekNotification.createFullScreenCallNotificationChannel(this)
    notificationManager.cancelAll()
    stopForeground(true)
  }

}
