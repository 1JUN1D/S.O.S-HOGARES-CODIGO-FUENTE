package com.uv.usperfil

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.draw.clip
import com.auth0.android.Auth0
import com.uv.usperfil.ui.theme.SOSTheme
import com.uv.login.LogoutManager
import com.uv.navegacion.*
import com.uv.usperfil.api.Prestador
import com.uv.usperfil.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilPrestadorActivity : ComponentActivity() {
    private lateinit var account: Auth0
    private lateinit var logoutManager: LogoutManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PerfilPrestadorActivity", "onCreate called")

        account = Auth0("wE6Wh4QdN9T4H3rf3CcquLuxHnBxLDvX", "dev-s30qk1z7tdznthon.us.auth0.com")
        logoutManager = LogoutManager(this, account)
        sharedPreferences = getSharedPreferences("perfil_prestador_prefs", Context.MODE_PRIVATE)

        // Obtener datos del intent y SharedPreferences
        val userEmail = intent.getStringExtra("userEmail") ?: "default@example.com"
        val nicknameFromIntent = intent.getStringExtra("nickname")
        val idPrestador = intent.getIntExtra("idPrestador", 0)
        var foto = intent.getStringExtra("foto") ?: "vacio"

        // Restaurar nickname de SharedPreferences si no está en el intent
        val nickname = nicknameFromIntent ?: sharedPreferences.getString("nickname", "Usuario") ?: "Usuario"

        // Guardar nickname en SharedPreferences
        if (nicknameFromIntent != null) {
            sharedPreferences.edit().putString("nickname", nicknameFromIntent).apply()
        }

        Log.d("PerfilPrestadorActivity", "userEmail: $userEmail, nickname: $nickname, idPrestador: $idPrestador, foto: $foto")

        // Deshabilitar el botón de regreso del sistema
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
                Log.d("PerfilPrestadorActivity", "Back button pressed")
            }
        })

        setContent {
            var showMenu by remember { mutableStateOf(false) }
            var prestadorFoto by remember { mutableStateOf(foto) }

            LaunchedEffect(foto) {
                if (foto == "vacio") {
                    fetchPrestadorByEmail(userEmail) { prestador ->
                        prestador?.let {
                            prestadorFoto = it.foto
                        }
                    }
                }
            }

            SOSTheme {
                PerfilPrestadorScreen(
                    username = nickname,
                    userEmail = userEmail,
                    idPrestador = idPrestador,
                    fotoUrl = prestadorFoto, // Pasar la URL de la foto actualizada
                    onSwitchToggle = { isChecked ->
                        Log.d("PerfilPrestadorActivity", "Switch toggled: $isChecked")
                        if (isChecked) {
                            checkPrestadorStatus(userEmail)
                        } else {
                            navigateToPerfilUsuario(userEmail, nickname)
                        }
                    },
                    onLogout = {
                        Log.d("PerfilPrestadorActivity", "Logout button clicked")
                        logoutManager.logout()
                    },
                    onFabClick = {
                        showMenu = !showMenu
                    }
                )

                if (showMenu) {
                    EditOptionsMenu(
                        onDismiss = { showMenu = false },
                        onEditPhoto = {
                            showMenu = false
                            navigateToEditarFotoPresActivity(nicknameFromIntent, idPrestador, userEmail)
                        },
                        onEditName = {
                            // Lógica para editar el nombre
                            showMenu = false
                        }
                    )
                }
            }
        }
    }

    private fun checkPrestadorStatus(userEmail: String) {
        Log.d("PerfilPrestadorActivity", "checkPrestadorStatus called with userEmail: $userEmail")
        RetrofitClient.instance.getPrestadorByEmail(userEmail).enqueue(object : Callback<Prestador> {
            override fun onResponse(call: Call<Prestador>, response: Response<Prestador>) {
                Log.d("PerfilPrestadorActivity", "checkPrestadorStatus onResponse: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    val prestador = response.body()
                    if (prestador != null && prestador.verificado) {
                        navigateToPerfilUsuario(userEmail, prestador.nickname)
                    } else {
                        Toast.makeText(this@PerfilPrestadorActivity, "Lo siento, aún tu solicitud como Prestador de servicio no se ha aceptado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@PerfilPrestadorActivity, "Lo siento, aún tu solicitud como Prestador de servicio no se ha aceptado", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Prestador>, t: Throwable) {
                Log.d("PerfilPrestadorActivity", "checkPrestadorStatus onFailure: ${t.message}")
                Toast.makeText(this@PerfilPrestadorActivity, "Error al verificar el estado de prestador", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun navigateToPerfilUsuario(userEmail: String, nickname: String) {
        Log.d("PerfilPrestadorActivity", "navigateToPerfilUsuario called")
        val intent = Intent(this@PerfilPrestadorActivity, PerfilUsuarioActivity::class.java).apply {
            putExtra("userEmail", userEmail)
            putExtra("nickname", nickname)
            putExtra("switchChecked", false)  // Estado del Switch desactivado
        }
        startActivity(intent)
    }

    private fun navigateToEditarFotoPresActivity(nickname: String?, id: Int, userEmail: String?) {
        val intent = Intent(this, EditarFotoPresActivity::class.java).apply {
            putExtra("nickname", nickname)
            putExtra("id", id)
            putExtra("userEmail",userEmail)
        }
        startActivity(intent)
    }

    private fun fetchPrestadorByEmail(email: String, callback: (Prestador?) -> Unit) {
        RetrofitClient.instance.getPrestadorByEmail(email).enqueue(object : Callback<Prestador> {
            override fun onResponse(call: Call<Prestador>, response: Response<Prestador>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Prestador>, t: Throwable) {
                callback(null)
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilPrestadorScreen(
    username: String,
    userEmail: String,
    idPrestador: Int,
    fotoUrl: String,
    onSwitchToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onFabClick: () -> Unit
) {
    val context = LocalContext.current

    var selectedItem by remember { mutableStateOf("Perfil") }
    val items = listOf("Mapa", "Solicitudes", "Chats", "Perfil")

    // Estado inicial del Switch
    var isChecked by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "Orders" -> {
                                    BadgedBox(badge = { Badge { Text("3") } }) {
                                        Icon(Icons.Filled.Home, contentDescription = null)
                                    }
                                }
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mostrar la imagen de perfil
            Image(
                painter = rememberImagePainter(
                    data = fotoUrl,
                    builder = {
                        crossfade(true)
                        error(R.drawable.publicar) // Asegura que una imagen de error se muestre si la URL falla
                        allowHardware(false)
                    }
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(180.dp)
                    .background(Color.Gray, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onSwitchToggle(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Tu correo electrónico es:", style = MaterialTheme.typography.bodyMedium)
            Text(userEmail, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onLogout) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
private fun EditOptionsMenu(onDismiss: () -> Unit, onEditPhoto: () -> Unit, onEditName: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Editar foto",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditPhoto)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Cancelar",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
