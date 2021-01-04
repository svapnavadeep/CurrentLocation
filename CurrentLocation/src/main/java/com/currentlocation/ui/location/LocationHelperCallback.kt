package com.currentlocation.ui.location

import com.currentlocation.ui.location.model.LocationData

interface LocationHelperCallback {
    fun updateLocation(locationData: LocationData?, flag: Boolean)

}