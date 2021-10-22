package com.reactnativevoisekappextension

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.reactnativevoisekappextension.callstate.VoisekCallStateHeadlessTaskInit
import com.reactnativevoisekappextension.callstate.VoisekCallStateHeadlessTaskService
import com.reactnativevoisekappextension.utils.Constants

@ReactModule(name = VoisekAppExtensionModule.NAME)
class VoisekAppExtensionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), PermissionListener, ActivityEventListener {
  private var reactContext: ReactApplicationContext? = reactContext
  private var callbackSuccsessInitCallService: Callback? = null
  private var callbackFailInitCallService: Callback? = null
  private val neededPerms = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE
  )

  override fun initialize() {
    super.initialize()
    reactContext?.addActivityEventListener(this)
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun initCallService(
    requestCallService: Boolean?,
    callbackSuccess: Callback?,
    callbackFail: Callback?
  ) {
    currentActivity.let {
      it
      if (it == null) {
        callbackFail?.invoke("Fail")
      } else {
        callbackSuccsessInitCallService = callbackSuccess
        callbackFailInitCallService = callbackFail
        val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
          Constants.CALLER_OPTIONS_KEY,
          Context.MODE_PRIVATE
        )
        val editorOptions = sharedPreferencesOptions.edit();
        val sharedPreferencesRequestData = reactApplicationContext.getSharedPreferences(
          Constants.REQUEST_DATA_KEY,
          Context.MODE_PRIVATE
        )
        val editorRequestData = sharedPreferencesRequestData.edit()
        if (requestCallService != null) {
          editorRequestData.putBoolean(Constants.OPTION_CAN_REQUEST_ROLE, requestCallService)
          editorOptions.putBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, requestCallService)
        }else{
          editorRequestData.putBoolean(Constants.OPTION_CAN_REQUEST_ROLE, false)
          editorOptions.putBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, false)
        }
        editorOptions.apply()
        editorRequestData.apply()
        val activity: PermissionAwareActivity = currentActivity as PermissionAwareActivity
        activity.requestPermissions(neededPerms, Constants.PHONE_NEEDED_PERM, this)
      }
    }
  }

  @ReactMethod
  fun stopCallService() {
    val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
      Constants.CALLER_OPTIONS_KEY,
      Context.MODE_PRIVATE
    )
    val editorOptions = sharedPreferencesOptions.edit()
    editorOptions.putBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, false)
    editorOptions.putBoolean(Constants.OPTION_BLOCK_CALL_ON_BLACK_LIST, false)
    editorOptions.apply()
  }

  @ReactMethod
  fun doActiveBlockCallOnList(active: Boolean) {
    val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
      Constants.CALLER_OPTIONS_KEY,
      Context.MODE_PRIVATE
    )
    val editorOptions = sharedPreferencesOptions.edit()
    editorOptions.putBoolean(Constants.OPTION_BLOCK_CALL_ON_BLACK_LIST, active)
    editorOptions.apply()
  }

  @ReactMethod
  fun addBlockingPhoneNumbers(callerList: ReadableArray, promise: Promise) {
    try {
      val sharedPreferencesBlockingNumbers = reactApplicationContext.getSharedPreferences(
        Constants.CALLER_BLOCK_KEY,
        Context.MODE_PRIVATE
      )
      val editorBlockingNumbers = sharedPreferencesBlockingNumbers.edit()
      editorBlockingNumbers.clear()
      for (i in 0 until callerList.size()) {
        val caller = callerList.getMap(i)
        if (caller != null && caller.hasKey("category") && caller.hasKey("number")) {
          val callerName = caller.getString("category")
          val callerNumber = caller.getString("number")
          editorBlockingNumbers.putString(callerNumber, callerName)
        }
      }
      editorBlockingNumbers.apply()
      promise.resolve("Did Add data")
    } catch (e: Exception) {
      Log.e("CALLER_ID", e.localizedMessage)
      promise.resolve(e.localizedMessage)
    }
  }

  @ReactMethod
  fun onGoingBackground(){
    Log.d("HJS INIT", "onGoingBackground");
    try {
      val service = Intent(reactApplicationContext, VoisekCallStateHeadlessTaskInit::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        reactApplicationContext.startForegroundService(service)
      }
      else{
        reactApplicationContext.startService(service)
      }
      HeadlessJsTaskService.acquireWakeLockNow(reactApplicationContext);
      Log.d("HJS INIT", "HeadlessJsTaskService");
    } catch (ex: IllegalStateException) {
      // By default, data only messages are "default" priority and cannot trigger Headless tasks
      Log.e("HJS INIT", "ERROR", ex);
    }
  }

  private fun requestRole() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && canRequestRole()) {
      try {
        val roleManager =
          reactApplicationContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        reactApplicationContext.startActivityForResult(
          intent,
          Constants.REQUEST_ID_BECOME_CALL_SCREENER,
          null
        )
      } catch (e: Exception) {
        Log.e("ROLE", e.localizedMessage)
        callbackFailInitCallService?.invoke()
      }
    } else {
      callbackSuccsessInitCallService?.invoke()
    }
  }

  private fun canRequestRole(): Boolean {
    val sharedPreferencesRequestData =
      reactApplicationContext.getSharedPreferences(Constants.REQUEST_DATA_KEY, Context.MODE_PRIVATE)
    return sharedPreferencesRequestData.getBoolean(Constants.OPTION_CAN_REQUEST_ROLE, false)
  }

  override fun onActivityResult(
    activity: Activity,
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == Constants.REQUEST_ID_BECOME_CALL_SCREENER) {
      if (resultCode != 0) {
        callbackSuccsessInitCallService?.invoke()
      } else {
        callbackFailInitCallService?.invoke()
      }
    }
  }
  override fun onNewIntent(intent: Intent) {}

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ): Boolean {
    if (requestCode == Constants.PHONE_NEEDED_PERM) {
      var havePerm = true
      if (grantResults.size == neededPerms.size) {
        for (data in grantResults) {
          if (data != PackageManager.PERMISSION_GRANTED) {
            havePerm = false
            break
          }
        }
      } else {
        havePerm = false
      }
      if (havePerm) {
        val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
          Constants.CALLER_OPTIONS_KEY,
          Context.MODE_PRIVATE
        )
        val editorOptions = sharedPreferencesOptions.edit();
        editorOptions.putBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, true)
        editorOptions.apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          requestRole()
        } else {
          callbackSuccsessInitCallService?.invoke()
        }
      } else {
        callbackFailInitCallService?.invoke()
      }
    }
    return true
  }

  companion object {
    const val NAME = "VoisekAppExtension"
  }

}
