package com.currentlocation.ui.permission

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.currentlocation.ui.permission.bottom.BottomSheetAlertCLDialog
import com.currentlocaton.R
import java.util.*

@TargetApi(Build.VERSION_CODES.M)
class PermissionActivity : AppCompatActivity() {
    private val RC_SETTINGS = 6739
    private val RC_PERMISSION = 6937

    private var allPermissions = ArrayList<String>()
    private var deniedPermissions = ArrayList<String>()
    private var noRationaleList = ArrayList<String>()
    private var options: Permissions.Options? = null

    companion object{
        val EXTRA_PERMISSIONS = "permissions"
        val EXTRA_RATIONALE = "rationale"
        val EXTRA_OPTIONS = "options"
        var permissionHandler: PermissionCallback? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            finish()
            return
        }
        window.statusBarColor = 0
        initView()
    }

    private fun initView() {
        allPermissions =
            intent.getStringArrayListExtra(EXTRA_PERMISSIONS)?: ArrayList()
        options =
            intent.getSerializableExtra(EXTRA_OPTIONS) as Permissions.Options?
        if (options == null) {
            options = Permissions.Options()
        }
        deniedPermissions = ArrayList()
        noRationaleList = ArrayList()

        var noRationale = true
        for (permission in allPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false
                } else {
                    noRationaleList.add(permission)
                }
            }
        }

        if (deniedPermissions.isEmpty()) {
            grant()
            return
        }

        val rationale = intent.getStringExtra(EXTRA_RATIONALE)
        if (noRationale || TextUtils.isEmpty(rationale)) {
            requestPermissions(deniedPermissions.toTypedArray(), RC_PERMISSION)
        } else {
            showRationale(rationale?:"")
        }
    }

    private fun showRationale(rationale: String) {
        val listener =
            DialogInterface.OnClickListener { dialog, which ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    requestPermissions(
                        toArray(deniedPermissions)!!,
                        RC_PERMISSION
                    )
                } else {
                    deny()
                }
            }
        AlertDialog.Builder(this).setTitle(options!!.rationaleDialogTitle)
            .setMessage(rationale)
            .setPositiveButton(android.R.string.ok, listener)
            .setNegativeButton(android.R.string.cancel, listener)
            .setOnCancelListener { deny() }.create().show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) {
            deny()
        } else {
            deniedPermissions.clear()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]!!)
                }
            }
            if (deniedPermissions.size == 0) {
                grant()
            } else {
                val blockedList =
                    ArrayList<String>() //set not to ask again.
                val justBlockedList =
                    ArrayList<String>() //just set not to ask again.
                val justDeniedList =
                    ArrayList<String>()
                for (permission in deniedPermissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        justDeniedList.add(permission)
                    } else {
                        blockedList.add(permission)
                        if (!noRationaleList.contains(permission)) {
                            justBlockedList.add(permission)
                        }
                    }
                }
                if (justBlockedList.size > 0) { //checked don't ask again for at least one.
                   deny()
                } else if (justDeniedList.size > 0) { //clicked deny for at least one.
                    deny()
                } else { //unavailable permissions were already set not to ask again.
                    if (permissionHandler?.onBlocked(
                            applicationContext,
                            blockedList
                        ) != true
                    ) {
                        sendToSettings()
                    } else finish()
                }
            }
        }
    }

    private fun sendToSettings() {
        if((options?.layoutId?:0) != 0){
            showCustomDialog()
            return
        }
        AlertDialog.Builder(this).setTitle(options!!.settingsDialogTitle)
            .setMessage(options!!.settingsDialogMessage)
            .setPositiveButton(
                options!!.settingsText
            ) { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivityForResult(intent, RC_SETTINGS)
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { dialog, which -> deny() }
            .setOnCancelListener { deny() }.create().show()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SETTINGS && permissionHandler != null) {
            Permissions.check(
                this, allPermissions.toTypedArray(), null, options,
                permissionHandler
            )
        }
        // super, because overridden method will make the handler null, and we don't want that.
        super.finish()
    }

    private fun toArray(arrayList: ArrayList<String>): Array<String?>? {
        val size = arrayList.size
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) {
            array[i] = arrayList[i]
        }
        return array
    }

    override fun finish() {
        permissionHandler = null
        super.finish()
    }

    private fun deny() {
        Log.d("showCustomDialog", "idenyd  "+options?.layoutId)

        if((options?.layoutId?:0) != 0){
            showCustomDialog()
        }else{
            val permissionHandler = permissionHandler
            finish()
            permissionHandler?.onDenied(applicationContext, deniedPermissions)
        }

    }
    private fun onPermissionDeny() {
        val permissionHandler = permissionHandler
        finish()
        permissionHandler?.onDenied(applicationContext, deniedPermissions)
    }

    private var mLocationDialog: BottomSheetAlertCLDialog?=null

    private fun showCustomDialog() {
        if (mLocationDialog==null)
            Log.d("showCustomDialog", "id  "+options?.layoutId)
            mLocationDialog=BottomSheetAlertCLDialog(this,
                options?.layoutId?:0,
                positiveBtnId = options?.dialogPositiveBtn?:0,
                negativeBtnId = options?.dialogNegativeBtn?:0,
                onPositiveClick =  {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    startActivityForResult(intent, RC_SETTINGS)
                }, onNegativeClick = {
                    onPermissionDeny()
                })
        if (mLocationDialog?.isShowing==false) mLocationDialog?.show()
    }

    private fun grant() {
        val permissionHandler = permissionHandler
        finish()
        permissionHandler?.onGranted()
    }
}