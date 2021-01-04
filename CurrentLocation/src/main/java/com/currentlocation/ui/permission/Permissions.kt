package com.currentlocation.ui.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.Serializable
import java.util.*

class Permissions {


    companion object{
        var loggingEnabled = true

        /**
         * Disable logs.
         */
        fun disableLogging() {
            loggingEnabled = false
        }

        fun log(message: String?) {
            if (loggingEnabled) Log.d("Permissions", message!!)
        }
        /**
         * Check/Request permissions and call the callback methods of permission handler accordingly.
         *
         * @param context     Android context.
         * @param permissions The array of one or more permission(s) to request.
         * @param rationale   Explanation to be shown to user if s/he has denied permission earlier.
         * If this parameter is null, permissions will be requested without showing
         * the rationale dialog.
         * @param options     The options for handling permissions.
         * @param handler     The permission handler object for handling callbacks of various user
         * actions such as permission granted, permission denied, etc.
         */
        fun check(
            context: Context,
            permissions: Array<String>,
            rationale: String?,
            options: Options?,
            handler: PermissionCallback?
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handler?.onGranted()
            } else {
                val permissionsSet =  LinkedHashSet<String>()
                Collections.addAll(permissionsSet, *permissions)
                var allPermissionProvided = true
                for (aPermission in permissionsSet) {
                    if (context.checkSelfPermission(aPermission) != PackageManager.PERMISSION_GRANTED) {
                        allPermissionProvided = false
                        break
                    }
                }
                if (allPermissionProvided) {
                    handler?.onGranted()
                    PermissionActivity.permissionHandler = null
                } else {
                    PermissionActivity.permissionHandler = handler
                    val permissionsList =
                        ArrayList(permissionsSet)
                    val intent = Intent(context, PermissionActivity::class.java)
                        .putExtra(PermissionActivity.EXTRA_PERMISSIONS, permissionsList)
                        .putExtra(PermissionActivity.EXTRA_RATIONALE, rationale)
                        .putExtra(PermissionActivity.EXTRA_OPTIONS, options)
                    if (options != null && options.createNewTask) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }

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
    }
}