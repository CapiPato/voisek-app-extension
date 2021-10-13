package com.reactnativevoisekappextension.callstate

import android.telephony.PhoneStateListener

class VoisekPhoneStateListener(private val callStatCallBack: VoisekCallStateExtensionModule) : PhoneStateListener() {
  override fun onCallStateChanged(state: Int, incomingNumber: String) {
    callStatCallBack.phoneCallStateUpdated(state, incomingNumber)
  }
  interface PhoneCallStateUpdate {
    fun phoneCallStateUpdated(state: Int, incomingNumber: String?)
  }
}
