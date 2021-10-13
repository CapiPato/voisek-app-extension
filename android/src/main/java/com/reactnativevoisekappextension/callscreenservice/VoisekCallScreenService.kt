package com.reactnativevoisekappextension.callscreenservice

import android.os.Build
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils
import androidx.annotation.RequiresApi
import com.reactnativevoisekappextension.utils.Constants
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.Q)
class VoisekCallScreenService : CallScreeningService() {
  override fun onScreenCall(details: Details) {
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
    val sharedPreferences = getSharedPreferences(Constants.CALLER_BLOCK_KEY, MODE_PRIVATE)
    if (sharedPreferences.contains(phoneNumber)) {
      return true
    } else {
      val phoneNumberList: List<String> = ArrayList(sharedPreferences.all.keys)
      for (n in phoneNumberList) {
        if (PhoneNumberUtils.compare(this, phoneNumber, n)) {
          return true
        }
      }
    }
    return false
  }

  val isBlockCallsOnListActive: Boolean
    get() {
      val sharedPreferences = getSharedPreferences(Constants.CALLER_BLOCK_OPTIONS_KEY, MODE_PRIVATE)
      return sharedPreferences.getBoolean("BlockCallsOnList", false)
    }
}
