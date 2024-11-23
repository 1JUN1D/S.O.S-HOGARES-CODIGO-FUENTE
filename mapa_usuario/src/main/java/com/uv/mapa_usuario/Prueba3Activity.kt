package com.uv.mapa_usuario

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.uv.mapa_usuario.api.*
import com.uv.mapa_usuario.ui.theme.SOSTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.airbnb.lottie.compose.*


import android.content.Intent
import android.os.Handler
import android.os.Looper

import androidx.compose.runtime.*

class Prueba3Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener el correo electrónico desde el Intent
        val userEmail = intent.getStringExtra("userEmail") ?: "No hay"
        var userId: Int? = null
        val idPrestador = intent.getIntExtra("idPrestador", 0)
        val serviceCode = intent.getStringExtra("serviceCode") ?: "No hay"
        val servicio = intent.getStringExtra("servicio")?: "Se desconoce"
        val userLat = intent.getDoubleExtra("userLat", 0.0)
        val userLon = intent.getDoubleExtra("userLon", 0.0)
        val describe2 = intent.getStringExtra("describe2") ?: "No description"
        val precioTotal = intent.getIntExtra("precioTotal", 0)
        val selectedDate = intent.getStringExtra("selectedDate") ?: "No date"
        val selectedHour = intent.getStringExtra("selectedHour") ?: "No hour"
        val selectedServices = intent.getStringExtra("selectedServices") ?: "No services"
        val imageUriString = intent.getStringExtra("selectedImageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }
        val tipo_pago = intent.getStringExtra("tipo_pago") ?: "No se especificó"
        Log.d("Prueba3Activity", "userEmail: $userEmail")
        Log.d("Prueba3Activity", "idPrestador: $idPrestador")
        Log.d("Prueba3Activity", "serviceCode: $serviceCode")
        Log.d("Prueba3Activity", "servicio: $servicio")
        Log.d("Prueba3Activity", "userLat: $userLat")
        Log.d("Prueba3Activity", "userLon: $userLon")
        Log.d("Prueba3Activity", "describe2: $describe2")
        Log.d("Prueba3Activity", "precioTotal: $precioTotal")
        Log.d("Prueba3Activity", "selectedDate: $selectedDate")
        Log.d("Prueba3Activity", "selectedHour: $selectedHour")
        Log.d("Prueba3Activity", "selectedServices: $selectedServices")
        Log.d("Prueba3Activity", "imageUri: $imageUri")
        Log.d("Prueba3Activity", "tipo_pago: $tipo_pago")

        // Realizar la llamada a la API para obtener el ID del usuario
        RetrofitClient.instance.getUserByEmail(userEmail).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val userList = response.body()
                    if (!userList.isNullOrEmpty()) {
                        userId = userList[0].id // El ID del primer usuario encontrado
                    }
                }

                // Mostrar la información usando Composables
                setContent {
                    SOSTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            DisplayData(
                                this@Prueba3Activity,
                                userEmail = userEmail,
                                idPrestador = idPrestador,
                                userId = userId,
                                serviceCode = serviceCode,
                                describe2 = describe2,
                                precioTotal = precioTotal,
                                selectedDate = selectedDate,
                                selectedHour = selectedHour,
                                selectedServices = selectedServices,
                                imageUri = imageUri,
                                tipo_pago = tipo_pago,
                                servicio = servicio,
                                userLat = userLat,
                                userLon = userLon
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Manejar el error de la llamada a la API
                Toast.makeText(this@Prueba3Activity, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()

                // Mostrar la información con un ID predeterminado en caso de fallo
                setContent {
                    SOSTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            DisplayData(
                                this@Prueba3Activity,
                                userEmail = userEmail,
                                idPrestador = idPrestador,
                                userId = null, // Sin identificador al fallar la solicitud
                                serviceCode = serviceCode,
                                describe2 = describe2,
                                precioTotal = precioTotal,
                                selectedDate = selectedDate,
                                selectedHour = selectedHour,
                                selectedServices = selectedServices,
                                imageUri = imageUri,
                                tipo_pago = tipo_pago,
                                servicio = servicio,
                                userLat = userLat,
                                userLon = userLon
                            )
                        }
                    }
                }
            }
        })
    }
}

@Composable
fun DisplayData(
    activity: Prueba3Activity,
    userEmail: String,
    idPrestador: Int,
    userId: Int?,
    serviceCode: String,
    describe2: String,
    precioTotal: Int,
    selectedDate: String,
    selectedHour: String,
    selectedServices: String,
    imageUri: Uri?,
    tipo_pago: String,
    servicio: String,
    userLat: Double,
    userLon: Double,
    modifier: Modifier = Modifier
) {
    val serviceNumber = mapServiceToNumber(servicio)
    val pagoNumber = mapPagoToNumber(tipo_pago)
    val formattedPrecioTotal = "%,d COP".format(precioTotal)
    val nonEditableTextFieldModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    var isLoading by remember { mutableStateOf(false) }

    if (isLoading) {
        // Mostrar la animación de carga
        LoaderAnimation()
    } else {
        Column(
            modifier = modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = servicio,
                onValueChange = {},
                label = { Text("Servicio (Número: $serviceNumber)") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = if (describe2.isEmpty()) "Sin descripción" else describe2,
                onValueChange = {},
                label = { Text("Descripción") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = formattedPrecioTotal,
                onValueChange = {},
                label = { Text("Precio Total") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Fecha Seleccionada") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = selectedHour,
                onValueChange = {},
                label = { Text("Hora Seleccionada") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = selectedServices,
                onValueChange = {},
                label = { Text("Servicios Seleccionados") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )
            OutlinedTextField(
                value = tipo_pago,
                onValueChange = {},
                label = { Text("Tipo de Pago") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )

            // Mostrar la imagen si existe
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentScale = ContentScale.Crop
                )
            } ?: OutlinedTextField(
                value = "No se seleccionó ninguna imagen",
                onValueChange = {},
                label = { Text("Imagen") },
                readOnly = true,
                modifier = nonEditableTextFieldModifier
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para enviar los datos
            Button(
                onClick = {
                    isLoading = true
                    val servicioData = Servicio(
                        idservicio = null,
                        idusuario = userId ?: 0,
                        idprestador = idPrestador,
                        categoria = serviceNumber ?: 0,
                        tipo = serviceCode,
                        descripcion = describe2,
                        lat = userLat,
                        lon = userLon,
                        fecha = selectedDate,
                        multimedia = imageUri?.lastPathSegment ?: "",
                        precio = precioTotal.toDouble(),
                        metodo_pago = pagoNumber ,
                        estado = 1,
                        valoracion = null,
                        comentario_val = null,
                        hora = selectedHour
                    )
                    enviarServicio(activity, servicioData, imageUri) {
                        isLoading = false
                        val intent = Intent(activity, Class.forName("com.uv.mapa_usuario.MapaUsuarioActivity"))
                        intent.putExtra("userEmail", userEmail) // Asegúrate de agregar esta línea
                        activity.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Enviar")
            }
        }
    }
}

@Composable
fun LoaderAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize()
    )
}

private fun mapServiceToNumber(servicio: String): Int {
    return when (servicio) {
        "Cerrajería" -> 1
        "Electricista" -> 2
        "Limpieza" -> 3
        "Pintura" -> 4
        else -> 0 // Asigna un valor predeterminado para servicios no reconocidos
    }
}

private fun mapPagoToNumber(tipo_pago: String): Int {
    return when (tipo_pago) {
        "EFECTIVO" -> 1
        "DAVIPLATA" -> 2
        "NEQUI" -> 3
        else -> 99 // Asigna un valor predeterminado para servicios no reconocidos
    }
}

private fun copiarImagenDesdeUri(context: Context, uri: Uri): File? {
    return try {
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null

        // Crear un archivo temporal en el almacenamiento interno de la aplicación
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            val buffer = ByteArray(4 * 1024) // 4 KB
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun enviarServicio(activity: Prueba3Activity, servicio: Servicio, imageUri: Uri?, onComplete: () -> Unit) {
    // Prepara el JSON para enviar
    val servicioJson = Gson().toJson(servicio)
    val requestBodyServicio = RequestBody.create("application/json".toMediaType(), servicioJson)

    // Usa `copiarImagenDesdeUri` para crear un archivo temporal desde el URI
    val tempFile = imageUri?.let { copiarImagenDesdeUri(activity, it) }
    val fotoFile = tempFile?.let { file ->
        MultipartBody.Part.createFormData("foto", file.name, RequestBody.create("image/*".toMediaType(), file))
    } ?: MultipartBody.Part.createFormData("foto", "", RequestBody.create("text/plain".toMediaType(), ""))

    Log.d("ServicioData", "Enviando servicio: $servicioJson")

    // Realiza la solicitud `POST` a la API
    RetrofitClient.instance.subirFotoServicio(requestBodyServicio, fotoFile).enqueue(object : Callback<MyApiResponse> {
        override fun onResponse(call: Call<MyApiResponse>, response: Response<MyApiResponse>) {
            if (response.isSuccessful) {
                val mensaje = response.body()?.message ?: "Operación exitosa"
                Log.d("ServicioData", "Respuesta exitosa: $mensaje")
                // Retrasar la ejecución de onComplete() 3 segundos para mostrar la animación
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
                    onComplete()
                }, 3000)
            } else {
                // Manejar el caso en que la respuesta no sea exitosa
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("API Error", "Error en respuesta: $errorBody")

                // No mostrar Toast de error si no se seleccionó una imagen y la respuesta fue exitosa
                if (imageUri != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Toast.makeText(activity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }, 3000)
                } else {
                    onComplete()
                }
            }
        }

        override fun onFailure(call: Call<MyApiResponse>, t: Throwable) {
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }, 3000)
        }
    })
}

