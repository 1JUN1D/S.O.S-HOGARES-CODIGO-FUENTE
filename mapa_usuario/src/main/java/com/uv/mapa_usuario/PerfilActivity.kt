package com.uv.mapa_usuario

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.uv.mapa_usuario.api.ApiService
import com.uv.mapa_usuario.api.MarkerModel
import com.uv.mapa_usuario.api.RetrofitClient
import com.uv.mapa_usuario.api.Valoracion
import com.uv.mapa_usuario.ui.theme.SOSTheme
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SOSTheme {
                ProfileScreen()
            }
        }
    }

    @Composable
    fun ProfileScreen() {
        val intent = intent
        val userEmail = intent.getStringExtra("userEmail") ?: "No hay"
        val idPrestador = intent.getIntExtra("idPrestador", 0)
        val nickname = intent.getStringExtra("nickname") ?: "Desconocido"
        val servicio = intent.getIntExtra("servicio", 1)
        val foto = intent.getStringExtra("foto") ?: ""
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val abre = intent.getStringExtra("abre") ?: "Desconocido"
        val cierra = intent.getStringExtra("cierra") ?: "Desconocido"
        val calificacion = intent.getDoubleExtra("calificacion", 0.0)
        val distance = intent.getStringExtra("distance") ?: "Desconocido"
        val servicioValor = intent.getStringExtra("servicioValor")?: "Prom"
        // Crear el objeto MarkerModel
        val markerModel = MarkerModel(idPrestador, nickname, foto, servicio, lat, lon, abre, cierra, calificacion,)

        // Llamar a la función composable que renderiza el perfil
        ProfileContent(markerModel, userEmail, distance, servicioValor)
    }

    @Composable
    fun ProfileContent(marker: MarkerModel, userEmail: String, distance: String, servicioValor: String) {
        val context = LocalContext.current
        val serviceText = getServiceText(marker.servicio)

        var comentarios by remember { mutableStateOf<List<Valoracion>>(emptyList()) }
        var mostrarTodos by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(marker.idPrestador) {
            scope.launch {
                fetchComentarios(marker.idPrestador) { response ->
                    comentarios = response
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Image(
                    painter = rememberImagePainter(marker.foto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(text = marker.nickname, style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(10.dp))

                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary,fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                        append("Servicio: ")
                    }
                    append(serviceText)
                },     style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Abre y Cierra text
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary,fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                        append("Abre: ")
                    }
                    append(marker.abre)
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                        append(" - Cierra: ")
                    }
                    append(marker.cierra)
                },     style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                StarRating2(rating = marker.calificacion)
                Spacer(modifier = Modifier.height(10.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                Text(
                    text = "Comentarios",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (comentarios.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Text(
                            text = "ESTE PRESTADOR DE SERVICIO\n" +
                                    "AUN NO TIENE RESEÑAS",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.comentarios),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

                        )}
                    }
                }
            }

            items(if (mostrarTodos) comentarios else comentarios.take(3)) { comentario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(rating = comentario.valoracion ?: 0.0)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${comentario.valoracion ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = comentario.comentario_val ?: "Sin comentario",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (comentarios.size > 3) {
                item {
                    Button(onClick = { mostrarTodos = !mostrarTodos }) {
                        Text(text = if (mostrarTodos) "Ver menos comentarios" else "Ver más")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val activityClass = getActivityClassForService(marker.servicio)
                        val intent = Intent(context, activityClass).apply {
                            putExtra("userEmail", userEmail)
                            putExtra("distance", distance)
                            putExtra("servicio", getServiceText(marker.servicio))
                            putExtra("userLat", intent.getDoubleExtra("userLat", 0.0))
                            putExtra("userLon", intent.getDoubleExtra("userLon", 0.0))
                            putExtra("idPrestador", marker.idPrestador)
                            putExtra("servicioValor", servicioValor)

                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Pedir Servicio", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    @Composable
    fun StarRating(rating: Double) {
        Row {
            repeat(5) { index ->
                val icon = when {
                    index < rating - 0.5 -> R.drawable.estrella_llena
                    index < rating && index + 0.5 >= rating -> R.drawable.media_estrella
                    else -> R.drawable.estrella_vacia
                }
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = when (icon) {
                        R.drawable.estrella_llena -> Color.Gray
                        R.drawable.media_estrella -> Color.Gray
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    fun StarRating2(rating: Double) {
        Row {
            repeat(5) { index ->
                val icon = when {
                    index + 1 <= rating -> R.drawable.estrella_llena2
                    index + 0.5 < rating -> R.drawable.media_estrella2
                    else -> R.drawable.estrella_vacia2
                }
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = if (icon == R.drawable.estrella_llena2 || icon == R.drawable.media_estrella2) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }


    private fun getActivityClassForService(service: Int): Class<*> {
        return when (service) {
            1 -> PedidoCerrajeroActivity::class.java
            2 -> PedidoElectricistaActivity::class.java
            3 -> PedidoLimpiezaActivity::class.java
            4 -> PedidoPinturaActivity::class.java
            else -> PruebaActivity::class.java // Una actividad por defecto si no hay coincidencias
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

    private fun fetchComentarios(idPrestador: Int, callback: (List<Valoracion>) -> Unit) {
        val call = RetrofitClient.instance.getComentarios(idPrestador)
        call.enqueue(object : Callback<List<Valoracion>> {
            override fun onResponse(call: Call<List<Valoracion>>, response: Response<List<Valoracion>>) {
                if (response.isSuccessful) {
                    callback(response.body() ?: emptyList())
                }
            }

            override fun onFailure(call: Call<List<Valoracion>>, t: Throwable) {
                // Manejar el error
            }
        })
    }
}

