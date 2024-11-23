package com.uv.usperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uv.usperfil.ui.theme.SOSTheme
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

import android.util.Log
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.uv.usperfil.api.RetrofitClient
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.delay

// EditarNombreActivity.kt
class EditarNombreActivity : ComponentActivity() {
    private val showLottieAnimation = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userEmail = intent.getStringExtra("userEmail") ?: ""

        // Agregar log para mostrar userEmail
        Log.d("EditarNombreActivity", "Received userEmail: $userEmail")

        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditarNombreScreen(
                        userEmail = userEmail,
                        onSubmitName = { newName -> updateNickname(userEmail, newName) },
                        showLottieAnimation = showLottieAnimation
                    )
                }
            }
        }
    }

    private fun updateNickname(userEmail: String, newName: String) {
        showLottieAnimation.value = true

        val call = RetrofitClient.instance.updateNicknameByEmail(userEmail, newName)

        lifecycleScope.launch {
            val result = suspendCoroutine<Response<Void>> { cont ->
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        cont.resume(response)
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        cont.resumeWithException(t)
                    }
                })
            }

            if (result.isSuccessful) {
                Toast.makeText(this@EditarNombreActivity, "Nombre actualizado con éxito", Toast.LENGTH_SHORT).show()
                delay(3000) // Mostrar animación Lottie durante 3 segundos
                finish() // Cerrar actividad después de la animación
            } else {
                showLottieAnimation.value = false
                Toast.makeText(this@EditarNombreActivity, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun EditarNombreScreen(
    userEmail: String,
    onSubmitName: (String) -> Unit,
    showLottieAnimation: MutableState<Boolean>
) {
    var newName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editar Nombre",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = newName,
            onValueChange = { newName = it },
            placeholder = { Text("Digite su nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSubmitName(newName) },
            enabled = newName.isNotEmpty()
        ) {
            Text("Enviar")
        }

        if (showLottieAnimation.value) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
