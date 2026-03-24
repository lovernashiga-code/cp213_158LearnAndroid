package com.example.lab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class SensorActivity : ComponentActivity() {
    private val viewModel: SensorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sensorData by viewModel.sensorData.collectAsState()
            val locationData by viewModel.locationData.collectAsState()
            val context = LocalContext.current

            var locationPermissionGranted by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                locationPermissionGranted = isGranted
                if (isGranted) {
                    viewModel.startLocationListening()
                } else {
                    Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(Unit) {
                // Check if we already have permission
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                locationPermissionGranted = isGranted

                if (isGranted) {
                    viewModel.startLocationListening()
                }
            }

            DisposableEffect(Unit) {
                viewModel.startSensorListening()
                onDispose {
                    viewModel.stopSensorListening()
                    viewModel.stopLocationListening()
                }
            }

            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Accelerometer Data",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "X: ${sensorData.x}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Y: ${sensorData.y}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Z: ${sensorData.z}", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Location GPS Data",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (locationPermissionGranted) {
                        Text(text = "Latitude: ${locationData.latitude}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Longitude: ${locationData.longitude}", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text(text = "Location Permission Required", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text("Request GPS Permission")
                        }
                    }
                }
            }
        }
    }
}
