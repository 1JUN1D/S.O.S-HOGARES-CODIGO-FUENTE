package com.uv.registro

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.uv.registro.api.Prestador
import com.uv.registro.api.RetrofitClient
import com.uv.registro.api.UserResponse
import com.uv.registro.ui.theme.SOSTheme
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class SectionData(val headerText: String, val items: List<String>)

class RegistroActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }
    private var tarifaCheckState by mutableStateOf(false)
    private var hojaCheckState by mutableStateOf(false)
    private var documentoCheckState by mutableStateOf(false)


    private val startForResultTarifas = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            tarifaCheckState = result.data?.getBooleanExtra("tarifaCompletada", false) ?: false
            val uriString = result.data?.getStringExtra("archivoTarifaUri")
            prefs.edit().putString("tarifaArchivoSeleccionadoUri", uriString ?: "").apply()

        }
    }

    private val startForResultHoja = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            hojaCheckState = result.data?.getBooleanExtra("hojaCompletada", false) ?: false
            val uriString = result.data?.getStringExtra("archivoHojaUri")
            prefs.edit().putString("hojaArchivoSeleccionadoUri", uriString ?: "").apply()
        }
    }

    private val startForResultDocumento = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            documentoCheckState = result.data?.getBooleanExtra("documentoCompletada", false) ?: false
            val uriString = result.data?.getStringExtra("archivoDocumentoUri")
            prefs.edit().putString("documentoArchivoSeleccionadoUri", uriString ?: "").apply()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("userEmail") ?: "Email not available"

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("current_activity", "RegistroActivity")
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
                RegistroScreen(userEmail, tarifaCheckState, hojaCheckState, documentoCheckState, ::navigateToTarifas, ::navigateToHoja, ::navigateToDocumento, prefs)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar el estado de los "Checks" al reanudar para reflejar cualquier cambio
        tarifaCheckState = prefs.getBoolean("tarifaCompletados", false)
        hojaCheckState = prefs.getBoolean("hojaCompletados", false)
        documentoCheckState = prefs.getBoolean("documentoCompletados", false)


    }



    private fun navigateToTarifas() {
        val intent = Intent(this, TarifasActivity::class.java)
        startForResultTarifas.launch(intent)
    }

    private fun navigateToHoja() {
        val intent = Intent(this, HojaActivity::class.java)
        startForResultHoja.launch(intent)
    }
    private fun navigateToDocumento() {
        val intent = Intent(this, DocumentoActivity::class.java)
        startForResultDocumento.launch(intent)
    }


}

@Composable
fun RegistroScreen(
    userEmail: String,
    tarifaCheckState: Boolean,
    hojaCheckState: Boolean,
    documentoCheckState: Boolean,
    navigateToTarifas: () -> Unit,
    navigateToHoja: () -> Unit,
    navigateToDocumento: () -> Unit,
    prefs: SharedPreferences  // Agregar esto como un parámetro
) {
    val allChecksPassed = remember(tarifaCheckState, hojaCheckState, documentoCheckState) {
        tarifaCheckState && hojaCheckState && documentoCheckState
    }
    var nombre by remember { mutableStateOf("") }
    var servicioSeleccionado by remember { mutableStateOf("") } // Estado para el servicio seleccionado
    var desde by remember { mutableStateOf("Lunes") }
    var hasta by remember { mutableStateOf("Domingo") }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1DA0FF), Color(0xFF90CEFF)),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )
    val sections = listOf(
        SectionData("Servicios", listOf("Cerrajeria", "Electricista", "Limpieza", "Pintura"))
    )
    val servicioMap = mapOf(
        "Cerrajeria" to 1,
        "Electricista" to 2,
        "Limpieza" to 3,
        "Pintura" to 4
    )

    Column(
        modifier = Modifier.background(brush = gradient).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp) // Añade padding alrededor del Row
        ) {
            Image(
                painter = painterResource(id = R.drawable.salvavidas2),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp) // Ajusta el tamaño según sea necesario
                    .padding(end = 16.dp) // Añade un padding al final para separar la imagen del texto
            )
            Text(
                text = "SOS\nHOGARES",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
            )
        }
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            value = userEmail,
            onValueChange = { },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            readOnly = true
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Expandable list
        ExpandableList(sections, servicioSeleccionado, onServiceSelected = { service ->
            servicioSeleccionado = service
        })
        Spacer(modifier = Modifier.height(20.dp))
        DateRangePicker(
            desde = desde,
            hasta = hasta,
            onDesdeSelected = { desde = it },
            onHastaSelected = { hasta = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        FilledTonalButton(
            onClick = navigateToTarifas,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Tarifa",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.width(8.dp))
            if (tarifaCheckState) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "Completado")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(
            onClick = navigateToHoja,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Hoja de Vida",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.width(8.dp))
            if (hojaCheckState) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "Completado")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilledTonalButton(
            onClick = navigateToDocumento,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Documento de Identidad",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.width(8.dp))
            if (documentoCheckState) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "Completado")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (nombre.isNotBlank() && servicioMap.containsKey(servicioSeleccionado) && tarifaCheckState && hojaCheckState && documentoCheckState) {
                    showDialog = true
                } else {
                    Toast.makeText(context, "Complete los campos requeridos", Toast.LENGTH_LONG).show()
                }
            }
            ,
            enabled = nombre.isNotBlank() && servicioMap.containsKey(servicioSeleccionado)
        ) {
            Text("Continuar")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirmación") },
                text = { Text("¿Estás seguro de que todos los datos suministrados están correctos?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        enviarDatosPrestador(nombre, userEmail, desde, hasta, servicioMap[servicioSeleccionado]!!, context, prefs)

                        // Navegar a MapaUsuarioActivity
                        val intent = Intent(context, Class.forName("com.uv.mapa_usuario.UbicacionActivity")).apply {
                            putExtra("userEmail", userEmail)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

fun enviarDatosPrestador(nombre: String, email: String, desde: String, hasta: String, servicioId: Int, context: Context, prefs: SharedPreferences) {
    val uriTarifa = Uri.parse(prefs.getString("tarifaArchivoSeleccionadoUri", ""))
    val uriHojaVida = Uri.parse(prefs.getString("hojaArchivoSeleccionadoUri", ""))
    val uriCedula = Uri.parse(prefs.getString("documentoArchivoSeleccionadoUri", ""))

    // Añadir logs para verificar los Uris
    Log.d("CheckUri", "Uri Tarifa: $uriTarifa")
    Log.d("CheckUri", "Uri Hoja Vida: $uriHojaVida")
    Log.d("CheckUri", "Uri Cedula: $uriCedula")

    val tarifaFile = prepareFilePart(uriTarifa, "tarifa", context)
    val hojaVidaFile = prepareFilePart(uriHojaVida, "hoja_vida", context)
    val cedulaFile = prepareFilePart(uriCedula, "cedula", context)

    val nicknamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), nombre)

    val prestadorInfo = Prestador(nombre, email, desde, hasta, servicioId, uriTarifa.toString(), uriHojaVida.toString(), uriCedula.toString())
    Log.d("PrestadorInfo", "Datos del prestador: $prestadorInfo")

    val prestadorRequestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), Gson().toJson(prestadorInfo))

    RetrofitClient.api.insertarPrestador(nombre, prestadorRequestBody, tarifaFile, hojaVidaFile, cedulaFile).enqueue(object : Callback<UserResponse> {
        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
            if (response.isSuccessful) {
                Toast.makeText(context, "Prestador registrado con éxito!", Toast.LENGTH_LONG).show()
            } else {
                Log.d("APIError", "Respuesta fallida: ${response.errorBody()?.string()}")
                Toast.makeText(context, "Error al registrar prestador: ${response.message()}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailure(call: Call<UserResponse>, t: Throwable) {
            Log.d("APIError", "Fallo de conexión: ${t.message}")
            Toast.makeText(context, "Fallo de conexión: ${t.message}", Toast.LENGTH_LONG).show()
        }
    })
}



private fun prepareFilePart(fileUri: Uri, partName: String, context: Context): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
    val fileStream = contentResolver.openInputStream(fileUri)
    val fileBytes = fileStream?.readBytes() ?: ByteArray(0)
    val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), fileBytes)
    val fileName = getFileName(fileUri, context) ?: "default_file_name.pdf"
    return MultipartBody.Part.createFormData(partName, fileName, requestFile)
}



private fun getFileName(uri: Uri, context: Context): String? {
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    return it.getString(index)
                }
            }
        }
    }
    return uri.path?.split("/")?.last() // Fall back to last segment of the path if all else fails
}



@Composable
fun ExpandableList(
    sections: List<SectionData>,
    servicioSeleccionado: String,
    onServiceSelected: (String) -> Unit
) {
    val isExpandedMap = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            sections.forEachIndexed { index, _ -> this[index] = false }
        }
    }

    LazyColumn( // Nested LazyColumn for list items
        modifier = Modifier
            // White background for expanded section
            .padding(vertical = 8.dp) // Add vertical padding
    ) {
        sections.forEachIndexed { index, sectionData ->
            item {
                SectionHeader(
                    text = sectionData.headerText, // Show initial header "Servicios"
                    isExpanded = isExpandedMap[index] ?: false,
                    servicioSeleccionado = servicioSeleccionado,
                    onHeaderClicked = {
                        isExpandedMap[index] = !(isExpandedMap[index] ?: false)
                        // Call 'onHeaderClicked' when header is selected
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isExpandedMap[index] == true) {
                items(sectionData.items) { itemText ->
                    SectionItem(
                        text = itemText,
                        isSelected = itemText == servicioSeleccionado,
                        onItemClicked = {
                            onServiceSelected(itemText)
                            // Update header when an item is selected
                            isExpandedMap[index] = false // Collapse list after selection
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    text: String,
    isExpanded: Boolean,
    servicioSeleccionado: String?,
    onHeaderClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.clickable(onClick = onHeaderClicked)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                AnimatedVisibility(visible = !isExpanded) {
                    Text(
                        text = "Servicios",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = servicioSeleccionado ?: text,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1.0f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowForward,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        }
    }
}


@Composable
fun SectionHeaderTwo(
    text: String,
    isExpanded: Boolean,
    servicioSeleccionado: String?,
    onHeaderClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.clickable(onClick = onHeaderClicked)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                AnimatedVisibility(visible = !isExpanded) {
                    Text(
                        text = text,  // Asegurarse de que este es el texto del parámetro
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = servicioSeleccionado ?: text,  // Aquí puede estar el problema
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1.0f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowForward,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        }
    }
}



@Composable
fun SectionItem(
    text: String,
    isSelected: Boolean,
    onItemClicked: () -> Unit
) {
    val colorb = MaterialTheme.colorScheme.surfaceVariant
    Box( // Wrap content in a Box
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClicked)
            .padding(horizontal = 16.dp)
            .background(colorb)
            .padding(vertical = 8.dp)

    ) {
        if (isSelected) { // Apply white background only when selected
            Surface( // Use Surface for background customization
                modifier = Modifier.matchParentSize(),
            ) {}
        }
        Text( // Text on top of the background
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Center) // Center text within the Box

        )
    }
}


@Composable
fun DateRangePicker(
    desde: String,
    hasta: String,
    onDesdeSelected: (String) -> Unit,
    onHastaSelected: (String) -> Unit
) {
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val isDesdeExpanded = remember { mutableStateOf(false) }
    val isHastaExpanded = remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Columna para "Desde"
        ExpandableListPicker(
            label = "Desde",
            items = daysOfWeek,
            selectedItem = desde,
            expanded = isDesdeExpanded,
            onItemSelected = {
                onDesdeSelected(it)
                isDesdeExpanded.value = false
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp)) // Espacio entre las columnas

        // Columna para "Hasta"
        ExpandableListPicker(
            label = "Hasta",
            items = daysOfWeek,
            selectedItem = hasta,
            expanded = isHastaExpanded,
            onItemSelected = {
                onHastaSelected(it)
                isHastaExpanded.value = false
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ExpandableListPicker(
    label: String,
    items: List<String>,
    selectedItem: String,
    expanded: MutableState<Boolean>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeaderTwo(
            text = label,
            isExpanded = expanded.value,
            servicioSeleccionado = selectedItem,
            onHeaderClicked = { expanded.value = !expanded.value }
        )
        if (expanded.value) {
            LazyColumn {
                items(items) { item ->
                    SectionItem(
                        text = item,
                        isSelected = item == selectedItem,
                        onItemClicked = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}



