package com.example.madhusiri.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            // Try last location first
            var location: Location? = Tasks.await(fusedLocationClient.lastLocation)
            
            // If last location is null or too old, request current location
            if (location == null) {
                location = Tasks.await(fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
            }
            return@withContext location
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun getLocality(location: Location): String? = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Try different address components to find the most specific locality
                    return@withContext address.locality 
                        ?: address.subLocality 
                        ?: address.subAdminArea 
                        ?: address.adminArea
                        ?: address.featureName
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun getCurrentVillage(): String? {
        val location = getCurrentLocation()
        return location?.let { getLocality(it) }
    }
}
