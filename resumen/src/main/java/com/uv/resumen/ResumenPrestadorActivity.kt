package com.uv.resumen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.*
import com.uv.resumen.api.RetrofitClient
import com.uv.resumen.api.Service
import com.uv.resumen.api.User
import com.uv.resumen.ui.theme.SOSTheme
import com.uv.navegacion.*
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class ResumenPrestadorActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var servicesWithEstado2 = mutableListOf<Service>()
    private val locationHistory = mutableListOf<Location>()
    private val servicesInProximity = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val userEmail = intent.getStringExtra("userEmail") ?: "ejemplo@email.com"
        val idPrestador = intent.getIntExtra("idPrestador", 6)
        val idServicio = intent.getIntExtra("idServicio", 0).takeIf { it != 0 }

        Log.d("ResumenPrestadorActivity", "Received idPrestador: $idPrestador, idServicio: $idServicio")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })
        setContent {
            SOSTheme {
                MyApp2(userEmail, idPrestador, idServicio, ::updateServicesWithEstado2, servicesInProximity)
            }
        }

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                interval = 2000 // 2 seconds
                fastestInterval = 1000 // 1 second
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        for (location in result.locations) {
                            addLocationToHistory(location)
                            val smoothedLocation = getSmoothedLocation()
                            if (smoothedLocation != null) {
                                Log.d("LocationUpdate", "New smoothed location: Lat=${smoothedLocation.latitude}, Lon=${smoothedLocation.longitude}")
                                checkProximity(smoothedLocation.latitude, smoothedLocation.longitude)
                            }
                        }
                    }
                }, Looper.getMainLooper())
            } catch (e: SecurityException) {
                Log.e("ResumenPrestadorActivity", "SecurityException: ${e.message}")
            }
        }
    }

    private fun addLocationToHistory(location: Location) {
        if (locationHistory.size >= 5) {
            locationHistory.removeAt(0)
        }
        locationHistory.add(location)
    }

    private fun getSmoothedLocation(): Location? {
        if (locationHistory.isEmpty()) return null

        val avgLat = locationHistory.map { it.latitude }.average()
        val avgLon = locationHistory.map { it.longitude }.average()

        return Location("").apply {
            latitude = avgLat
            longitude = avgLon
        }
    }

    private fun checkProximity(currentLat: Double, currentLon: Double) {
        servicesWithEstado2.forEach { service ->
            val destLat = service.lat
            val destLon = service.lon

            Log.d("ProximityCheck", "Current location: Lat=$currentLat, Lon=$currentLon")
            Log.d("ProximityCheck", "Destination location: Lat=$destLat, Lon=$destLon")

            if (isWithinProximity(currentLat, currentLon, destLat, destLon, 3.0)) {
                Log.d("ProximityCheck", "Within proximity of 3 meters for service ID: ${service.idservicio}")
                servicesInProximity.add(service.idservicio)
            } else {
                Log.d("ProximityCheck", "Not within proximity of 3 meters for service ID: ${service.idservicio}")
            }
        }
    }

    private fun isWithinProximity(lat1: Double, lon1: Double, lat2: Double, lon2: Double, radiusMeters: Double): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        Log.d("ProximityCheck", "Distance to destination: ${results[0]} meters")
        return results[0] <= radiusMeters
    }

    private fun isToday(dateString: String?): Boolean {
        if (dateString == null) return false

        val serviceDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        val today = LocalDate.now()
        return serviceDate == today
    }

    private fun updateServicesWithEstado2(services: List<Service>) {
        servicesWithEstado2 = services.filter { it.estado == 2 && isToday(it.fecha) }.toMutableList()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp2(userEmail: String, idPrestador: Int, idServicio: Int?, updateServicesWithEstado2: (List<Service>) -> Unit, servicesInProximity: Set<Int>) {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Solicitudes") }
    val items = listOf("Mapa", "Solicitudes","Chats", "Perfil")
    var selectedOrder by remember { mutableStateOf<Service?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf(emptyList<Service>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idPrestador) {
        // Cargar los servicios del usuario
        val callOrders = RetrofitClient.instance.getServicesByPrestador(idPrestador)
        callOrders.enqueue(object : Callback<List<Service>> {
            override fun onResponse(call: Call<List<Service>>, response: Response<List<Service>>) {
                if (response.isSuccessful) {
                    orders = response.body() ?: emptyList()
                    updateServicesWithEstado2(orders)
                    if (idServicio != null) {
                        selectedOrder = orders.find { it.idservicio == idServicio }
                        showDialog = selectedOrder != null
                    }
                    isLoading = false
                    if (orders.isEmpty()) {
                        errorMessage = "Aun no te han solicitado"
                    }
                } else {
                    Toast.makeText(context, "Error al obtener servicios", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                Toast.makeText(context, "Fallo de conexión al obtener servicios", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

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
    ) { innerPadding ->
        if (isLoading) {
            LoaderAnimation()
        } else {
            if (orders.isEmpty() && errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vacio2),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            } else {
                // Filtrar y ordenar la lista de pedidos
                val filteredOrders = orders
                    .filter { it.estado != 4 }
                    .sortedBy {
                        when (it.estado) {
                            2 -> 0
                            1 -> 1
                            3 -> 2
                            5 -> 3
                            else -> 4 // En caso de que haya estados fuera de rango
                        }
                    }

                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderSummary2(order, servicesInProximity.contains(order.idservicio)) {
                            selectedOrder = order
                            showDialog = true
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (showDialog && selectedOrder != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cerrar")
                            }
                        },
                        title = { Text("Detalles del Pedido") },
                        text = {
                            OrderDetails2(selectedOrder!!, servicesInProximity.contains(selectedOrder!!.idservicio))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummary2(order: Service, isInProximity: Boolean, onClick: () -> Unit) {
    val cardColor = if (order.estado == 5) Color(0xFFFFE5E5) else MaterialTheme.colorScheme.surfaceVariant // Rojo pastel si cancelado

    val fechaCompleta = order.fecha ?: "Desconocido"
    val fecha = if (fechaCompleta != "Desconocido") {
        val parsedDate = LocalDateTime.parse(fechaCompleta, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
        val currentDate = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(currentDate, parsedDate)
        when {
            daysBetween < 0 -> "Expirado"
            daysBetween == 1L -> "Falta un día"
            else -> parsedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale("es", "ES")))
        }
    } else {
        "Desconocido"
    }

    val precio = order.precio?.let {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        numberFormat.currency = Currency.getInstance("COP")
        numberFormat.format(it)
    } ?: "Desconocido"
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
                Text("$precio COP", style = MaterialTheme.typography.titleLarge)
                Text(order.tipo ?: "Desconocido", style = MaterialTheme.typography.titleMedium)
                Text("Para:$fecha", style = MaterialTheme.typography.bodyLarge)
                Text("Para:${order.hora}", style = MaterialTheme.typography.bodySmall)
            }

            if (order.estado == 5) {
                // Mostrar una "X" en lugar del indicador circular
                Icon(Icons.Default.Close, contentDescription = "Cancelado", tint = Color.Red)
            } else {
                // Mostrar el indicador circular según el estado
                CircularProgressIndicator(
                    progress = when (order.estado) {
                        1 -> 0.33f
                        2 -> 0.66f
                        3 -> 1f
                        else -> 0f
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun OrderDetails2(order: Service, isInProximity: Boolean) {
    val multimediaUrl = order.multimedia ?: ""
    val context = LocalContext.current
    var showImageDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp),         horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            mapNumberToService2(order.categoria.toInt() ?: 0),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(order.tipo ?: "Desconocido", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))

        if (multimediaUrl.isNotEmpty()) {
            Image(
                painter = rememberImagePainter(
                    data = order.multimedia,
                    builder = {
                        crossfade(true)
                        allowHardware(false)
                    }
                ),
                contentDescription = "Order Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { showImageDialog = true },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Determinar el estado del pedido en texto
        val estadoText = when (order.estado) {
            1 -> "Servicio: Pendiente"
            2 -> "Servicio: Aceptado"
            3 -> "Servicio: Finalizado"
            4 -> "Servicio: Cancelado"
            else -> "Estado Desconocido"
        }

        val pagoText = when (order.metodo_pago) {
            1 -> "EFECTIVO"
            2 -> "DAVIPLATA"
            3 -> "NEQUI"
            else -> "Desconocido"
        }

        val precioFormat = order.precio?.let {
            NumberFormat.getNumberInstance(Locale.US).format(it)
        } ?: "N/A"

        // Formatear la fecha
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formattedDate = order.fecha?.let {
            try {
                val date = dateFormat.parse(it)
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                "N/A"
            }
        } ?: "N/A"

        // Mostrar el estado del pedido en texto
        Text(estadoText, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Descripción:", style = MaterialTheme.typography.bodyMedium)
        Text(order.descripcion ?: "Descripción no disponible", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Precio: ", style = MaterialTheme.typography.bodyMedium)
            Text("$precioFormat COP", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Fecha: ", style = MaterialTheme.typography.bodyMedium)
            Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Método de pago: ", style = MaterialTheme.typography.bodyMedium)
            Text(pagoText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (order.estado == 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        // Realiza la llamada para aceptar el servicio
                        RetrofitClient.instance.aceptarService(order.idservicio).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Pedido aceptado", Toast.LENGTH_SHORT).show()
                                    restartActivity(context) // Reiniciar la actividad
                                } else {
                                    Toast.makeText(context, "No se pudo aceptar el pedido", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Error de conexión al aceptar", Toast.LENGTH_SHORT).show()
                            }
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1cb333))
                ) {
                    Text("Aceptar Pedido", color = Color.White)
                }
                Button(
                    onClick = {
                        // Realiza la llamada para rechazar el servicio
                        RetrofitClient.instance.rechazarService(order.idservicio).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Pedido rechazado", Toast.LENGTH_SHORT).show()
                                    restartActivity(context) // Reiniciar la actividad
                                } else {
                                    Toast.makeText(context, "No se pudo rechazar el pedido", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Error de conexión al rechazar", Toast.LENGTH_SHORT).show()
                            }
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Rechazar Pedido", color = Color.White)
                }
            }
        }

        if (order.estado == 2) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val intent = Intent(context, Class.forName("com.uv.mapa_usuario.RutaActivity")).apply {
                    putExtra("LAT", order.lat)
                    putExtra("LON", order.lon)
                }
                context.startActivity(intent)
            }) {
                Text("Ver en el Mapa")
            }

            if (isInProximity) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    RetrofitClient.instance.finalizarService(order.idservicio).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "El servicio ha finalizado automáticamente", Toast.LENGTH_SHORT).show()
                                restartActivity(context) // Reiniciar la actividad
                            } else {
                                Toast.makeText(context, "No se pudo finalizar el pedido", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "Error de conexión al finalizar", Toast.LENGTH_SHORT).show()
                        }
                    })
                }) {
                    Text("Finalizar")
                }
            }
        }

        if (order.estado == 3) {
            Text("Este trabajo ha sido finalizado con exito",    textAlign = TextAlign.Center
            )
        }

        if (showImageDialog) {
            Dialog(onDismissRequest = { showImageDialog = false }) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberImagePainter(
                                data = order.multimedia,
                                builder = {
                                    crossfade(true)
                                    allowHardware(false)
                                }
                            ),
                            contentDescription = "Full Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { showImageDialog = false },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 35.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun LoaderAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LaunchedEffect(Unit) {
        delay(3000) // Duración de la animación
    }
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize()
    )
}

private fun restartActivity(context: Context) {
    val intent = (context as Activity).intent
    context.finish()
    context.startActivity(intent)
}

fun mapNumberToService2(categoria: Int): String {
    return when (categoria) {
        1 -> "Cerrajería"
        2 -> "Electricista"
        3 -> "Limpieza"
        4 -> "Pintura"
        else -> "Desconocido"
    }
}
