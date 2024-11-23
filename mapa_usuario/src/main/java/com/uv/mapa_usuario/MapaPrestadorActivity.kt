package com.uv.mapa_usuario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.uv.mapa_usuario.api.DirectionsResponse
import com.uv.mapa_usuario.api.MarkerModel
import com.uv.mapa_usuario.api.RetrofitClient
import com.uv.mapa_usuario.api.Servicio
import com.uv.mapa_usuario.api.User
import com.uv.mapa_usuario.ui.theme.SOSTheme
import com.uv.navegacion.navigateToChatPrestador
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.uv.navegacion.navigateToMap
import com.uv.navegacion.navigateToPerfil
import com.uv.navegacion.navigateToPerfilPrestador
import com.uv.navegacion.navigateToPrestadorMap
import com.uv.navegacion.navigateToResumen
import com.uv.navegacion.navigateToSettings
import kotlinx.coroutines.withContext

class MapaPrestadorActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var isLocationPermissionGranted by mutableStateOf(false)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "juand@hotmail.com"
        val idPrestador = intent.getIntExtra("idPrestador",0)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                isLocationPermissionGranted = isGranted
                if (isGranted) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                }
            }

        setContent {
            SOSTheme {
                checkPermissions()
                Scaffold(
                    bottomBar = { NavigationBarComponent2(userEmail, idPrestador) }  // Cambia a la nueva NavigationBar
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isLocationPermissionGranted) {
                            NavigationHost2(fusedLocationClient, userEmail, idPrestador, innerPadding)
                        } else {
                            RequestPermissionsView2(requestPermissionLauncher)
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
}

@Composable
fun NavigationBarComponent2(userEmail: String, idPrestador:Int) {
    val context = LocalContext.current

    var selectedItem by remember { mutableStateOf("Mapa") }
    val items = listOf("Mapa", "Solicitudes","Chats", "Perfil")
    val icons = mapOf(
        "Mapa" to Icons.Default.LocationOn,
        "Solicitudes" to Icons.Default.Home,
        "Perfil" to Icons.Default.Person,
        "Chats" to Icons.Filled.Send
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(icons[item]!!, contentDescription = null) },
                label = { Text(item) },
                selected = selectedItem == item,
                onClick = {
                    selectedItem = item
                    when (item) {
                        "Mapa" -> navigateToPrestadorMap(context, userEmail, idPrestador)
                        "Solicitudes" -> navigateToResumen(context, userEmail, idPrestador)
                        "Chats" -> navigateToChatPrestador(context, userEmail, idPrestador)

                        "Perfil" -> navigateToPerfilPrestador(context, userEmail, idPrestador)
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationHost2(
    fusedLocationClient: FusedLocationProviderClient,
    userEmail: String, idPrestador: Int,
    innerPadding: PaddingValues
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "Mapa", modifier = Modifier.padding(innerPadding)) {
        composable("Mapa") { MyGoogleMaps2(fusedLocationClient, userEmail, idPrestador) }
        composable("Solicitudes") { Text(text = "Home Screen", style = MaterialTheme.typography.headlineMedium) }
        composable("Perfil") { Text(text = "Profile Screen", style = MaterialTheme.typography.headlineMedium) }
    }
}



@Composable
fun RequestPermissionsView2(requestPermissionLauncher: ActivityResultLauncher<String>) {
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
fun MyGoogleMaps2(fusedLocationClient: FusedLocationProviderClient, userEmail: String, idPrestador: Int) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(0.0, 0.0), 10f, 0f, 0f)
    }

    var activeFilter by remember { mutableStateOf<String?>(null) }
    val allMarkers = remember { mutableStateListOf<Servicio>() }
    val filteredMarkers = remember { mutableStateListOf<Servicio>() }
    var buttonsEnabled by remember { mutableStateOf(true) }

    // Un mapa para mantener estados de marcadores de forma persistente usando idServicio
    val markerStates = remember { mutableMapOf<String, MarkerState>() }

    LaunchedEffect(key1 = idPrestador) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val newPos = CameraPosition(LatLng(location.latitude, location.longitude), 17f, 0f, 0f)
                    cameraPositionState.position = newPos
                }
            }
        }
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.instance.getServicesByPrestador(idPrestador).execute()
                    if (response.isSuccessful) {
                        val services = response.body() ?: emptyList()
                        allMarkers.clear()
                        allMarkers.addAll(services)
                        filteredMarkers.addAll(services)  // Carga inicial de todos los servicios como marcadores
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error al obtener servicios", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fallo de conexión al obtener servicios", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Actualización eficiente de la lista de servicios filtrados
    val currentFilter = activeFilter
    LaunchedEffect(currentFilter) {
        val newFilteredMarkers = if (currentFilter == null) allMarkers else allMarkers.filter { it.tipo == currentFilter }
        filteredMarkers.clear()
        filteredMarkers.addAll(newFilteredMarkers)
    }

    // Preparar o actualizar el estado de cada servicio como marcador
    filteredMarkers.forEach { service ->
        if (!markerStates.containsKey(service.idservicio.toString())) {
            markerStates[service.idservicio.toString()] = rememberMarkerState(position = LatLng(service.lat, service.lon))
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

    val properties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true,mapStyleOptions = mapStyleOptions, minZoomPreference = 13.0f)) }
    val uiSettings by remember { mutableStateOf(MapUiSettings(mapToolbarEnabled =false, myLocationButtonEnabled = true, zoomControlsEnabled = false)) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize(),
            properties = properties,
            uiSettings = uiSettings,
        ) {
            filteredMarkers.forEach { service ->
                val state = markerStates[service.idservicio.toString()]!!
                MarkerInfoWindow(
                    state = state,
                    title = service.tipo,
                    snippet = "Haz clic para más detalles",
                    draggable = false,
                    onInfoWindowClick = {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val origin = "${location.latitude},${location.longitude}"
                                val destination = "${service.lat},${service.lon}"
                                // Suponiendo que RetrofitClient ya está configurado con la API de Google Maps
                                RetrofitClient.googleMapsApiInstance.getDirections(origin, destination, BuildConfig.MAPS_API_KEY).enqueue(object : Callback<DirectionsResponse> {
                                    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                                        Log.d("API Directions", "Response received")
                                        if (response.isSuccessful) {
                                            response.body()?.let { directions ->
                                                val distance = directions.routes.firstOrNull()?.legs?.firstOrNull()?.distance?.text ?: "N/A"
                                                val duration = directions.routes.firstOrNull()?.legs?.firstOrNull()?.duration?.text ?: "N/A"
                                                Log.d("API Directions", "Distance: $distance, Duration: $duration")

                                                // Ahora, muestra un Toast o un Snackbar con la distancia y la duración
                                                Toast.makeText(context, "Distancia: $distance, Tiempo: $duration", Toast.LENGTH_LONG).show()
                                                Log.d("Intent Data", "User Email: $userEmail")

                                                // Lanza PruebaActivity con los datos adicionales
                                                val intent = Intent(context, Class.forName("com.uv.resumen.ResumenPrestadorActivity")).apply {
                                                    putExtra("userEmail", userEmail)
                                                    putExtra("idPrestador", service.idprestador)
                                                    putExtra("idServicio", service.idservicio)  // Añadir idServicio
                                                }
                                                context.startActivity(intent)
                                            } ?: Log.d("API Directions", "No routes found in response")
                                        } else {
                                            Log.d("API Directions", "Failed response: ${response.errorBody()?.string()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                                        Log.e("API Directions", "Error fetching directions", t)

                                        Toast.makeText(context, "Error obteniendo la ruta: ${t.message}", Toast.LENGTH_LONG).show()
                                    }
                                })
                            }
                        }
                    }
                ) {
                    InfoWindowContent2(service)
                }
            }
        }
    }
}



fun getMarkerIcon2(service: String): Int {
    return when (service) {
        "Cerrajeria" -> R.drawable.cerrajeria
        "Electrodomesticos" -> R.drawable.electrodomesticos
        "Jardineria" -> R.drawable.jardineria
        "Limpieza" -> R.drawable.limpieza
        else -> R.drawable.limpieza // Un icono por defecto para servicios no especificados
    }
}



@Composable
fun InfoWindowContent2(service: Servicio) {
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
        Text(text = "Codigo de servicio: ${service.idservicio}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tipo: ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = service.tipo,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center

            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Descripción: ${service.descripcion}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Acción del botón, por ejemplo, abrir un diálogo de llamada */ }
        ) {
            Text("Llamar")
        }
    }
}


sealed class Screen2(val route: String, val icon: ImageVector, val label: String) {
    object Map : Screen2("map", Icons.Default.LocationOn, "Map")
    object Home : Screen2("home", Icons.Default.Home, "Home")
    object Profile : Screen2("profile", Icons.Default.Person, "Profile")
}

