package com.reactnativevoisekappextension.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.reactnativevoisekappextension.utils.Constants


object VoisekNotification {
  const val TAG = "VNOT"

  fun createFullScreenCallNotificationChannel(
    context: Context
  ): NotificationManagerCompat {
    val notificationManager: NotificationManagerCompat =
      NotificationManagerCompat.from(context);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelId = Constants.VOISEK_NOT_CHANNEL_ID
      val channelName = Constants.VOISEK_NOT_CHANNEL_NAME
      val channelDesc = Constants.VOISEK_NOT_CHANNEL_DESC
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(channelId, channelName, importance)
      channel.description = channelDesc;
      channel.setShowBadge(false)
      channel.setBypassDnd(true);
      channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC;
      // Register the channel with the system
      val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
      manager.createNotificationChannel(channel)
    }
    return notificationManager;
  }

  fun showAFullScreenNotification(
    context: Context?,
    title: String,
    desc: String
  ): Notification? {
    if (context != null) {
      Log.d(TAG, "showAFullScreenNotification")
      val resourceId: Int =
        context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
      createFullScreenCallNotificationChannel(context)
      val pendingIntent = getPendingIntent(context)
      return createFullScreenCallNotification(
        context,
        pendingIntent,
        resourceId,
        title,
        desc
      )
    }
    return null
  }

  fun createNotificationBlank(context: Context): Notification? {
    if (context != null) {
      Log.d(TAG, "createNotificationBlank")
      val notName = Constants.VOISEK_NOT_CHANNEL_NAME
      val notDesc = Constants.VOISEK_NOT_CHANNEL_DESC
      val resourceId: Int =
        context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
      createFullScreenCallNotificationChannel(context)
      val pendingIntent = getPendingIntent(context)
      return createFullScreenCallNotification(
        context,
        pendingIntent,
        resourceId,
        notName,
        notDesc
      );
    }
    return null
  }

  private fun createFullScreenCallNotification(
    context: Context,
    pendingIntent: PendingIntent?,
    resourceId: Int,
    notName: String,
    notDesc: String
  ): Notification {
    val builder = NotificationCompat.Builder(context, Constants.VOISEK_NOT_CHANNEL_ID)
      .setSmallIcon(resourceId)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setContentTitle(notName)
      .setContentText(notDesc)
      .setColor(Color.RED)
      .setColorized(true)
    if (pendingIntent !== null) {
      //builder.setContentIntent(pendingIntent)
      builder.setFullScreenIntent(pendingIntent, true)
    }
    return builder.build();
  }

  private fun getPendingIntent(context: Context): PendingIntent? {
    val mainActivityClass = getMainActivityClass(context)
    val launchIntent: Intent? = Intent(context, mainActivityClass)
    return PendingIntent.getActivity(
      context,
      0,
      launchIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )
  }

  private fun getMainActivityClass(context: Context): Class<*>? {
    val packageName = context.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent == null || launchIntent.component == null) {
      Log.e(TAG, "Failed to get launch intent or component")
      return null
    }
    return try {
      Class.forName(launchIntent.component!!.className)
    } catch (e: ClassNotFoundException) {
      Log.e(TAG, "Failed to get main activity class")
      null
    }
  }
}
