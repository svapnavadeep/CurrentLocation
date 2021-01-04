package com.currentlocation.ui.permission

import android.app.Activity
import android.content.Context
import android.util.Log
import java.util.ArrayList

class PermissionHelper(private val mActivity: Activity) {

    val mTAG = "checkPermissions"

    fun checkPermissions(permissions: Array<String>, success: (isAllow: Boolean) -> Unit = {}) {
        val permissionHandler = object : PermissionCallback() {
            override fun onGranted() {
                Log.d(mTAG, "permissionHandler onGranted")
                success(true)
            }

            override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>) {
                super.onDenied(context, deniedPermissions)
                Log.d(mTAG, "permissionHandler onDenied")
                success(false)
            }

            override fun onJustBlocked(
                context: Context?,
                justBlockedList: ArrayList<String>,
                deniedPermissions: ArrayList<String>
            ) {
                super.onJustBlocked(context, justBlockedList, deniedPermissions)
                Log.d(mTAG, "permissionHandler onJustBlocked")
                success(false)
            }
        }

        Permissions.check(
            mActivity,
            permissions,
            null,
            null,
            permissionHandler
        )

    }
}