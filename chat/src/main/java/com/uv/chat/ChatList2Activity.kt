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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
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
import kotlinx.coroutines.delay
import com.uv.chat.api.RetrofitClient
import com.uv.chat.api.Chat2
import com.uv.chat.api.ui.theme.SOSTheme
import com.uv.navegacion.navigateToChatPrestador
import com.uv.navegacion.navigateToPerfilPrestador
import com.uv.navegacion.navigateToPrestadorMap
import com.uv.navegacion.navigateToResumen

class ChatList2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userEmail = intent.getStringExtra("userEmail") ?: "default@example.com"
        val idPrestador = intent.getIntExtra("idPrestador", 0)
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
                    ChatList2Screen(userEmail = userEmail, idPrestador = idPrestador) { senderId, nickname ->
                        startActivity(Chat2Activity.newIntent2(this, senderId, idPrestador, nickname))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatList2Screen(userEmail: String, idPrestador: Int, onChatClick: (Int, String) -> Unit) {
    val apiService = RetrofitClient.instance
    val chats = remember { mutableStateListOf<Chat2>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf("Chats") }
    val items = listOf("Mapa", "Solicitudes", "Chats", "Perfil")
    val context = LocalContext.current

    LaunchedEffect(idPrestador) {
        delay(3000) // Retrasar intencionalmente 3 segundos
        try {
            Log.d("ChatList2Screen", "Fetching chats for idPrestador: $idPrestador")
            val chatList = apiService.getChatsPrestador(idPrestador)
            if (chatList.isNullOrEmpty()) {
                errorMessage = "Aun no haz recibido ningun mensaje"
                Log.d("ChatList2Screen", "No chats available for idPrestador: $idPrestador")
            } else {
                chats.addAll(chatList)
                chatList.forEach { chat ->
                    Log.d("ChatList2Screen", "Chat details: sender_id=${chat.sender_id}, receiver_id=${chat.usfoto}, nickname=${chat.nickname}")
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
            Log.e("ChatList2Screen", "Error fetching chats for idPrestador: $idPrestador", e)
        }
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
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (chats.isNotEmpty()) {
                chats.forEach { chat ->
                    ChatListItem2(chat = chat, onClick = {
                        val nickname = chat.nickname ?: "Unknown"
                        Log.d("ChatListActivity", "UserID: $idPrestador, SenderID: ${chat.sender_id}, Nickname: $nickname")
                        onChatClick(chat.sender_id, nickname)
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
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            } else {
                // Mostrar la animación de carga mientras se obtienen los chats
                LoaderAnimation()
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
private fun ChatListItem2(chat: Chat2, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(data = chat.usfoto ?: ""),
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
                text = chat.nickname ?: "Unknown",
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

