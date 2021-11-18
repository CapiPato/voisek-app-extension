package com.reactnativevoisekappextension

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.CallScreeningService
import android.text.TextUtils
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.reactnativevoisekappextension.callstate.VoisekCallStateHeadlessTaskService
import com.reactnativevoisekappextension.notification.VoisekNotificationService
import com.reactnativevoisekappextension.utils.Constants
import java.util.*
import kotlin.concurrent.schedule


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
  private var isOnPlay = true;
  private var isOnInit = false;

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
    groupAppName: String?,
    directoryExtensionId: String?,
    requestCallService: Boolean?,
    callbackSuccess: Callback?,
    callbackFail: Callback?
  ) {
    currentActivity.let {
      it
      if (it == null) {
        callbackFail?.invoke("Fail")
      } else {
        isOnInit = true
        mainActivity = it
        callbackSuccessInitCallService = callbackSuccess
        callbackFailInitCallService = callbackFail
        cancelNotifications(0)
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
  fun setNotificationData(
    listeningChannelTitle: String?,
    listeningChannelDesc: String?,
    listeningBackgroundNotTitle: String?,
    listeningBackgroundNotDesc: String?,
    listeningStartNotTitle: String?,
    listeningStartNotDesc: String?,
    listeningEndNotTitle: String?,
    listeningEndNotDesc: String?
  ) {
    val sharedPreferencesNotData = reactApplicationContext.getSharedPreferences(
      Constants.NOT_SHARED_PREF_DATA,
      Context.MODE_PRIVATE
    )
    val editorNotData = sharedPreferencesNotData.edit()

    //Notification Channel Text
    if (listeningChannelTitle != null && !TextUtils.isEmpty(listeningChannelTitle)) {
      editorNotData.putString(Constants.NOT_CHANNEL_NAME_KEY, listeningChannelTitle)
    } else {
      editorNotData.putString(Constants.NOT_CHANNEL_NAME_KEY, Constants.NOT_CHANNEL_NAME)
    }
    if (listeningChannelDesc != null && !TextUtils.isEmpty(listeningChannelDesc)) {
      editorNotData.putString(Constants.NOT_CHANNEL_DESC_KEY, listeningChannelDesc)
    } else {
      editorNotData.putString(Constants.NOT_CHANNEL_DESC_KEY, Constants.NOT_CHANNEL_DESC)
    }

    //Going background Notification Text
    if (listeningBackgroundNotTitle != null && !TextUtils.isEmpty(listeningBackgroundNotTitle)) {
      editorNotData.putString(
        Constants.NOT_NAME_LISTENING_BACKGROUND_KEY,
        listeningBackgroundNotTitle
      )
    } else {
      editorNotData.putString(
        Constants.NOT_NAME_LISTENING_BACKGROUND_KEY,
        Constants.NOT_NAME_LISTENING_BACKGROUND
      )
    }
    if (listeningBackgroundNotDesc != null && !TextUtils.isEmpty(listeningBackgroundNotDesc)) {
      editorNotData.putString(
        Constants.NOT_DESC_LISTENING_BACKGROUND_KEY,
        listeningBackgroundNotDesc
      )
    } else {
      editorNotData.putString(
        Constants.NOT_DESC_LISTENING_BACKGROUND_KEY,
        Constants.NOT_DESC_LISTENING_BACKGROUND
      )
    }

    //Start Listening Notification Text
    if (listeningStartNotTitle != null && !TextUtils.isEmpty(listeningStartNotTitle)) {
      editorNotData.putString(Constants.NOT_NAME_LISTENING_INIT_KEY, listeningStartNotTitle)
    } else {
      editorNotData.putString(
        Constants.NOT_NAME_LISTENING_INIT_KEY,
        Constants.NOT_NAME_LISTENING_INIT
      )
    }
    if (listeningStartNotDesc != null && !TextUtils.isEmpty(listeningStartNotDesc)) {
      editorNotData.putString(Constants.NOT_DESC_LISTENING_INIT_KEY, listeningStartNotDesc)
    } else {
      editorNotData.putString(
        Constants.NOT_DESC_LISTENING_INIT_KEY,
        Constants.NOT_DESC_LISTENING_INIT
      )
    }

    //End Listening Notification Text
    if (listeningEndNotTitle != null && !TextUtils.isEmpty(listeningEndNotTitle)) {
      editorNotData.putString(Constants.NOT_NAME_LISTENING_END_KEY, listeningEndNotTitle)
    } else {
      editorNotData.putString(
        Constants.NOT_NAME_LISTENING_END_KEY,
        Constants.NOT_NAME_LISTENING_END
      )
    }
    if (listeningEndNotDesc != null && !TextUtils.isEmpty(listeningEndNotDesc)) {
      editorNotData.putString(Constants.NOT_DESC_LISTENING_END_KEY, listeningEndNotDesc)
    } else {
      editorNotData.putString(
        Constants.NOT_DESC_LISTENING_END_KEY,
        Constants.NOT_DESC_LISTENING_END
      )
    }

    editorNotData.apply()
  }


  @ReactMethod
  fun stopCallService() {
    val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
      Constants.CALLER_OPTIONS_KEY,
      Context.MODE_PRIVATE
    )
    val stopService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
    stopService.action = Constants.NOT_ACTION_FOREGROUND_SERVICE_STOP;
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
        if (caller != null && caller.hasKey("category") && caller.hasKey("phoneNumber")) {
          val callerName = caller.getString("category")
          val callerNumber = caller.getString("phoneNumber")
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
  fun addSpamPhoneNumbers(callerList: ReadableArray, promise: Promise) {
    try {
      val sharedPreferencesBlockingNumbers = reactApplicationContext.getSharedPreferences(
        Constants.CALLER_SPAM_KEY,
        Context.MODE_PRIVATE
      )
      val editorBlockingNumbers = sharedPreferencesBlockingNumbers.edit()
      editorBlockingNumbers.clear()
      for (i in 0 until callerList.size()) {
        val caller = callerList.getMap(i)
        if (caller != null && caller.hasKey("label") && caller.hasKey("phoneNumber")) {
          val callerNumber = caller.getString("phoneNumber")
          val callerLabel = caller.getString("label")
          editorBlockingNumbers.putString(callerNumber, callerLabel)
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
  fun cancelNotifications(timerForNotToCancel: Int) {
    Timer("CancelNot", false).schedule(timerForNotToCancel.toLong()) {
      val stopService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
      reactApplicationContext.stopService(stopService)
    }
  }

  @ReactMethod
  fun showAFullScreenNotification(title: String, desc: String, timerForNotToShow: Int) {
    Timer("SendingNot", false).schedule(timerForNotToShow.toLong()) {
      val notService = Intent(reactApplicationContext, VoisekNotificationService::class.java)
      notService.action = Constants.NOT_ACTION_FOREGROUND_SHOW_NOT;
      notService.putExtra("title", title);
      notService.putExtra("desc", desc);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        reactApplicationContext.startForegroundService(notService)
      } else {
        reactApplicationContext.startService(notService)
      }
    }
  }

  @ReactMethod
  fun reloadCallExtension(){

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
        isOnInit = false
      }
    } else {
      callbackSuccessInitCallService?.invoke()
      isOnInit = false
    }
  }

  private fun canRequestRole(): Boolean {
    val sharedPreferencesRequestData =
      reactApplicationContext.getSharedPreferences(Constants.REQUEST_DATA_KEY, Context.MODE_PRIVATE)
    return sharedPreferencesRequestData.getBoolean(Constants.OPTION_CAN_REQUEST_ROLE, false)
  }

  private fun isCanCallCheck(): Boolean {
    val sharedPreferencesOptions = reactApplicationContext.getSharedPreferences(
      Constants.CALLER_OPTIONS_KEY,
      CallScreeningService.MODE_PRIVATE
    )
    return sharedPreferencesOptions.getBoolean(Constants.OPTION_CAN_CHECK_CALL_STATE, false)
  }

  private fun keepOnBackground() {
    if (isCanCallCheck()) {
      try {
        val service =
          Intent(reactApplicationContext, VoisekCallStateHeadlessTaskService::class.java)
        val bundle = Bundle()
        bundle.putString("goingBackground", "goingBackground")
        service.putExtras(bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          reactApplicationContext?.startForegroundService(service)
        } else {
          reactApplicationContext?.startService(service)
        }
        HeadlessJsTaskService.acquireWakeLockNow(reactApplicationContext)
        Log.d("Life", "CALL BACKGROUND")
      } catch (ex: IllegalStateException) {
        Log.e("Life", "ERROR", ex)
      }
    }
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
        isOnInit = false
      } else {
        callbackFailInitCallService?.invoke()
        isOnInit = false
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
          isOnInit = false
        }
      } else {
        callbackFailInitCallService?.invoke()
        isOnInit = false
      }
    }
    return true
  }

  companion object {
    const val NAME = "VoisekAppExtension"
  }

  override fun onHostResume() {
    Log.d("Life", "onHostResume")
    isOnPlay = true
  }

  override fun onHostPause() {
    Log.d("Life", "onHostPause")
    if (isOnPlay && !isOnInit) {
      keepOnBackground()
      isOnPlay = false
    }
  }

  override fun onHostDestroy() {
    Log.d("Life", "onHostDestroy")
    if (isOnPlay && !isOnInit) {
      keepOnBackground()
      isOnPlay = false
    }
  }

}
