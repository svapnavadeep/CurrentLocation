package com.currentlocation.ui.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.currentlocation.ui.location.model.LocationData
import com.currentlocation.ui.permission.PermissionHelper
import com.currentlocation.ui.permission.Permissions
import java.io.Serializable
import java.lang.ref.WeakReference

class LocationHelper private constructor(
    private val mActivity: Activity?,
    private val fragment: Fragment?,
    private val option: Permissions.Options?
) {

    private val activityWeakRef: WeakReference<Activity?> = WeakReference(mActivity)
    private val fragmentWeakRef: WeakReference<Fragment?> = WeakReference(fragment)
    fun lastKnownLocation(success: (locationData: LocationData?) -> Unit) {
       // locationCallback.clear()
        locationCallback = object : LocationHelperCallback {
            override fun updateLocation(locationData: LocationData?, flag: Boolean) {
                success(locationData)
            }
        }

        val permissionReceive: (f: Boolean) -> Unit = {
            /*when user gives location permission*/
            if (it) {
                activityWeakRef.get()?.apply {
                    if(locationCallback == null){
                        success(null)
                    }else{
                        this.startActivity(getLocationIntent(this))
                        this.overridePendingTransition(0, 0)
                    }
                }
                fragmentWeakRef.get()?.apply {
                    if(locationCallback == null){
                        success(null)
                    }else{
                        this.startActivity(getLocationIntent(this.requireActivity()))
                        this.requireActivity().overridePendingTransition(0, 0)
                    }
                }
            } else {
                success(null)
            }
        }

        /*Ask for permission*/
        activityWeakRef.get()?.apply {
            PermissionHelper(this).checkPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                option,
                permissionReceive
            )
        }
        /*Ask for permission*/
        fragmentWeakRef.get()?.apply {
            PermissionHelper(this.requireActivity()).checkPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                option,
                permissionReceive
            )
        }
    }

    companion object {
        @JvmStatic
        fun startWith(activity: Activity,option: Permissions.Options?=null) = LocationHelper(activity, null,option)

        @JvmStatic
        fun startWith(fragment: Fragment,option: Permissions.Options?=null) = LocationHelper(null, fragment,option)

        @JvmStatic
        var locationCallback: LocationHelperCallback?= null
    }

    private fun getLocationIntent(context: Context?): Intent {
        return Intent(context, LocationHelperActivity::class.java)
    }

}

