package com.uv.mapa_usuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uv.mapa_usuario.ui.theme.SOSTheme

class PruebaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail")?: "No hay"
        val idPrestador =  intent.getIntExtra("idPrestador",0)
        val nickname = intent.getStringExtra("nickname") ?: "No Name"
        val servicio = intent.getStringExtra("servicio") ?: "No Service"
        val foto = intent.getStringExtra("foto")
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val distance = intent.getStringExtra("distance") ?: "Desconocido"
        val duration = intent.getStringExtra("duration") ?: "Desconocido"
        val userLat = intent.getDoubleExtra("userLat", 0.0)
        val userLon = intent.getDoubleExtra("userLon", 0.0)

        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarkerDetails(userEmail,idPrestador,nickname, servicio, foto, lat, lon, distance, duration, userLat, userLon)
                }
            }
        }
    }
}


@Composable
fun MarkerDetails(userEmail: String,idPrestador: Int,nickname: String, servicio: String, foto: String?, lat: Double, lon: Double, distance: String, duration: String, userLat: Double, userLon: Double) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Usuario $userEmail", style = MaterialTheme.typography.titleMedium)
        Text(text = "Id prestador: $idPrestador", style = MaterialTheme.typography.titleMedium)
        Text(text = "Nickname: $nickname", style = MaterialTheme.typography.titleMedium)
        Text(text = "Servicio: $servicio", style = MaterialTheme.typography.titleMedium)
        Text(text = "Latitude: $lat, Longitude: $lon", style = MaterialTheme.typography.titleMedium)
        Text(text = "Distancia: $distance", style = MaterialTheme.typography.titleMedium)
        Text(text = "Duración: $duration", style = MaterialTheme.typography.titleMedium)
        Text(text = "Tu Latitud: $userLat, Tu Longitud: $userLon", style = MaterialTheme.typography.titleMedium)
        // Aquí podrías cargar la imagen usando Coil si lo deseas, mostrando `foto`.
    }
}

