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
    private val option:Options?
) {

    private val activityWeakRef: WeakReference<Activity?> = WeakReference(mActivity)
    private val fragmentWeakRef: WeakReference<Fragment?> = WeakReference(fragment)
    private val permissionOption:Permissions.Options? = null
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
                Permissions.Options().apply {
                    setSettingLayout(option?.layoutId?:0)
                    setSettingsText(option?.settingsText?:"")
                    setRationaleDialogTitle(option?.rationaleDialogTitle?:"")
                    setSettingsDialogTitle(option?.settingsDialogTitle?:"")
                    setSettingsDialogMessage(option?.settingsDialogMessage?:"")
                    sendDontAskAgainToSettings(option?.sendBlockedToSettings?:false)
                },
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
                Permissions.Options().apply {
                    setSettingLayout(option?.layoutId?:0)
                    setSettingsText(option?.settingsText?:"")
                    setRationaleDialogTitle(option?.rationaleDialogTitle?:"")
                    setSettingsDialogTitle(option?.settingsDialogTitle?:"")
                    setSettingsDialogMessage(option?.settingsDialogMessage?:"")
                    sendDontAskAgainToSettings(option?.sendBlockedToSettings?:false)
                },
                permissionReceive
            )
        }
    }

    companion object {
        @JvmStatic
        fun startWith(activity: Activity,option:Options?=null) = LocationHelper(activity, null,option)

        @JvmStatic
        fun startWith(fragment: Fragment,option:Options?=null) = LocationHelper(null, fragment,option)

        @JvmStatic
        var locationCallback: LocationHelperCallback?= null
    }

    private fun getLocationIntent(context: Context?): Intent {
        return Intent(context, LocationHelperActivity::class.java)
    }

    /**
     * Options to customize while requesting permissions.
     */
    class Options : Serializable {
        var settingsText = "Settings"
        var rationaleDialogTitle = "Permissions Required"
        var settingsDialogTitle = "Permissions Required"
        var settingsDialogMessage = "Required permission(s) have been set" +
                " not to ask again! Please provide them from settings."
        var sendBlockedToSettings = true
        var createNewTask = false
        var layoutId:Int=0

        /**
         * Sets the button text for "settings" while asking user to go to settings.
         *
         * @param settingsText The text for "settings".
         * @return same instance.
         */
        fun setSettingsText(settingsText: String): Options {
            this.settingsText = settingsText
            return this
        }

        /**
         * Sets the "Create new Task" flag in Intent, for when we're
         * calling this library from within a Service or other
         * non-activity context.
         *
         * @param createNewTask true if we need the Intent.FLAG_ACTIVITY_NEW_TASK
         * @return same instance.
         */
        fun setCreateNewTask(createNewTask: Boolean): Options {
            this.createNewTask = createNewTask
            return this
        }

        /**
         * Sets the title text for permission rationale dialog.
         *
         * @param rationaleDialogTitle the title text.
         * @return same instance.
         */
        fun setRationaleDialogTitle(rationaleDialogTitle: String): Options {
            this.rationaleDialogTitle = rationaleDialogTitle
            return this
        }

        /**
         * Sets the title text of the dialog which asks user to go to settings, in the case when
         * permission(s) have been set not to ask again.
         *
         * @param settingsDialogTitle the title text.
         * @return same instance.
         */
        fun setSettingsDialogTitle(settingsDialogTitle: String): Options {
            this.settingsDialogTitle = settingsDialogTitle
            return this
        }

        /**
         * Sets the message of the dialog which asks user to go to settings, in the case when
         * permission(s) have been set not to ask again.
         *
         * @param settingsDialogMessage the dialog message.
         * @return same instance.
         */
        fun setSettingsDialogMessage(settingsDialogMessage: String): Options {
            this.settingsDialogMessage = settingsDialogMessage
            return this
        }

        /**
         * In the case the user has previously set some permissions not to ask again, if this flag
         * is true the user will be prompted to go to settings and provide the permissions otherwise
         * the method [PermissionHandler.onDenied] will be invoked
         * directly. The default state is true.
         *
         * @param send whether to ask user to go to settings or not.
         * @return same instance.
         */
        fun sendDontAskAgainToSettings(send: Boolean): Options {
            sendBlockedToSettings = send
            return this
        }

        fun setSettingLayout(layout:Int){
            layoutId = layout
        }
    }
}

