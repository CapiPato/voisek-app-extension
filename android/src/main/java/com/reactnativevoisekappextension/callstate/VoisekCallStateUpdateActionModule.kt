package com.reactnativevoisekappextension.callstate

import com.facebook.react.bridge.JavaScriptModule

interface VoisekCallStateUpdateActionModule : JavaScriptModule {
  fun callStateUpdated(state: String?, phoneNumber: String?)
}
