package com.uv.mapa_usuario

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uv.registro.RegistroActivity
import com.uv.registro.ui.theme.SOSTheme


class ConfirmacionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "juand@hotmail.com"

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("current_activity", "ConfirmacionActivity")
            putString("userEmail", userEmail)
            apply()
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })

        setContent {
            SOSTheme {
                window.navigationBarColor = 0xFF90CEFF.toInt()
                MainScreen(userEmail)
            }
        }
    }
}


@Composable
fun MainScreen(userEmail: String) {
    val context = LocalContext.current
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1DA0FF), Color(0xFF90CEFF)),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )
        Column(
            modifier = Modifier.fillMaxSize().background(brush = gradient),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TU SOLICITUD PARA ACCEDER COMO PRESTADOR DE SERVICIO SE ENCUENTRA EN PROCESO",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = "Aprobación",
                modifier = Modifier.size(100.dp).padding(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "EN CUANTO SE VERIFIQUE TU INFORMACIÓN TE LLEGARÁ UNA NOTIFICACIÓN AVISÁNDOTE QUE YA ERES APTO PARA PRESTAR TUS SERVICIOS, MIENTRAS TANTO PUEDES CONTINUAR COMO UN USUARIO",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSecondary ,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = { val intent = Intent(context, MapaUsuarioActivity::class.java).apply {
                    putExtra("userEmail", userEmail)
                }
                    context.startActivity(intent) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Ir a Inicio", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

}


