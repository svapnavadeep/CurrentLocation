package com.currentlocatonlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import com.currentlocation.ui.location.LocationHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (findViewById<TextView>(R.id.tvLoc))?.setOnClickListener {
            LocationHelper.startWith(this).lastKnownLocation {
                (findViewById<TextView>(R.id.tvAds)).text = it?.fullAddress
            }
        }
    }
}