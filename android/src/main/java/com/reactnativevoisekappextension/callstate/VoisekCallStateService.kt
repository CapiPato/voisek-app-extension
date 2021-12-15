package com.reactnativevoisekappextension.callstate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import android.os.Build
import android.os.Bundle
import android.telecom.CallScreeningService
import android.telephony.PhoneStateListener
import android.telephony.PhoneStateListener.LISTEN_CALL_STATE
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.reactnativevoisekappextension.notification.VoisekNotification
import com.reactnativevoisekappextension.notification.VoisekNotificationService
import com.reactnativevoisekappextension.utils.Constants


class VoisekCallStateService : BroadcastReceiver() {
  private val tag = "phoneStateService"
  private var currentEvent: String? = null
  private var currentNumber: String? = null
  override fun onReceive(context: Context, intent: Intent) {
    if (isCanCallCheck(context)) {
      val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
      val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
      if (state == null) {
        invokeCallHeadlessTask(context, "ongoing", currentNumber)
        currentNumber = null
      } else if (currentEvent != state || (phoneNumber != null && currentNumber != phoneNumber)) {
        currentEvent = state
        if (phoneNumber != null) {
          currentNumber = phoneNumber
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
          if (currentNumber != null) {
            invokeCallHeadlessTask(context, "incoming", currentNumber)
          }
        }
        if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
          if (currentNumber != null) {
            invokeCallHeadlessTask(context, "offhook", currentNumber)
            currentNumber = null
          }
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
          if (currentNumber != null) {
            invokeCallHeadlessTask(context, "disconnected", null)
            currentNumber = null
          }
        }
      }
    }
  }

  private fun invokeCallHeadlessTask(context: Context?, event: String?, phoneNumber: String?) {
    try {
      val service = Intent(context, VoisekCallStateHeadlessTaskService::class.java)
      val bundle = Bundle()
      if (phoneNumber != null) {
        bundle.putString("phoneNumber", phoneNumber)
      }
      if (event != null) {
        bundle.putString("event", event)
      }
      service.putExtras(bundle)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context?.startForegroundService(service)
      } else {
        context?.startService(service)
      }
      HeadlessJsTaskService.acquireWakeLockNow(context)
    } catch (ex: IllegalStateException) {
      Log.e(tag, "ERROR", ex)
    }
  }

  private fun isCanCallCheck(context: Context): Boolean {
    val sharedPreferencesOptions = context.getSharedPreferences(
      Constants.CALLER_OPTIONS_KEY,
      CallScreeningService.MODE_PRIVATE
    )
    return sharedPreferencesOptions.getBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, false)
  }
}
