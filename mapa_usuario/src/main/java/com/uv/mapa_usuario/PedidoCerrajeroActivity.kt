package com.uv.mapa_usuario

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.uv.mapa_usuario.ui.theme.SOSTheme
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PedidoCerrajeroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "No hay"
        val distance = intent.getStringExtra("distance") ?: "10"
        val servicio = intent.getStringExtra("servicio") ?: "Se desconoce"
        val userLat = intent.getDoubleExtra("userLat", 0.0)
        val userLon = intent.getDoubleExtra("userLon", 0.0)
        val idPrestador = intent.getIntExtra("idPrestador", 0)
        val defPrecio = intent.getStringExtra("servicioValor") ?: "prom"
        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Cerrajero (navController, distance, idPrestador, userEmail, servicio, userLat, userLon, defPrecio)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cerrajero(
    navController: NavController,
    distance: String,
    idPrestador: Int,
    userEmail: String,
    servicio: String,
    userLat: Double,
    userLon: Double,
    defPrecio: String
) {
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedHour by remember { mutableStateOf<String?>(null) }
    var precioTotal by remember { mutableStateOf(0) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var describe2 by remember { mutableStateOf("") }

    val precioInstalacionMax = 6351
    val precioInstalacionMin = 5349
    val precioProblemasMax = 77810
    val precioProblemasMin = 61790
    val extraWeekendPrice = 15000
    val extraNightPrice = 20000

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = photoUri
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val photoFile = createImageFile(context)
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            photoUri?.let {
                cameraLauncher.launch(it)
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Text(
            text = "SERVICIOS DE CERRAJERIA",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 24.sp),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 10.dp, bottom = 15.dp)
        )

        // Botones para servicios
        var installChecked by remember { mutableStateOf(false) }
        var fixChecked by remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ServiceButton(
                text = "Duplicado de llaves",
                icon = R.drawable.key,
                isChecked = installChecked,
                onClick = { installChecked = !installChecked }
            )
            ServiceButton(
                text = "Apertura de puertas (Casa, Apartamento, Carro)",
                icon = R.drawable.door,
                isChecked = fixChecked,
                onClick = { fixChecked = !fixChecked },
                fontSize = 14
            )
        }

        // Date and Time selection
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, top = 10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.calendario),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Elige el día y la hora",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Button(
                onClick = {
                    showDatePickerDialog(context) { date ->
                        selectedDate = date
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(bottom = 8.dp, top = 5.dp),
            ) {
                Text(text = selectedDate?.let { formatDate5(it) } ?: "Seleccionar Fecha")
            }
            SelectHourButton(selectedHour, context) { hour ->
                selectedHour = hour
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, top = 10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Agrega la foto (Opcional)",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Botones para tomar o subir foto
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PermissionChecker.PERMISSION_GRANTED) {
                        val photoFile = createImageFile(context)
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        photoUri?.let {
                            cameraLauncher.launch(it)
                        }
                    } else {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(bottom = 8.dp, top = 5.dp)
            ) {
                Text(text = "Tomar foto")
            }
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(bottom = 8.dp, top = 5.dp)
            ) {
                Text(text = "Subir foto")
            }
        }

        // Mostrar vista previa de la imagen seleccionada
        if (selectedImageUri != null) {
            Image(
                painter = rememberImagePainter(data = selectedImageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .width(250.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                        shape = RoundedCornerShape(12.dp)
                        clip = true
                    }
                    .clickable { showImageDialog = true },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Description input
        Column(
            modifier = Modifier.padding(bottom = 8.dp, top = 10.dp)
        ) {
            Text(
                text = "¿Como te ayudamos?",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = describe2,
                onValueChange = { describe2 = it },
                placeholder = { Text("Describe detalladamente tu problema...") },
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xffe8e8e8)),
                modifier = Modifier
                    .requiredWidth(300.dp)
                    .requiredHeight(117.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        // Calcular precios en tiempo real
        val precioDomicilio = calcularPrecioDomicilio(distance)
        val precioServicio = calcularPrecioServicio(installChecked, fixChecked, defPrecio, precioInstalacionMin, precioInstalacionMax, precioProblemasMin, precioProblemasMax)
        val extraPriceForDate = if (selectedDate?.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || selectedDate?.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) extraWeekendPrice else 0
        val extraPriceForHour = if (selectedHour?.let { isNightHour(it) } == true) extraNightPrice else 0
        precioTotal = precioDomicilio + precioServicio + extraPriceForDate + extraPriceForHour

        // Mostrar precios calculados
        val formattedDomicilioPrice = NumberFormat.getNumberInstance(Locale.US).format(precioDomicilio)
        val formattedServicioPrice = NumberFormat.getNumberInstance(Locale.US).format(precioServicio)
        val formattedTotalPrice = NumberFormat.getNumberInstance(Locale.US).format(precioTotal)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Tarfia de domicilio: $formattedDomicilioPrice COP",
                color = Color.DarkGray,
                textAlign = TextAlign.Left,
                style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Tarfia de servicio: $formattedServicioPrice COP",
                color = Color.DarkGray,
                textAlign = TextAlign.Left,
                style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(top = 8.dp)
            )
            if (extraPriceForDate > 0) {
                Text(
                    text = "Tarfia de fin de semana: ${
                        NumberFormat.getNumberInstance(Locale.US).format(extraPriceForDate)
                    } COP",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Left,
                    style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (extraPriceForHour > 0) {
                Text(
                    text = "Tarfia nocturna: ${
                        NumberFormat.getNumberInstance(Locale.US).format(extraPriceForHour)
                    } COP",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Left,
                    style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Text(
            text = "Precio Total: $formattedTotalPrice COP",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp)
        )

        // Continue button
        val isButtonEnabled = (installChecked || fixChecked) && selectedDate != null && selectedHour != null

        Button(
            onClick = {
                if (isButtonEnabled) {
                    val serviceCode = generateServiceCode(installChecked, fixChecked)
                    val selectedServiceNames = getServiceNames(installChecked, fixChecked)

                    val intent = Intent(context, PagoActivity::class.java).apply {
                        putExtra("userEmail", userEmail)
                        putExtra("idPrestador", idPrestador)
                        putExtra("servicio", servicio)
                        putExtra("userLat", userLat)
                        putExtra("userLon", userLon)
                        putExtra("describe2", describe2)
                        putExtra("precioTotal", precioTotal)
                        putExtra("selectedDate", selectedDate?.timeInMillis)
                        putExtra("selectedHour", selectedHour)
                        selectedImageUri?.let { putExtra("selectedImageUri", it.toString()) }
                        putExtra("serviceCode", serviceCode)
                        putExtra("selectedServices", selectedServiceNames)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Debes completar todos los datos", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) MaterialTheme.colorScheme.tertiary else Color.Gray,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.padding(bottom = 20.dp, top = 20.dp)
        ) {
            Text(text = "Continuar")
        }
    }

    // Mostrar imagen completa en un diálogo
    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = rememberImagePainter(data = selectedImageUri),
                        contentDescription = "Full Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { showImageDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 35.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceButton(
    text: String,
    @DrawableRes icon: Int,
    isChecked: Boolean,
    onClick: () -> Unit,
    fontSize: Int = 18
) {
    val backgroundColor = if (isChecked) MaterialTheme.colorScheme.tertiary else Color.LightGray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable(onClick = onClick)
            .width(120.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = fontSize.sp),
        )
    }
}

@Composable
private fun SelectHourButton(selectedHour: String?, context: Context, onHourSelected: (String) -> Unit) {
    Button(
        onClick = {
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            showTimePickerDialog(context, hourOfDay, minute, onHourSelected)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.padding(bottom = 8.dp, top = 5.dp, start = 8.dp)
    ) {
        Text(text = selectedHour ?: "Seleccionar Hora")
    }
}

private fun calcularPrecioDomicilio(distance: String): Int {
    val distanceInKm = distance.substringBefore(" ").toDoubleOrNull() ?: 0.0
    return (distanceInKm * 1000).toInt()
}

private fun calcularPrecioServicio(
    installChecked: Boolean,
    fixChecked: Boolean,
    defPrecio: String,
    precioInstalacionMin: Int,
    precioInstalacionMax: Int,
    precioProblemasMin: Int,
    precioProblemasMax: Int
): Int {
    var precioTotal = 0

    if (installChecked) {
        precioTotal += when (defPrecio) {
            "max" -> precioInstalacionMax
            "min" -> precioInstalacionMin
            else -> (precioInstalacionMax + precioInstalacionMin) / 2
        }
    }

    if (fixChecked) {
        precioTotal += when (defPrecio) {
            "max" -> precioProblemasMax
            "min" -> precioProblemasMin
            else -> (precioProblemasMax + precioProblemasMin) / 2
        }
    }

    return precioTotal
}

private fun showDatePickerDialog(context: Context, onDateSelected: (Calendar) -> Unit) {
    val today = Calendar.getInstance()
    val maxDate = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            onDateSelected(selectedDate)
        },
        today.get(Calendar.YEAR),
        today.get(Calendar.MONTH),
        today.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = today.timeInMillis
        datePicker.maxDate = maxDate.timeInMillis
    }.show()
}

private fun showTimePickerDialog(context: Context, hourOfDay: Int, minute: Int, onHourSelected: (String) -> Unit) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay >= 12) "PM" else "AM"
            val hourFormatted = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
            val hourString = String.format(Locale.getDefault(), "%02d:%02d %s", hourFormatted, minute, amPm)
            onHourSelected(hourString)
        },
        hourOfDay,
        minute,
        false
    ).show()
}

private fun formatDate5(calendar: Calendar): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}

// Function to generate service code
private fun generateServiceCode(installChecked: Boolean, fixChecked: Boolean): String {
    return when {
        installChecked && fixChecked -> "C.1.2"
        installChecked -> "C.1"
        fixChecked -> "C.2"
        else -> ""
    }
}

// Function to get selected service names
private fun getServiceNames(installChecked: Boolean, fixChecked: Boolean): String {
    val services = mutableListOf<String>()
    if (installChecked) services.add("Duplicado de llaves")
    if (fixChecked) services.add("Apertura de puertas (Casa, Apartamento, Carro)")
    return services.joinToString(" y ")
}

// Function to check if the selected hour is a night hour
private fun isNightHour(hour: String): Boolean {
    val hourInt = hour.substringBefore(":").toIntOrNull() ?: return false
    val amPm = hour.substringAfter(" ").toLowerCase(Locale.ROOT)
    return (hourInt in 7..11 && amPm == "pm") || (hourInt in 12..6 && amPm == "am")
}

// Function to create an image file
private fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

