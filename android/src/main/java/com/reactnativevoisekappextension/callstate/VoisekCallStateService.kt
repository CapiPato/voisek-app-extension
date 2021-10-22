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
import com.reactnativevoisekappextension.utils.Constants


class VoisekCallStateService : BroadcastReceiver() {
  private val tag = "phoneStateService"
  override fun onReceive(context: Context, intent: Intent) {
    if(isCanCallCheck(context)) {
      Log.d(tag, "onReceive")
      val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
      val monitor = StateMonitor(context)
      telephonyManager.listen(monitor, LISTEN_CALL_STATE)
    }
  }

  private fun isCanCallCheck(context: Context): Boolean
   {
      val sharedPreferencesOptions = context.getSharedPreferences(Constants.CALLER_OPTIONS_KEY,
        CallScreeningService.MODE_PRIVATE
      )
      return sharedPreferencesOptions.getBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, false)
    }

  private inner class StateMonitor(private val context: Context) : PhoneStateListener() {
    private var wasAppInOffHook = false
    private var wasAppInRinging = false
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
      when (state) {
        TelephonyManager.CALL_STATE_IDLE -> {
          if (wasAppInOffHook) { // if there was an ongoing call and the call state switches to idle, the call must have gotten disconnected
            invokeCallHeadlessTask("disconnected", phoneNumber)
          } else if (wasAppInRinging) { // if the phone was ringing but there was no actual ongoing call, it must have gotten missed
            invokeCallHeadlessTask("missed", phoneNumber)
          }
          //reset device state
          wasAppInRinging = false
          wasAppInOffHook = false
        }
        TelephonyManager.CALL_STATE_OFFHOOK -> {
          //Device call state: Off-hook. At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.
          wasAppInOffHook = true
          invokeCallHeadlessTask("offhook", phoneNumber)
        }
        TelephonyManager.CALL_STATE_RINGING -> {
          // Device call state: Ringing. A new call arrived and is ringing or waiting. In the latter case, another call is already active.
          wasAppInRinging = true
          invokeCallHeadlessTask("incoming", phoneNumber)
        }
      }
    }

    private fun invokeCallHeadlessTask(event: String?, phoneNumber: String?){
      try {
        val service = Intent(context, VoisekCallStateHeadlessTaskService::class.java)
        val bundle = Bundle()
        bundle.putString("phoneNumber", phoneNumber)
        bundle.putString("event", event)
        service.putExtras(bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(service)
        }
        else{
          context.startService(service)
        }
        HeadlessJsTaskService.acquireWakeLockNow(context)
      } catch (ex: IllegalStateException) {
        // By default, data only messages are "default" priority and cannot trigger Headless tasks
        Log.e(tag, "ERROR", ex)
      }
    }
  }
}
