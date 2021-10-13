package com.reactnativevoisekappextension.callstate

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import java.util.*

@ReactModule(name = VoisekCallStateExtensionModule.NAME)
class VoisekCallStateExtensionModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), ActivityLifecycleCallbacks,
  VoisekPhoneStateListener.PhoneCallStateUpdate {
  private var wasAppInOffHook = false
  private var wasAppInRinging = false
  private var telephonyManager: TelephonyManager? = null
  private var jsModule: VoisekCallStateUpdateActionModule? = null
  private var callDetectionPhoneStateListener: VoisekPhoneStateListener? = null
  private var activity: Activity? = null
  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun startCallerListener() {
    if (activity == null) {
      activity = currentActivity
      activity!!.application.registerActivityLifecycleCallbacks(this)
    }
    telephonyManager = reactContext.getSystemService(
      Context.TELEPHONY_SERVICE
    ) as TelephonyManager
    callDetectionPhoneStateListener = VoisekPhoneStateListener(this)
    telephonyManager!!.listen(
      callDetectionPhoneStateListener,
      PhoneStateListener.LISTEN_CALL_STATE
    )
  }

  @ReactMethod
  fun stopCallerListener() {
    telephonyManager!!.listen(
      callDetectionPhoneStateListener,
      PhoneStateListener.LISTEN_NONE
    )
    telephonyManager = null
    callDetectionPhoneStateListener = null
  }

  /**
   * @return a map of constants this module exports to JS. Supports JSON types.
   */
  public override fun getConstants(): Map<String, Any>? {
    val map: MutableMap<String, Any> = HashMap()
    map["incoming"] = "incoming"
    map["offhook"] = "offhook"
    map["disconnected"] = "disconnected"
    map["missed"] = "missed"
    return map
  }

  // Activity Lifecycle Methods
  public override fun onActivityCreated(activity: Activity, savedInstanceType: Bundle) {}
  public override fun onActivityStarted(activity: Activity) {}
  public override fun onActivityResumed(activity: Activity) {}
  public override fun onActivityPaused(activity: Activity) {}
  public override fun onActivityStopped(activity: Activity) {}
  public override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
  public override fun onActivityDestroyed(activity: Activity) {}
  public override fun phoneCallStateUpdated(state: Int, phoneNumber: String?) {
    jsModule = reactContext.getJSModule(VoisekCallStateUpdateActionModule::class.java)
    when (state) {
      TelephonyManager.CALL_STATE_IDLE -> {
        if (wasAppInOffHook == true) { // if there was an ongoing call and the call state switches to idle, the call must have gotten disconnected
          jsModule?.callStateUpdated("disconnected", phoneNumber)
        } else if (wasAppInRinging == true) { // if the phone was ringing but there was no actual ongoing call, it must have gotten missed
          jsModule?.callStateUpdated("missed", phoneNumber)
        }

        //reset device state
        wasAppInRinging = false
        wasAppInOffHook = false
      }
      TelephonyManager.CALL_STATE_OFFHOOK -> {
        //Device call state: Off-hook. At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.
        wasAppInOffHook = true
        jsModule?.callStateUpdated("offhook", phoneNumber)
      }
      TelephonyManager.CALL_STATE_RINGING -> {
        // Device call state: Ringing. A new call arrived and is ringing or waiting. In the latter case, another call is already active.
        wasAppInRinging = true
        jsModule?.callStateUpdated("incoming", phoneNumber)
      }
    }
  }

  companion object {
    const val NAME = "VoisekCallStateExtensionAndroid"
  }
}
