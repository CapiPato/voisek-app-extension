package com.reactnativevoisekappextension.callscreenservice

import android.os.Build
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.PermissionAwareActivity
import com.reactnativevoisekappextension.utils.Constants
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.Q)
class VoisekCallScreenService : CallScreeningService() {
  override fun onScreenCall(details: Details) {
    Log.d("CallScreeningService", details.handle.schemeSpecificPart)
    if (isBlockCallsOnListActive) {
      if (details.callDirection == Details.DIRECTION_INCOMING) {
        val phoneNumber = details.handle.schemeSpecificPart
        if (isPhoneOnBlackList(phoneNumber)) {
          respondToCall(details, blockCallResponse())
        }
      }
    }
  }

  private fun blockCallResponse(): CallResponse {
    return CallResponse.Builder().setDisallowCall(true)
      .setRejectCall(true)
      .setSkipNotification(true)
      .setSkipCallLog(false)
      .build()
  }

  private fun isPhoneOnBlackList(phoneNumber: String): Boolean {
    val sharedPreferencesBlockingNumbers = getSharedPreferences(Constants.CALLER_BLOCK_KEY, MODE_PRIVATE)
    if (sharedPreferencesBlockingNumbers.contains(phoneNumber)) {
      return true
    } else {
      val phoneNumberList: List<String> = ArrayList(sharedPreferencesBlockingNumbers.all.keys)
      for (n in phoneNumberList) {
        if (PhoneNumberUtils.compare(this, phoneNumber, n)) {
          return true
        }
      }
    }
    return false
  }

  private val isBlockCallsOnListActive: Boolean
    get() {
      val sharedPreferencesOptions = getSharedPreferences(Constants.CALLER_OPTIONS_KEY, MODE_PRIVATE)
      return sharedPreferencesOptions.getBoolean(Constants.OPTION_BLOCK_CALL_ON_BLACK_LIST, false)
    }
}
