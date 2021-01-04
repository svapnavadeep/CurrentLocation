package com.currentlocation.ui.location.model

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

data class LocationData(
    var location: Location? = null,
    var fullAddress: String? = "",
    var name: String? = "",
    var country: String? = "",
    var state: String? = ""
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Location::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(location, flags)
        parcel.writeString(fullAddress)
        parcel.writeString(name)
        parcel.writeString(country)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationData> {
        override fun createFromParcel(parcel: Parcel): LocationData {
            return LocationData(parcel)
        }

        override fun newArray(size: Int): Array<LocationData?> {
            return arrayOfNulls(size)
        }
    }
}

