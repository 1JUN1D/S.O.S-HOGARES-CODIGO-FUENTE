package com.uv.mapa_usuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uv.mapa_usuario.ui.theme.SOSTheme


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.OnBackPressedCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.uv.mapa_usuario.api.DirectionsResponse
import com.uv.mapa_usuario.api.LocationRequest
import com.uv.mapa_usuario.api.RetrofitClient
import com.uv.mapa_usuario.api.SimpleResponse
import com.uv.mapa_usuario.ui.theme.SOSTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class UbicacionActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isLocationPermissionGranted by mutableStateOf(false)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "juand@hotmail.com"

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("current_activity", "UbicacionActivity")
            putString("userEmail", userEmail)
            apply()
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                isLocationPermissionGranted = isGranted
                if (isGranted) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                }
            }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })

        setContent {
            MyApp {
                checkPermissions()
                if (isLocationPermissionGranted) {
                    MapScreen(fusedLocationClient, userEmail)
                } else {
                    RequestPermissionsView(requestPermissionLauncher)
                }
            }
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionGranted = true
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    SOSTheme {
        content()
    }
}


@Composable
fun MapScreen(fusedLocationClient: FusedLocationProviderClient, userEmail: String) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(0.0, 0.0), 15f, 0f, 0f)
    }
    var markerPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(fusedLocationClient) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val newPos = CameraPosition(LatLng(it.latitude, it.longitude), 15f, 0f, 0f)
                    cameraPositionState.position = newPos
                    markerPosition = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.salvavidas2),
                    contentDescription = "Salvavidas",
                    modifier = Modifier.size(40.dp).padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Selecciona tu ubicación",
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSecondaryContainer),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            )

            // Marker image fixed at the center of the screen
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.marker), // Replace with your marker image resource
                    contentDescription = "Marker",
                    modifier = Modifier.size(40.dp)
                )
            }

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Text("Ya fije mi ubicación")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmación") },
                    text = { Text("¿Estás seguro de que esta es la ubicación que deseas fijar?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            isLoading = true
                            markerPosition = cameraPositionState.position.target

                            val locationRequest = LocationRequest(
                                email = userEmail,
                                lat = markerPosition.latitude,
                                lon = markerPosition.longitude
                            )

                            RetrofitClient.instance.updateLocation(locationRequest).enqueue(object : Callback<SimpleResponse> {
                                override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        Log.d("UbicacionActivity", "Ubicación actualizada: ${response.body()?.message}")
                                        // Navigate to ConfirmacionActivity
                                        val intent = Intent(context, ConfirmacionActivity::class.java).apply {
                                            putExtra("userEmail", userEmail)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Log.e("UbicacionActivity", "Error al actualizar ubicación: ${response.errorBody()?.string()}")
                                    }
                                }

                                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                                    isLoading = false
                                    Log.e("UbicacionActivity", "Error: ${t.message}")
                                }
                            })
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
