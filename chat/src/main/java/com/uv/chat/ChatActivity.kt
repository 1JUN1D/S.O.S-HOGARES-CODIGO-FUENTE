package com.uv.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uv.chat.api.Message
import com.uv.chat.api.RetrofitClient
import com.uv.chat.api.ui.theme.SOSTheme
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatActivity : ComponentActivity() {
    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getIntExtra("USER_ID", 1)
        val receiverId = intent.getIntExtra("RECEIVER_ID", 55)
        val nickname = intent.getStringExtra("NICKNAME") ?: "Chat"

        // Conectar a socket.io
        socket = IO.socket("http://44.215.59.153:3000")
        socket.connect()

        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(userId = userId, receiverId = receiverId, nickname = nickname, socket = socket)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    companion object {
        fun newIntent(context: Context, userId: Int, receiverId: Int, nickname: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("RECEIVER_ID", receiverId)
                putExtra("NICKNAME", nickname)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(userId: Int, receiverId: Int, nickname: String, socket: Socket) {
    val messages = remember { mutableStateListOf<Message>() }
    var newMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = remember { LazyListState() }
    val context = LocalContext.current

    LaunchedEffect(userId, receiverId) {
        try {
            // Obtener mensajes iniciales desde el servidor a través de la API REST
            val apiService = RetrofitClient.instance
            val messageList = apiService.getMessages(userId, receiverId)
            messages.addAll(messageList)
        } catch (e: Exception) {
            errorMessage = e.message
        }

        // Configurar el socket para escuchar mensajes
        socket.on("chat message") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val senderId = data.getInt("sender_id")
                val receiverId = data.getInt("receiver_id")
                val messageText = data.getString("message")
                val status = data.getInt("status")
                val createdAt = data.getString("created_at")

                if (senderId == receiverId || receiverId == userId || senderId == userId) {
                    val message = Message(
                        id = data.getInt("id"),
                        sender_id = senderId,
                        receiver_id = receiverId,
                        message = messageText,
                        created_at = createdAt,
                        status = status
                    )
                    scope.launch {
                        messages.add(message)
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nickname) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState
            ) {
                items(messages) { message ->
                    MessageItem(message = message, isOwnMessage = message.status == 1)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                val iconButtonSize = 48.dp // Definir el tamaño fijo para el IconButton

                BasicTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .height(iconButtonSize) // Asegurar que el BasicTextField tenga la misma altura que el IconButton
                        .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                        .padding(8.dp)
                )
                IconButton(
                    onClick = {
                        val message = Message(
                            sender_id = userId,
                            receiver_id = receiverId,
                            message = newMessage,
                            status = 1
                        )
                        // Enviar el mensaje a través del socket
                        val jsonMessage = JSONObject()
                        jsonMessage.put("sender_id", userId)
                        jsonMessage.put("receiver_id", receiverId)
                        jsonMessage.put("message", newMessage)
                        jsonMessage.put("status", 1)
                        socket.emit("chat message", jsonMessage)

                        scope.launch {
                            try {
                                // Eliminar la lógica de llamada a la API para evitar duplicaciones
                                messages.add(message) // Agregar mensaje al final de la lista
                                newMessage = ""
                                listState.animateScrollToItem(messages.size - 1) // Desplazarse al final de la lista
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(iconButtonSize) // Usar el tamaño fijo definido
                        .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }

            errorMessage?.let {
                Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isOwnMessage: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isOwnMessage) MaterialTheme.colorScheme.primary else Color(0xFFEFEFEF),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.message,
                color = if (isOwnMessage) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
