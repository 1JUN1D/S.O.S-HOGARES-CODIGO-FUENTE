package com.uv.mapa_usuario

import android.Manifest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

import com.uv.mapa_usuario.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import com.uv.mapa_usuario.ui.theme.SOSTheme

import android.speech.tts.TextToSpeech

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import com.google.android.gms.location.*
import com.google.maps.android.compose.*

import java.util.*

import androidx.core.text.HtmlCompat

import android.location.Location


class RutaActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isLocationPermissionGranted by mutableStateOf(false)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textToSpeech: TextToSpeech
    private var isTextToSpeechInitialized by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lat = intent.getDoubleExtra("LAT", 0.0)
        val lon = intent.getDoubleExtra("LON", 0.0)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                isLocationPermissionGranted = isGranted
                if (isGranted) {
                    initializeLocationServices()
                }
            }

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
                isTextToSpeechInitialized = true
            }
        }

        setContent {
            checkPermissions()
            SOSTheme {
                Scaffold { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isLocationPermissionGranted) {
                            MapScreen(fusedLocationClient, lat, lon, ::speakDirections)
                        } else {
                            RequestPermissionsView(requestPermissionLauncher)
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val context = this
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionGranted = true
                initializeLocationServices()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun speakDirections(instruction: String) {
        if (isTextToSpeechInitialized) {
            val plainTextInstruction = HtmlCompat.fromHtml(instruction, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            textToSpeech.speak(plainTextInstruction, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TextToSpeech", "TextToSpeech not initialized")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

@Composable
fun MapScreen(
    fusedLocationClient: FusedLocationProviderClient,
    destLat: Double,
    destLon: Double,
    speakDirections: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(destLat, destLon), 15f, 0f, 0f)
    }
    var originLat by remember { mutableStateOf(0.0) }
    var originLon by remember { mutableStateOf(0.0) }
    var showRoute by remember { mutableStateOf(false) }
    val polylinePoints = remember { mutableStateListOf<LatLng>() }
    val navigationInstructions = remember { mutableStateListOf<String>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var lastLocationUpdate by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(key1 = fusedLocationClient) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    originLat = it.latitude
                    originLon = it.longitude
                    cameraPositionState.position = CameraPosition(LatLng(originLat, originLon), 15f, 0f, 0f)
                    showRoute = true
                }
            }
        }
    }

    LaunchedEffect(key1 = showRoute) {
        if (showRoute) {
            val request = LocationRequest.create().apply {
                interval = 5000 // 5 seconds
                fastestInterval = 2000 // 2 seconds
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        lastLocationUpdate = currentLocation
                        updateRouteProgress(
                            currentLocation = currentLocation,
                            polylinePoints = polylinePoints,
                            navigationInstructions = navigationInstructions,
                            currentStepIndex = currentStepIndex,
                            onCurrentStepIndexChanged = { newIndex -> currentStepIndex = newIndex },
                            speakDirections = speakDirections
                        )
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    val jsonStyle = """
        [
          {
            "featureType": "all",
            "elementType": "labels",
            "stylers": [
              {
                "visibility": "off"
              }
            ]
          }
        ]
    """.trimIndent()

    val mapStyleOptions = MapStyleOptions(jsonStyle)

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true, mapStyleOptions = mapStyleOptions),
            uiSettings = MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = true)
        ) {
            Marker(
                state = MarkerState(position = LatLng(destLat, destLon)),
                title = "Destino",
                snippet = "Este es el lugar de destino"
            )
            if (showRoute) {
                LaunchedEffect(key1 = showRoute) {
                    val directions = getDirections(originLat, originLon, destLat, destLon)
                    directions?.let { (points, instructions) ->
                        polylinePoints.clear()
                        polylinePoints.addAll(points)
                        navigationInstructions.clear()
                        navigationInstructions.addAll(instructions)
                    }
                }
                if (polylinePoints.isNotEmpty()) {
                    Polyline(points = polylinePoints, color = Color.Blue, width = 10f)
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showRoute && navigationInstructions.isNotEmpty()) {
                androidx.compose.material3.Button(onClick = { speakDirections(navigationInstructions[currentStepIndex]) }) {
                    androidx.compose.material3.Text("Iniciar viaje")
                }
            }
        }
    }
}

fun updateRouteProgress(
    currentLocation: LatLng,
    polylinePoints: SnapshotStateList<LatLng>,
    navigationInstructions: SnapshotStateList<String>,
    currentStepIndex: Int,
    onCurrentStepIndexChanged: (Int) -> Unit,
    speakDirections: (String) -> Unit
) {
    // Check if the user has passed the current point
    if (currentStepIndex < polylinePoints.size && isPointPassed(currentLocation, polylinePoints[currentStepIndex])) {
        // Move to the next step
        onCurrentStepIndexChanged(currentStepIndex + 1)
        // Speak the next instruction
        if (currentStepIndex < navigationInstructions.size) {
            speakDirections(navigationInstructions[currentStepIndex])
        }
    }
}

fun isPointPassed(currentLocation: LatLng, point: LatLng): Boolean {
    val results = FloatArray(1)
    Location.distanceBetween(
        currentLocation.latitude, currentLocation.longitude,
        point.latitude, point.longitude, results
    )
    return results[0] < 10 // Adjust the threshold as needed
}

suspend fun getDirections(
    originLat: Double,
    originLon: Double,
    destLat: Double,
    destLon: Double
): Pair<List<LatLng>, List<String>>? {
    return withContext(Dispatchers.IO) {
        val directionsService = RetrofitClient.googleMapsApiInstance
        try {
            val response = directionsService.getDirections2(
                "$originLat,$originLon",
                "$destLat,$destLon",
                BuildConfig.MAPS_API_KEY,
                "es-cl"
            ).awaitResponse()

            if (response.isSuccessful) {
                val route = response.body()?.routes?.firstOrNull()
                if (route != null) {
                    Log.d("Directions", "Route found: $route")
                    val points = route.legs.firstOrNull()?.steps?.flatMap { step ->
                        Log.d("Directions", "Step: ${step.polyline.points}")
                        decodePolyline(step.polyline.points)
                    } ?: emptyList()
                    val instructions = route.legs.firstOrNull()?.steps?.map { step ->
                        step.html_instructions
                    } ?: emptyList()
                    Log.d("Directions", "Decoded points: $points")
                    return@withContext Pair(points, instructions)
                } else {
                    Log.d("Directions", "No route found")
                    null
                }
            } else {
                Log.e("Directions", "Response error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Directions", "Exception: ${e.message}")
            null
        }
    }
}

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng((lat / 1E5), (lng / 1E5))
        poly.add(p)
    }
    return poly
}
