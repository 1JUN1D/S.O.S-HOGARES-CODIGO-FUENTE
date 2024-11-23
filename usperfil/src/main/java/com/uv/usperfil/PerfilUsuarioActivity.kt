package com.uv.usperfil

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import coil.compose.rememberImagePainter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.auth0.android.Auth0
import com.uv.usperfil.ui.theme.SOSTheme
import com.uv.navegacion.navigateToPerfil
import com.uv.navegacion.navigateToSettings
import com.uv.login.LogoutManager
import com.uv.navegacion.navigateToChat
import com.uv.navegacion.navigateToMap
import com.uv.usperfil.api.Prestador
import com.uv.usperfil.api.RetrofitClient
import com.uv.usperfil.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// PerfilUsuarioActivity.kt
// PerfilUsuarioActivity.kt
class PerfilUsuarioActivity : ComponentActivity() {
    private lateinit var account: Auth0
    private lateinit var logoutManager: LogoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        account = Auth0("wE6Wh4QdN9T4H3rf3CcquLuxHnBxLDvX", "dev-s30qk1z7tdznthon.us.auth0.com")
        logoutManager = LogoutManager(this, account)

        val userEmail = intent.getStringExtra("userEmail") ?: "default@example.com"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })

        setContent {
            var user by remember { mutableStateOf<User?>(null) }
            var showMenu by remember { mutableStateOf(false) }

            // Primer LaunchedEffect para cargar el usuario desde las preferencias o el servidor
            LaunchedEffect(userEmail) {
                val savedUser = getUserFromPreferences(this@PerfilUsuarioActivity)
                if (savedUser != null && savedUser.email == userEmail) {
                    user = savedUser
                }
                // Siempre intenta actualizar desde el servidor
                fetchUserByEmail(userEmail) { fetchedUser ->
                    if (fetchedUser != null) {
                        if (savedUser == null || savedUser.usfoto != fetchedUser.usfoto) {
                            saveUserToPreferences(this@PerfilUsuarioActivity, fetchedUser)
                        }
                    }
                    user = fetchedUser
                }
            }

            // Segundo LaunchedEffect para registrar la respuesta de fetchUserByEmail en Logcat
            LaunchedEffect(userEmail) {
                fetchUserByEmail(userEmail) { fetchedUser ->
                    Log.d("LaunchedEffectLog", "Fetched user in second LaunchedEffect: $fetchedUser")
                }
            }

            SOSTheme {
                user?.let {
                    PerfilScreen(
                        username = it.nickname ?: "Usuario",
                        userEmail = it.email ?: userEmail,
                        usfoto = it.usfoto ?: "",
                        onSwitchToggle = { isChecked ->
                            if (isChecked) {
                                checkPrestadorStatus(userEmail)
                            }
                        },
                        onLogout = {
                            logoutManager.logout()
                        },
                        onFabClick = {
                            showMenu = !showMenu
                        }
                    )

                    // Mostrar el menú de opciones cuando se presiona el FAB
                    if (showMenu) {
                        EditOptionsMenu(
                            onDismiss = { showMenu = false },
                            onEditPhoto = {
                                showMenu = false
                                val intent = Intent(this@PerfilUsuarioActivity, EditarFotoActivity::class.java).apply {
                                    putExtra("email", userEmail)
                                    putExtra("id", it.id)
                                }
                                startActivity(intent)
                            },
                            onEditName = {
                                showMenu = false
                                val intent = Intent(this@PerfilUsuarioActivity, EditarNombreActivity::class.java).apply {
                                    putExtra("userEmail", userEmail)
                                }
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkPrestadorStatus(userEmail: String) {
        RetrofitClient.instance.getPrestadorByEmail(userEmail).enqueue(object : Callback<Prestador> {
            override fun onResponse(call: Call<Prestador>, response: Response<Prestador>) {
                if (response.isSuccessful) {
                    val prestador = response.body()
                    if (prestador != null && prestador.verificado) {
                        val intent = Intent(this@PerfilUsuarioActivity, PerfilPrestadorActivity::class.java).apply {
                            putExtra("userEmail", userEmail)
                            putExtra("nickname", prestador.nickname)
                            putExtra("idPrestador", prestador.id)
                            putExtra("foto", prestador.foto)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@PerfilUsuarioActivity, "Lo siento, aún tu solicitud como Prestador de servicio no se ha aceptado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@PerfilUsuarioActivity, "Lo siento, aún tu solicitud como Prestador de servicio no se ha aceptado", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Prestador>, t: Throwable) {
                Toast.makeText(this@PerfilUsuarioActivity, "Error al verificar el estado de prestador", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchUserByEmail(userEmail: String, callback: (User?) -> Unit) {
        RetrofitClient.instance.getUserByEmail(userEmail).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val user = response.body()?.firstOrNull()
                    Log.d("fetchUserByEmail", "Fetched user: $user")
                    callback(user)
                } else {
                    Log.e("fetchUserByEmail", "Error: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("fetchUserByEmail", "Failure: ${t.message}")
                callback(null)
            }
        })
    }

    fun saveUserToPreferences(context: Context, user: User) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        Log.d("saveUserToPreferences", "Saving user: $user")
        editor.putInt("id", user.id)
        editor.putString("nickname", user.nickname)
        editor.putString("email", user.email)
        editor.putString("usfoto", user.usfoto)  // Guarda la URL correcta
        editor.apply()
    }

    fun getUserFromPreferences(context: Context): User? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getInt("id",-1)
        val nickname = sharedPreferences.getString("nickname", null)
        val email = sharedPreferences.getString("email", null)
        val usfoto = sharedPreferences.getString("usfoto", null)

        val user = if (id != -1 && nickname != null && email != null && usfoto != null) {
            User(id= id ,nickname = nickname, email = email, usfoto = usfoto)
        } else {
            null
        }
        Log.d("getUserFromPreferences", "Loaded user: $user")
        return user
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    username: String,
    userEmail: String,
    usfoto: String,
    onSwitchToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onFabClick: () -> Unit
) {
    val context = LocalContext.current

    // Verifica la URL de usfoto
    Log.d("PerfilScreen", "URL de la foto de perfil: $usfoto")

    var selectedItem by remember { mutableStateOf("Perfil") }
    val items = listOf("Mapa", "Solicitudes", "Chats", "Perfil")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(
                    data = usfoto,
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
                checked = false,
                onCheckedChange = onSwitchToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
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
                text = "Editar nombre",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditName)
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