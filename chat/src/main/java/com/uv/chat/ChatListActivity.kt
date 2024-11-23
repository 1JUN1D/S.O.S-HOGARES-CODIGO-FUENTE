package com.uv.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.*

import com.uv.navegacion.navigateToPerfil
import com.uv.navegacion.navigateToSettings
import com.uv.navegacion.navigateToMap
import com.uv.chat.api.RetrofitClient
import com.uv.chat.api.Chat
import com.uv.chat.api.User
import com.uv.chat.api.ui.theme.SOSTheme
import com.uv.navegacion.navigateToChat
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userEmail = intent.getStringExtra("userEmail") ?: "juan94692@gmail.com"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada aquí deshabilita el botón de regreso
            }
        })
        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatListScreen(userEmail = userEmail) { userId, chatId, nickname ->
                        startActivity(ChatActivity.newIntent(this, userId, chatId, nickname))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(userEmail: String, onChatClick: (Int, Int, String) -> Unit) {
    val apiService = RetrofitClient.instance
    var userId by remember { mutableStateOf<Int?>(null) }
    val chats = remember { mutableStateListOf<Chat>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf("Chats") }
    val items = listOf("Mapa", "Solicitudes", "Chats", "Perfil")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cargar el userId desde la API usando el email
    LaunchedEffect(userEmail) {
        delay(3000)  // Retrasar intencionalmente 3 segundos
        apiService.getUserByEmail(userEmail).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                val user = response.body()?.firstOrNull()
                if (user != null) {
                    userId = user.id
                    // Una vez obtenido el userId, cargar la lista de chats desde la API
                    scope.launch {
                        try {
                            val chatList = apiService.getChats(user.id)
                            if (chatList.isEmpty()) {
                                errorMessage = "Aun no haz recibido ningun mensaje"
                            } else {
                                chats.addAll(chatList)
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                } else {
                    errorMessage = "User not found"
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                errorMessage = t.message
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
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (userId == null && errorMessage == null) {
                // Mostrar la animación de carga mientras se obtiene el userId
                LoaderAnimation()
            } else if (userId != null && chats.isNotEmpty()) {
                chats.forEach { chat ->
                    ChatListItem(chat = chat, onClick = {
                        Log.d("ChatListActivity", "UserID: $userId, ReceiverID: ${chat.receiver_id}, Nickname: ${chat.nickname}")
                        onChatClick(userId!!, chat.receiver_id, chat.nickname)
                    })
                    Divider()
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mensaje_vacio),
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
private fun ChatListItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(data = chat.foto),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = chat.nickname,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Mira tu ultimo mensaje...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}


