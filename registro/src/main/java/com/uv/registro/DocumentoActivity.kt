package com.uv.registro

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.uv.registro.ui.theme.SOSTheme

class DocumentoActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    private var archivoUri: Uri? by mutableStateOf(null)
    private var thumbnailBitmap: Bitmap? by mutableStateOf(null)

    private val seleccionarArchivoLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        archivoUri = uri
        uri?.let {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val fileName = getFileName(uri)
            prefs.edit().putString("documentoArchivoSeleccionadoUri", uri.toString()).apply()
            prefs.edit().putString("documentoArchivoNombre", fileName).apply()
            prefs.edit().putBoolean("documentoCompletados", true).apply()
            generateThumbnail(uri)

        }
    }

    private val solicitarPermisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            abrirSelectorArchivos()
        } else {
            // Opcional: Manejar la negación del permiso aquí
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        archivoUri = prefs.getString("documentoArchivoSeleccionadoUri", null)?.let { Uri.parse(it) }
        archivoUri?.let { generateThumbnail(it) }

        setContent {
            SOSTheme {
                PantallaDocumento(
                    archivoUri = archivoUri,
                    archivoNombre = prefs.getString("documentoArchivoNombre", "No file selected"),
                    thumbnailBitmap = thumbnailBitmap,
                    onSubirArchivoClicked = { solicitarPermisoYAbrirArchivos() },
                    onOkClicked = { finishActivityConResultado() },
                    onBorrarClicked = { borrarSeleccion() },
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    private fun abrirSelectorArchivos() {
        seleccionarArchivoLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
    }

    private fun solicitarPermisoYAbrirArchivos() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> abrirSelectorArchivos()
            else -> solicitarPermisoLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun borrarSeleccion() {
        archivoUri = null
        prefs.edit().remove("documentoArchivoSeleccionadoUri").apply()
        prefs.edit().remove("documentoArchivoNombre").apply()
        prefs.edit().putBoolean("documentoCompletados", false).apply()
    }


    private fun finishActivityConResultado() {
        val uri = archivoUri
        if (uri != null) {
            val data = Intent().apply {
                putExtra("documentoCompletados", true)
                putExtra("archivoDocumentoUri", uri.toString())
            }
            setResult(Activity.RESULT_OK, data)
        } else {
            Toast.makeText(this, "No se ha seleccionado un archivo válido.", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun generateThumbnail(uri: Uri) {
        if (contentResolver.getType(uri) == "application/pdf") {
            contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { pdfRenderer ->
                    val page = pdfRenderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    thumbnailBitmap = bitmap
                    page.close()
                }
            }
        } else {
            // Handle other file types if needed
        }
    }
}

@Composable
fun PantallaDocumento(
    archivoUri: Uri?,
    archivoNombre: String?,
    thumbnailBitmap: Bitmap?,
    onSubirArchivoClicked: () -> Unit,
    onOkClicked: () -> Unit,
    onBorrarClicked: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    Column {

        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
            }
            Image(
                painter = painterResource(id = R.drawable.salvavidas2),
                contentDescription = "Salvavidas",
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sube tu cédula", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSecondaryContainer), fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onSubirArchivoClicked,
                    modifier = Modifier.width(300.dp)
                ) {
                    Text(
                        "Subir Archivos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                archivoUri?.let {
                    Text(
                        "Archivo seleccionado:",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSecondaryContainer),
                        fontWeight = FontWeight.Bold
                    )

                    // Thumbnail Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(Color.DarkGray) // Apply background after clipping
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        thumbnailBitmap?.let { bitmap ->
                            Box(
                                modifier = Modifier
                                    .height(300.dp)
                                    .background(Color.White), // Background for thumbnail
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Thumbnail",
                                    modifier = Modifier
                                        .height(300.dp)
                                )
                            }
                        }
                    }

                    // Text Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(Color.LightGray)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = archivoNombre ?: "Archivo Desconocido",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onBorrarClicked,
                        modifier = Modifier.width(300.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            "BORRAR",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onOkClicked,
                    modifier = Modifier.width(300.dp),
                    enabled = archivoUri != null
                ) {
                    Text(
                        "OK",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
