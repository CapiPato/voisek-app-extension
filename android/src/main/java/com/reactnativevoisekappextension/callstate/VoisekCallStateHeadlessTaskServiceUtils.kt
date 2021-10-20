package com.reactnativevoisekappextension.callstate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat


object VoisekCallStateHeadlessTaskServiceUtils {
  const val SETUP_NOTIFICATION_CHANNEL = "voisek-extension.listener"
  private fun createSetupNotificationChannel(context: Context?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        SETUP_NOTIFICATION_CHANNEL,
        "Voisek Listener",
        NotificationManager.IMPORTANCE_HIGH
      )
      channel.setShowBadge(false)
      channel.setSound(null, null)
      val manager = (context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
      manager.createNotificationChannel(channel)
    }
  }

  fun createBlankSetupNotification(context: Context?): Notification? {
    var channel: String? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createSetupNotificationChannel(context)
      channel = SETUP_NOTIFICATION_CHANNEL
    }
    val notificationBuilder = NotificationCompat.Builder(
      context!!, channel!!
    )
    return notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
      .setCategory(Notification.CATEGORY_SERVICE)
      .build()
  }
}
