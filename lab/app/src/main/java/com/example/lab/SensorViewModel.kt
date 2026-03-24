package com.example.lab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SensorData(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)
data class LocationData(val latitude: Double = 0.0, val longitude: Double = 0.0)

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)
    private val locationTracker = LocationTracker(application)
    
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    init {
        sensorTracker.setOnSensorValuesChangedListener { x, y, z ->
            _sensorData.value = SensorData(x, y, z)
        }
    }

    fun startSensorListening() {
        sensorTracker.startListening()
    }

    fun stopSensorListening() {
        sensorTracker.stopListening()
    }

    fun startLocationListening() {
        locationTracker.startListening { lat, lng ->
            _locationData.value = LocationData(lat, lng)
        }
    }

    fun stopLocationListening() {
        locationTracker.stopListening()
    }
}
