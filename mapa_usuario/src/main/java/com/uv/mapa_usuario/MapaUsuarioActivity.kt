package com.uv.mapa_usuario

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.uv.mapa_usuario.api.DirectionsResponse
import com.uv.mapa_usuario.api.MarkerModel
import com.uv.mapa_usuario.api.RetrofitClient
import com.uv.mapa_usuario.ui.theme.SOSTheme
import com.uv.navegacion.navigateToChat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import com.uv.navegacion.navigateToMap
import com.uv.navegacion.navigateToPerfil
import com.uv.navegacion.navigateToSettings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

data class PolylineFeature(
    val properties: Map<String, Any>,
    val points: List<LatLng>
)

class MapaUsuarioActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isLocationPermissionGranted by mutableStateOf(false)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var polylineFeatures: List<PolylineFeature>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "juand@hotmail.com"

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("current_activity", "MapaUsuarioActivity")
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

        // Cargar y parsear el archivo JSON en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            val jsonString = loadJSONFromAsset()
            polylineFeatures = if (jsonString != null) parseGeoJson(jsonString) else emptyList()

            launch(Dispatchers.Main) {
                setContent {
                    SOSTheme {
                        // Hacer la barra de estado transparente
                        window.statusBarColor = android.graphics.Color.TRANSPARENT

                        // Ajustar los flags del sistema para texto oscuro en la barra de estado
                        window.decorView.systemUiVisibility = (
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Para texto oscuro en la barra de estado
                                )
                        checkPermissions()
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            if (isLocationPermissionGranted) {
                                MyGoogleMaps(fusedLocationClient, userEmail, polylineFeatures)
                            } else {
                                RequestPermissionsView(requestPermissionLauncher)
                            }
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
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun loadJSONFromAsset(): String? {
        val json: String?
        try {
            val inputStream = assets.open("polilinea_comunas.geojson")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun parseGeoJson(jsonString: String): List<PolylineFeature> {
        val featuresList = mutableListOf<PolylineFeature>()
        try {
            val jsonObject = JSONObject(jsonString)
            val features = jsonObject.getJSONArray("features")

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.getJSONObject("geometry")
                val properties = feature.getJSONObject("properties")
                val propertiesMap = mutableMapOf<String, Any>()

                properties.keys().forEach { key ->
                    propertiesMap[key] = properties.get(key)
                }

                val type = geometry.getString("type")
                val latLngList = mutableListOf<LatLng>()

                if (type == "MultiLineString") {
                    val coordinates = geometry.getJSONArray("coordinates")
                    for (j in 0 until coordinates.length()) {
                        val lineString = coordinates.getJSONArray(j)
                        for (k in 0 until lineString.length()) {
                            val coord = lineString.getJSONArray(k)
                            val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                            latLngList.add(latLng)
                        }
                    }
                } else if (type == "LineString") {
                    val coordinates = geometry.getJSONArray("coordinates")
                    for (j in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(j)
                        val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                        latLngList.add(latLng)
                    }
                }

                featuresList.add(PolylineFeature(properties = propertiesMap, points = latLngList))
            }
            featuresList.forEach { feature ->
                Log.d("GeoJSONFeature", "Properties: ${feature.properties}, Points: ${feature.points.size}")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return featuresList
    }
}

@Composable
fun RequestPermissionsView(requestPermissionLauncher: ActivityResultLauncher<String>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SE REQUIERE EL PERMISO DE UBICACIÓN PARA MOSTRAR EL MAPA.",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
            Text("Solicitar permiso")
        }
    }
}

@Composable
fun MyGoogleMaps(fusedLocationClient: FusedLocationProviderClient, userEmail: String, polylineFeatures: List<PolylineFeature>) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(0.0, 0.0), 10f, 0f, 0f)
    }

    var activeFilter by remember { mutableStateOf<Int?>(null) }
    val allMarkers = remember { mutableStateListOf<MarkerModel>() }
    val filteredMarkers = remember { mutableStateListOf<MarkerModel>() }
    var buttonsEnabled by remember { mutableStateOf(true) }

    // Un mapa para mantener estados de marcadores de forma persistente usando nickname
    val markerStates = remember { mutableMapOf<String, MarkerState>() }

    LaunchedEffect(key1 = true) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val newPos = CameraPosition(LatLng(location.latitude, location.longitude), 17f, 0f, 0f)
                    cameraPositionState.position = newPos
                    Log.d("MapScreen", "Current Location: Lat=${location.latitude}, Lon=${location.longitude}")
                }
            }
        }
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    val markers = RetrofitClient.instance.getMarkers().execute().body() ?: emptyList()
                    allMarkers.clear()
                    allMarkers.addAll(markers)
                    filteredMarkers.addAll(markers)  // Carga inicial de todos los marcadores

                    Log.d("MapScreen", "Markers loaded: ${markers.size}")
                    markers.forEach { marker ->
                        Log.d("MapScreen", "Marker: Lat=${marker.lat}, Lon=${marker.lon}, Name=${marker.servicio}")
                    }
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error loading markers: ${e.message}")
                }
            }
        }
    }

    // Actualización eficiente de la lista de marcadores filtrados
    val currentFilter = activeFilter
    LaunchedEffect(currentFilter) {
        val newFilteredMarkers = if (currentFilter == null) allMarkers else allMarkers.filter { it.servicio == currentFilter }
        filteredMarkers.clear()
        filteredMarkers.addAll(newFilteredMarkers)
    }

    // Preparar o actualizar el estado de cada marcador
    filteredMarkers.forEach { marker ->
        if (!markerStates.containsKey(marker.idPrestador.toString())) {
            markerStates[marker.idPrestador.toString()] = rememberMarkerState(position = LatLng(marker.lat, marker.lon))
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

    // Crea una instancia de MapStyleOptions utilizando el JSON definido
    val mapStyleOptions = MapStyleOptions(jsonStyle)

    val properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true, mapStyleOptions = mapStyleOptions, minZoomPreference = 13.0f)) }
    val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled = false, myLocationButtonEnabled = true, zoomControlsEnabled = false)) }
    var selectedItem by remember { mutableStateOf("Mapa") }
    val items = listOf("Mapa", "Solicitudes", "Chats", "Perfil")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "Mapa" -> Icon(Icons.Default.LocationOn, contentDescription = null)
                                "Solicitudes" -> Icon(Icons.Filled.Home, contentDescription = null)
                                "Chats" -> Icon(Icons.Filled.Send, contentDescription = null)
                                "Perfil" -> Icon(Icons.Filled.Person, contentDescription = null)
                                else -> Icon(Icons.Filled.Home, contentDescription = null)
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == item,
                        onClick = {
                            selectedItem = item
                            when (item) {
                                "Mapa" -> navigateToMap(context, userEmail)
                                "Solicitudes" -> navigateToSettings(context, userEmail)
                                "Chats" -> navigateToChat(context, userEmail)
                                "Perfil" -> navigateToPerfil(context, userEmail)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GoogleMap(
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                properties = properties,
                uiSettings = uiSettings,
            ) {
                filteredMarkers.forEach { marker ->
                    val state = markerStates[marker.idPrestador.toString()]!!
                    MarkerInfoWindow(
                        state = state,
                        title = marker.nickname,
                        icon = BitmapDescriptorFactory.fromResource(getMarkerIcon(marker.servicio)),
                        snippet = "Haz clic para más detalles",
                        draggable = false,
                        onInfoWindowClick = {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val origin = "${location.latitude},${location.longitude}"
                                    val destination = "${marker.lat},${marker.lon}"

                                    // Obtener el servicio correspondiente del polígono
                                    val servicioNombre = when (marker.servicio) {
                                        1 -> "cerrajero"
                                        2 -> "electricis"
                                        3 -> "limpieza"
                                        4 -> "pintura"
                                        else -> null
                                    }
                                    val servicioValor = polylineFeatures.firstOrNull { feature ->
                                        // Verificar si el marcador está dentro del polígono (se necesita una función contains)
                                        contains(feature.points, LatLng(marker.lat, marker.lon))
                                    }?.properties?.get(servicioNombre) as? String ?: "N/A"

                                    // Suponiendo que RetrofitClient ya está configurado con la API de Google Maps
                                    RetrofitClient.googleMapsApiInstance.getDirections(
                                        origin,
                                        destination,
                                        BuildConfig.MAPS_API_KEY
                                    ).enqueue(object : Callback<DirectionsResponse> {
                                        override fun onResponse(
                                            call: Call<DirectionsResponse>,
                                            response: Response<DirectionsResponse>
                                        ) {
                                            Log.d("API Directions", "Response received")
                                            if (response.isSuccessful) {
                                                response.body()?.let { directions ->
                                                    val distance =
                                                        directions.routes.firstOrNull()?.legs?.firstOrNull()?.distance?.text
                                                            ?: "N/A"
                                                    val duration =
                                                        directions.routes.firstOrNull()?.legs?.firstOrNull()?.duration?.text
                                                            ?: "N/A"
                                                    Log.d(
                                                        "API Directions",
                                                        "Distance: $distance, Duration: $duration"
                                                    )

                                                    // Ahora, muestra un Toast o un Snackbar con la distancia y la duración
                                                    Toast.makeText(
                                                        context,
                                                        "Distancia: $distance, Tiempo: $duration",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    Log.d("Intent Data", "User Email: $userEmail")

                                                    // Lanza PruebaActivity con los datos adicionales
                                                    val intent = Intent(
                                                        context,
                                                        PerfilActivity::class.java
                                                    ).apply {
                                                        putExtra("userEmail", userEmail)
                                                        putExtra("idPrestador", marker.idPrestador)
                                                        putExtra("nickname", marker.nickname)
                                                        putExtra("servicio", marker.servicio)
                                                        putExtra("foto", marker.foto)
                                                        putExtra("lat", marker.lat)
                                                        putExtra("lon", marker.lon)
                                                        putExtra("abre", marker.abre)
                                                        putExtra("cierra", marker.cierra)
                                                        putExtra("calificacion", marker.calificacion)
                                                        putExtra("distance", distance)
                                                        putExtra("duration", duration)
                                                        putExtra("userLat", location.latitude)
                                                        putExtra("userLon", location.longitude)
                                                        putExtra("servicioValor", servicioValor)
                                                    }
                                                    Log.d("Intent Data", "Sending data: servicio=${marker.servicio},  lat=${marker.lat}, lon=${marker.lon},  userLat=${location.latitude}, userLon=${location.longitude}, servicioValor=$servicioValor")

                                                    context.startActivity(intent)
                                                } ?: Log.d(
                                                    "API Directions",
                                                    "No routes found in response"
                                                )
                                            } else {
                                                Log.d(
                                                    "API Directions",
                                                    "Failed response: ${response.errorBody()?.string()}"
                                                )
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<DirectionsResponse>,
                                            t: Throwable
                                        ) {
                                            Log.e("API Directions", "Error fetching directions", t)

                                            Toast.makeText(
                                                context,
                                                "Error obteniendo la ruta: ${t.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    })
                                }
                            }
                        }
                    ) {
                        InfoWindowContent(marker)
                    }
                }
                polylineFeatures.forEach { feature ->
                    Polyline(
                        points = feature.points,
                        color = Color.Black,
                        width = 5f
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row {
                    Button(
                        onClick = {
                            if (buttonsEnabled) {
                                activeFilter = 1
                                buttonsEnabled = false
                            } else {
                                activeFilter = null
                                buttonsEnabled = true
                                filteredMarkers.clear()
                                filteredMarkers.addAll(allMarkers)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!buttonsEnabled && activeFilter == 1) Color(
                                0xFF4CAF50
                            ) else Color.Gray
                        ),
                        enabled = buttonsEnabled || (activeFilter == 1)
                    ) {
                        Text("Cerrajería")
                    }
                    Button(
                        onClick = {
                            if (buttonsEnabled) {
                                activeFilter = 2
                                buttonsEnabled = false
                            } else {
                                activeFilter = null
                                buttonsEnabled = true
                                filteredMarkers.clear()
                                filteredMarkers.addAll(allMarkers)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!buttonsEnabled && activeFilter == 2) Color(
                                0xFF4CAF50
                            ) else Color.Gray
                        ),
                        enabled = buttonsEnabled || (activeFilter == 2)
                    ) {
                        Text("Electricista")
                    }
                }
                Row(modifier = Modifier.padding(bottom = 14.dp)) {
                    Button(
                        onClick = {
                            if (buttonsEnabled) {
                                activeFilter = 4
                                buttonsEnabled = false
                            } else {
                                activeFilter = null
                                buttonsEnabled = true
                                filteredMarkers.clear()
                                filteredMarkers.addAll(allMarkers)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp, end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!buttonsEnabled && activeFilter == 4) Color(
                                0xFF4CAF50
                            ) else Color.Gray
                        ),
                        enabled = buttonsEnabled || (activeFilter == 4)
                    ) {
                        Text("Pintura")
                    }
                    Button(
                        onClick = {
                            if (buttonsEnabled) {
                                activeFilter = 3
                                buttonsEnabled = false
                            } else {
                                activeFilter = null
                                buttonsEnabled = true
                                filteredMarkers.clear()
                                filteredMarkers.addAll(allMarkers)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp, start = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!buttonsEnabled && activeFilter == 3) Color(
                                0xFF4CAF50
                            ) else Color.Gray
                        ),
                        enabled = buttonsEnabled || (activeFilter == 3)
                    ) {
                        Text("Limpieza")
                    }
                }
            }
        }
    }
}

// Función para verificar si un punto está dentro de un polígono utilizando el algoritmo de ray-casting
fun contains(polygon: List<LatLng>, point: LatLng): Boolean {
    var intersectCount = 0
    for (j in polygon.indices) {
        val i = (j + 1) % polygon.size
        val vertex1 = polygon[j]
        val vertex2 = polygon[i]

        if (rayIntersectsSegment(point, vertex1, vertex2)) {
            intersectCount++
        }
    }
    return (intersectCount % 2) == 1
}

fun rayIntersectsSegment(point: LatLng, vertex1: LatLng, vertex2: LatLng): Boolean {
    val px = point.longitude
    val py = point.latitude
    val v1x = vertex1.longitude
    val v1y = vertex1.latitude
    val v2x = vertex2.longitude
    val v2y = vertex2.latitude

    if ((v1y > py) != (v2y > py) && (px < (v2x - v1x) * (py - v1y) / (v2y - v1y) + v1x)) {
        return true
    }
    return false
}





private fun getMarkerIcon(service: Int): Int {
    return when (service) {
        1 -> R.drawable.cerrajeria
        2 -> R.drawable.electrodomesticos
        3 -> R.drawable.limpieza
        4 -> R.drawable.jardineria
        else -> R.drawable.limpieza // Un icono por defecto para servicios no especificados
    }
}

private fun getServiceText(serviceId: Int): String {
    return when (serviceId) {
        1 -> "Cerrajería"
        2 -> "Electricista"
        3 -> "Limpieza"
        4 -> "Pintura"
        else -> "Servicio desconocido"
    }
}

@Composable
private fun InfoWindowContent(marker: MarkerModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .clickable {
                // Acción que deseas realizar cuando se toca el contenido de la ventana de información
                // Por ejemplo, abrir una nueva actividad o mostrar un diálogo.
                val intent = Intent(context, PruebaActivity::class.java)
                context.startActivity(intent)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = marker.nickname, style = MaterialTheme.typography.titleLarge)
        Image(
            painter = rememberImagePainter(
                data = marker.foto,
                builder = {
                    crossfade(true)
                    allowHardware(false)
                }
            ),
            contentDescription = "Imagen de ${marker.nickname}",
            modifier = Modifier
                .size(130.dp, 130.dp)
                .padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Categoría: ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = getServiceText(marker.servicio),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Acción del botón, por ejemplo, abrir un diálogo de llamada */ }
        ) {
            Text("Llamar")
        }
    }
}
