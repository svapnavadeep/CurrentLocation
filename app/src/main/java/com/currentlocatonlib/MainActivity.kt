package com.currentlocatonlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import com.currentlocation.ui.location.LocationHelper
import com.currentlocation.ui.permission.Permissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById<TextView>(R.id.tvLoc))?.setOnClickListener {
            val option = Permissions.Options().apply {
                setSettingLayout(R.layout.bottomsheet_custom_location)
                setDialogPositiveBtnId(R.id.btnEnableLocation)
                setDialogNegativeBtnId(R.id.imgCross)
            }

            LocationHelper.startWith(this,option).lastKnownLocation {
                (findViewById<TextView>(R.id.tvAds)).text = it?.fullAddress
            }
        }
    }
}