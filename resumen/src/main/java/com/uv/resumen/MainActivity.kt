package com.uv.resumen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.smarttoolfactory.ratingbar.RatingBar
import com.smarttoolfactory.ratingbar.model.*

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uv.resumen.api.RetrofitClient
import com.uv.resumen.api.Service
import com.uv.resumen.ui.theme.SOSTheme
import coil.compose.rememberImagePainter
import com.uv.navegacion.navigateToChat
import com.uv.navegacion.navigateToMap
import com.uv.navegacion.navigateToPerfil
import com.uv.navegacion.navigateToSettings
import com.uv.resumen.api.ResenaRequest
import com.uv.resumen.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import com.airbnb.lottie.compose.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userEmail = intent.getStringExtra("userEmail") ?: "hola@gamil.com"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })

        setContent {
            SOSTheme {
                MyApp(userEmail)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyApp(userEmail: String) {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Solicitudes") }
    val items = listOf("Mapa", "Solicitudes","Chats", "Perfil")
    var selectedOrder by remember { mutableStateOf<Service?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<Int?>(null) }
    var orders by remember { mutableStateOf(emptyList<Service>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Primero, obtener el ID del usuario
    LaunchedEffect(userEmail) {
        delay(3000)  // Retrasar intencionalmente 3 segundos
        RetrofitClient.instance.getUserByEmail(userEmail).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    userId = response.body()!![0].id

                    // Ahora cargar los servicios
                    val callOrders = RetrofitClient.instance.getServicesByUser(userId!!)
                    callOrders.enqueue(object : Callback<List<Service>> {
                        override fun onResponse(call: Call<List<Service>>, response: Response<List<Service>>) {
                            if (response.isSuccessful) {
                                orders = response.body() ?: emptyList()
                                isLoading = false
                                if (orders.isEmpty()) {
                                    errorMessage = "Aun no haz hecho ninguna solicitud"
                                }
                                Log.d("ServiceList", "Services obtained: $orders")
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Error al obtener servicios", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Fallo de conexión al obtener servicios", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Interfaz de usuario con NavigationBar
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

        Column() {
            if (isLoading) {
                // Mostrar la animación de carga mientras se obtienen los datos
                LoaderAnimation()
            } else if (filteredOrders.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderSummary(order) {
                            selectedOrder = order
                            showDialog = true
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vacio),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 28.sp)
                    )
                }
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
                    OrderDetails(selectedOrder!!) {
                        showDialog = false
                        restartActivity(context)
                    }
                }
            )
        }
    }
}

@Composable
private fun OrderSummary(order: Service, onClick: () -> Unit) {
    val cardColor = if (order.estado == 5) Color(0xFFFFE5E5) else MaterialTheme.colorScheme.surfaceVariant // Rojo pastel si cancelado

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
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(mapNumberToService(order.categoria.toInt() ?: 0), style = MaterialTheme.typography.titleLarge)
                Text(order.tipo ?: "Desconocido", style = MaterialTheme.typography.bodyLarge)
            }

            if (order.estado == 4) {
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
private fun OrderDetails(order: Service, onClose: () -> Unit) {
    val multimediaUrl = order.multimedia ?: ""
    val context = LocalContext.current
    var showImageDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            if (order.estado == 3 && order.valoracion == null) {
                Text("Califique el servicio", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                RatingBar(
                    rating = rating,
                    space = 6.dp,
                    itemCount = 5,
                    itemSize = 38.dp,
                    painterEmpty = painterResource(id = R.drawable.estrella_vacia2),
                    painterFilled = painterResource(id = R.drawable.estrella_llena2),
                    gestureStrategy = GestureStrategy.DragAndPress,
                    ratingInterval = RatingInterval.Half,
                    onRatingChange = { rating = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentarios") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val resenaRequest = ResenaRequest(
                            valoracion = rating.toDouble(),
                            comentario_val = comment,
                            idservicio = order.idservicio
                        )
                        RetrofitClient.instance.actualizarResena(resenaRequest).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Calificación enviada: $rating estrellas. Comentarios: $comment", Toast.LENGTH_SHORT).show()
                                    onClose() // Cerrar el diálogo
                                } else {
                                    Toast.makeText(context, "Error al enviar la calificación", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Error de red al enviar la calificación", Toast.LENGTH_SHORT).show()
                            }
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text("Enviar", modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                Text(
                    mapNumberToService(order.categoria.toInt() ?: 0),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(order.tipo ?: "Desconocido", style = MaterialTheme.typography.bodyMedium,     textAlign = TextAlign.Center
                )
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
                    Spacer(modifier = Modifier.height(8.dp))
                }

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

                // Formatear el precio con separadores de miles
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

                Text(estadoText, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Descripción:", style = MaterialTheme.typography.bodyMedium)
                Text(order.descripcion ?: "Descripción no disponible", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Precio
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Precio: ", style = MaterialTheme.typography.bodyMedium)
                    Text("$precioFormat COP", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Fecha
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Fecha: ", style = MaterialTheme.typography.bodyMedium)
                    Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Método de pago
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Método de pago: ", style = MaterialTheme.typography.bodyMedium)
                    Text(pagoText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Trabajador asignado
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Trabajador asignado: ", style = MaterialTheme.typography.bodyMedium)
                    Text(order.nickname ?: "N/A", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                if (order.estado == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                RetrofitClient.instance.cancelService(order.idservicio).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Pedido cancelado", Toast.LENGTH_SHORT).show()
                                            restartActivity(context)
                                        } else {
                                            Toast.makeText(context, "No se pudo cancelar el pedido", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(context, "Error de conexión al cancelar", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Cancelar Pedido", color = Color.White)
                        }
                    }
                }

                if (order.estado == 2) { Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val intent = Intent(context, Class.forName("com.uv.chat.ChatActivity")).apply {
                            putExtra("USER_ID", order.idusuario)
                            putExtra("RECEIVER_ID", order.idprestador)
                            putExtra("NICKNAME", order.nickname)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Chatear con el prestador")
                    }
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
                                        .padding(top=35.dp)
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
    }
}

@Composable
private fun LoaderAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    painterEmpty: Painter,
    painterFilled: Painter,
    itemSize: Dp = 24.dp,
    space: Dp = 4.dp,
    tintEmpty: Color = Color.Gray,
    tintFilled: Color = Color.Yellow,
    itemCount: Int = 5
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(space),
        modifier = Modifier.wrapContentSize()
    ) {
        for (i in 1..itemCount) {
            val painter = if (i <= rating) painterFilled else painterEmpty
            val tint = if (i <= rating) tintFilled else tintEmpty
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(itemSize)
                    .clickable { onRatingChange(i.toFloat()) }
                    .padding(2.dp),
                colorFilter = ColorFilter.tint(tint)
            )
        }
    }
}

private fun restartActivity(context: Context) {
    val intent = (context as Activity).intent
    context.finish()
    context.startActivity(intent)
}

private fun mapNumberToService(categoria: Int): String {
    return when (categoria) {
        1 -> "Cerrajería"
        2 -> "Electricista"
        3 -> "Limpieza"
        4 -> "Pintura"
        else -> "Desconocido"
    }
}
