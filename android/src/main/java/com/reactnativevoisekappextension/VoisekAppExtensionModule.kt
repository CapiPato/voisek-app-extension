package com.reactnativevoisekappextension

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.reactnativevoisekappextension.notification.VoisekNotification
import com.reactnativevoisekappextension.notification.VoisekNotificationService
import com.reactnativevoisekappextension.utils.Constants


@ReactModule(name = VoisekAppExtensionModule.NAME)
class VoisekAppExtensionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), PermissionListener, ActivityEventListener,
  LifecycleEventListener {
  private lateinit var mainActivity: Activity
  private var callbackSuccessInitCallService: Callback? = null
  private var callbackFailInitCallService: Callback? = null
  private val neededPerms = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_CALL_LOG,
    Manifest.permission.READ_PHONE_STATE
  )

  override fun initialize() {
    super.initialize()
    currentActivity.let {
      it
      if (it != null) {
        mainActivity = it
      }
    }
    reactApplicationContext?.addActivityEventListener(this)
    reactApplicationContext?.addLifecycleEventListener(this);
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
        mainActivity = it
        callbackSuccessInitCallService = callbackSuccess
        callbackFailInitCallService = callbackFail
        cancelNotifications()
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
        } else {
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
    val stopService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
    stopService.action = Constants.VOISEK_ACTION_FOREGROUND_SERVICE_STOP;
    reactApplicationContext.startService(stopService)
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
  fun cancelNotifications() {
    val stopService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
    reactApplicationContext.stopService(stopService)
  }

  @ReactMethod
  fun showAFullScreenNotification(title: String, desc: String) {
    val notService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
    notService.action = Constants.VOISEK_ACTION_FOREGROUND_SHOW_NOT;
    notService.putExtra("title", title);
    notService.putExtra("desc", desc);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      reactApplicationContext.startForegroundService(notService)
    } else {
      reactApplicationContext.startService(notService)
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
      callbackSuccessInitCallService?.invoke()
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
        callbackSuccessInitCallService?.invoke()
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
          callbackSuccessInitCallService?.invoke()
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

  override fun onHostResume() {
    Log.d("Life", "onHostResume")
  }

  override fun onHostPause() {
    Log.d("Life", "onHostPause")
  }

  override fun onHostDestroy() {
    Log.d("Life", "onHostDestroy")
  }

}
