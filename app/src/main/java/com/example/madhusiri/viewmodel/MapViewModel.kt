package com.example.madhusiri.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madhusiri.model.Hive
import com.example.madhusiri.model.SprayAlert
import com.example.madhusiri.repository.FirebaseRepository
import com.example.madhusiri.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _hives = MutableStateFlow<List<Hive>>(emptyList())
    val hives: StateFlow<List<Hive>> = _hives

    private val _sprayAlerts = MutableStateFlow<List<SprayAlert>>(emptyList())
    val sprayAlerts: StateFlow<List<SprayAlert>> = _sprayAlerts

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    init {
        observeHives()
        observeSprayAlerts()
    }

    private fun observeHives() {
        viewModelScope.launch {
            repository.getAllHives().collect {
                _hives.value = it
            }
        }
    }

    private fun observeSprayAlerts() {
        viewModelScope.launch {
            repository.getSprayAlerts().collect {
                _sprayAlerts.value = it
            }
        }
    }

    fun updateCurrentLocation(context: Context) {
        viewModelScope.launch {
            val helper = LocationHelper(context)
            _currentLocation.value = helper.getCurrentLocation()
        }
    }

    fun addHive(name: String, lat: Double, lng: Double, ownerUid: String, ownerName: String) {
        viewModelScope.launch {
            val hive = Hive(
                ownerUid = ownerUid,
                ownerName = ownerName,
                latitude = lat,
                longitude = lng,
                healthStatus = "Healthy",
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveHive(hive)
        }
    }

    fun postSprayAlert(lat: Double, lng: Double, farmerUid: String, farmerName: String) {
        viewModelScope.launch {
            val alert = SprayAlert(
                farmerUid = farmerUid,
                farmerName = farmerName,
                latitude = lat,
                longitude = lng,
                timestamp = System.currentTimeMillis()
            )
            repository.postSprayAlert(alert)
        }
    }

    fun deleteHive(hiveId: String) {
        viewModelScope.launch {
            repository.deleteHive(hiveId)
        }
    }
}
