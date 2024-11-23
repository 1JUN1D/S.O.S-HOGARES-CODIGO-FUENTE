package com.uv.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.auth0.android.Auth0
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.uv.login.ui.theme.SOSTheme
import com.uv.mapa_usuario.MapaUsuarioActivity

import com.auth0.android.result.Credentials
import com.auth0.android.callback.Callback

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.jwt.JWT
import com.uv.mapa_usuario.ConfirmacionActivity
import com.uv.mapa_usuario.UbicacionActivity
import com.uv.registro.RegistroActivity

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val currentActivity = prefs.getString("current_activity", "InicioActivity")
        val userEmail = prefs.getString("userEmail", "Email not available")

        when (currentActivity) {
            "MapaUsuarioActivity" -> {
                val account = Auth0("wE6Wh4QdN9T4H3rf3CcquLuxHnBxLDvX", "dev-s30qk1z7tdznthon.us.auth0.com")
                val authentication = AuthenticationAPIClient(account)
                val storage = SharedPreferencesStorage(this)
                val credentialsManager = CredentialsManager(authentication, storage)

                credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
                    override fun onSuccess(credentials: Credentials) {
                        val intent = Intent(this@InicioActivity, MapaUsuarioActivity::class.java).apply {
                            putExtra("userEmail", userEmail)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }

                    override fun onFailure(exception: CredentialsManagerException) {
                        // Asegura que el código de la UI esté en el hilo principal
                        runOnUiThread {
                            setContent {
                                SOSTheme {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        contentColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        WelcomeScreen()
                                    }
                                }
                            }
                        }
                    }
                })
            }
            "RegistroActivity" -> {
                val intent = Intent(this, RegistroActivity::class.java).apply {
                    putExtra("userEmail", userEmail)
                }
                startActivity(intent)
                finish()
            }
            "UbicacionActivity" -> {
                val intent = Intent(this, UbicacionActivity::class.java).apply {
                    putExtra("userEmail", userEmail)
                }
                startActivity(intent)
                finish()
            }
            "ConfirmacionActivity" -> {
                val intent = Intent(this, ConfirmacionActivity::class.java).apply {
                    putExtra("userEmail", userEmail)
                }
                startActivity(intent)
                finish()
            }
            else -> {
                // Mostrar la interfaz de inicio de sesión
                setContent {
                    SOSTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            contentColor = MaterialTheme.colorScheme.surface
                        ) {
                            WelcomeScreen()
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun WelcomeScreen() {
    val context = LocalContext.current // Obtener el contexto actual
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1DA0FF), Color(0xFF90CEFF)),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.salvavidas),
            contentDescription = "Logo",
            modifier = Modifier
                .size(310.dp) // Ajusta el tamaño según sea necesario
                .padding(bottom = 16.dp) // Añade un padding inferior si necesitas espacio entre la imagen y los botones
        )
        Button(
            onClick = {
                // Crear el Intent y empezar la actividad
                val intent = Intent(context, AutActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.width(300.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(text = "Iniciar Sesión",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) )
        }
        Spacer(modifier = Modifier.height(26.dp))
        Button(
            onClick = { val intent = Intent(context, AutSign::class.java)
                context.startActivity(intent) },
            modifier = Modifier.width(300.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(text ="Registrarse",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) )
        }
        Spacer(modifier = Modifier.height(26.dp))
        Button(
            onClick = { val intent = Intent(context, AuthSignPres::class.java)
                context.startActivity(intent) },
            modifier = Modifier.width(300.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(text ="Registrese como\nPrestador de Servicio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center // Asegura que el texto esté centrado
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SOSTheme {
        WelcomeScreen()
    }
}