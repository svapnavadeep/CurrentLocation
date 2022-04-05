package com.currentlocation.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.currentlocation.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.currentlocation.ui.location.model.LocationData
import kotlinx.coroutines.*
import java.text.DateFormat
import java.util.*

class LocationHelperActivity : AppCompatActivity() {
    private val mTAG = "BaseLocationActivity"
    private var mLastUpdateTime: String? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mRequestingLocationUpdates = false
    val mLocationListener = LocationHelper.locationCallback

    private val REQUEST_CHECK_SETTINGS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_helper)
        supportActionBar?.hide()
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                Log.d(mTAG, "onLocationResult $locationResult")
                updateLocationUI()
            }
        }
        startLocation()
    }

    private fun startLocation() {
        Log.d(mTAG, "initLocation")

        mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        mSettingsClient = LocationServices.getSettingsClient(this)

        val task = mSettingsClient?.checkLocationSettings(builder.build())
        task?.addOnSuccessListener { locationSettingsResponse ->
            startLocationUpdates()
        }

        task?.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@LocationHelperActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun updateLocationUI() {
        Log.d(mTAG, "updateLocationUI")
        if (mCurrentLocation != null) {
            getLocationAddress(this, mCurrentLocation!!)
            stopLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        Log.d(mTAG, "startLocationUpdates")

        mRequestingLocationUpdates = true
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mLocationRequest?.let { locRequest->
            mLocationCallback?.let { callback->
                mFusedLocationClient?.requestLocationUpdates(
                    locRequest,
                    callback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        Log.d(mTAG, "stopLocationUpdates")
        mLocationCallback?.let { mFusedLocationClient?.removeLocationUpdates(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    startLocationUpdates()
                }
                Activity.RESULT_CANCELED -> {
                    mRequestingLocationUpdates = false
                    setLocation(null, false)
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mRequestingLocationUpdates) {
            stopLocationUpdates()
        }
    }

    private fun getLocationAddress(context: Context, location: Location) {
        var country: String? = ""
        var state: String? = ""
        var fullAddress: String = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geoCoder = Geocoder(context, Locale.US)
                val addresses: List<Address> = geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                state = addresses[0].locality ?: ""
                fullAddress = addresses[0].getAddressLine(0)
                country =  if (addresses[0].countryName == null) {
                    getCountryPhoneCode(context)
                }else{
                    addresses[0].countryName
                }
            } catch (exception: Exception) {
                val countryName = getCountryPhoneCode(context)
                country = countryName
                state = ""
                Log.d("CountryPhoneCode", "Exception ${exception.message}")
            }finally {
                withContext(Dispatchers.Main){
                    setLocation(
                        LocationData(
                            location = location,
                            country = country ?: "",
                            state = state ?: "",
                            fullAddress = fullAddress
                        ), true
                    )
                }
            }
        }
    }

    private fun setLocation(
        locationData: LocationData?,
        b: Boolean
    ) {
        mLocationListener?.updateLocation(locationData, b)
        finish()
        overridePendingTransition(0, 0)
    }


    fun getCountryPhoneCode(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var country = ""
        try {
            country = tm.networkCountryIso
            if (country.equals("in", true)) {
                country = "India"
            }
            Log.d("CountryPhoneCode", "networkCountryIso " + tm.networkCountryIso)
        } catch (e: java.lang.Exception) {
            Log.d("CountryPhoneCode", "Exception " + e.message)
        }
        return country
    }
}