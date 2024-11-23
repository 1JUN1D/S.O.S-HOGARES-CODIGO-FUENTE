package com.uv.usperfil

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.uv.usperfil.ui.theme.SOSTheme
import java.io.File
import java.text.SimpleDateFormat
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import java.util.Date
import java.util.Locale
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.delay
import java.io.FileOutputStream

class EditarFotoActivity : ComponentActivity() {
    private var currentPhotoPath: String? = null
    private val selectedImageUri = mutableStateOf<Uri?>(null)
    private val showLottieAnimation = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getIntExtra("id", -1)
        val userEmail = intent.getStringExtra("email") ?: ""

        // Agregar log para mostrar userId y userEmail
        Log.d("EditarFotoActivity", "Received userId: $userId, userEmail: $userEmail")

        setContent {
            SOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditarFotoScreen(
                        userId = userId,
                        userEmail = userEmail,
                        onTakePhoto = { takePhoto() },
                        onSelectPhoto = { selectPhoto() },
                        onUploadPhoto = { uploadPhoto(userId, userEmail) },
                        selectedImageUri = selectedImageUri,
                        showLottieAnimation = showLottieAnimation
                    )
                }
            }
        }
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("EditarFotoActivity", "Error creating image file", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    it
                )
                currentPhotoPath = it.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_SELECT_PHOTO)
    }

    private fun uploadPhoto(userId: Int, userEmail: String) {
        if (currentPhotoPath == null) {
            Toast.makeText(this, "No se ha seleccionado ninguna foto", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(currentPhotoPath!!)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("foto", file.name, requestFile)

        val userJson = """
            {
                "id": $userId,
                "email": "$userEmail"
            }
        """.trimIndent()

        val call = RetrofitClient.instance.actualizarFotoUsuario(
            MultipartBody.Part.createFormData("user", userJson),
            body
        )

        showLottieAnimation.value = true

        lifecycleScope.launch {
            val uploadResult = suspendCoroutine<Response<Void>> { cont ->
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        cont.resume(response)
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        cont.resumeWithException(t)
                    }
                })
            }

            if (uploadResult.isSuccessful) {
                Toast.makeText(this@EditarFotoActivity, "Foto actualizada con éxito", Toast.LENGTH_SHORT).show()
                delay(3000) // Mostrar animación Lottie durante 3 segundos
                finish() // Cerrar actividad después de la animación
            } else {
                showLottieAnimation.value = false
                Toast.makeText(this@EditarFotoActivity, "Error al actualizar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
            Log.d("EditarFotoActivity", "Image file created at $currentPhotoPath")
        }
    }

    companion object {
        private const val REQUEST_TAKE_PHOTO = 1
        private const val REQUEST_SELECT_PHOTO = 2
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> {
                    Log.d("EditarFotoActivity", "Photo taken, currentPhotoPath: $currentPhotoPath")
                    currentPhotoPath?.let {
                        val file = File(it)
                        if (file.exists()) {
                            selectedImageUri.value = Uri.fromFile(file)
                            Log.d("EditarFotoActivity", "Photo URI set: ${selectedImageUri.value}")
                            Toast.makeText(this, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                REQUEST_SELECT_PHOTO -> {
                    val uri = data?.data
                    uri?.let {
                        currentPhotoPath = copyFileToInternalStorage(it, "selected_photo")
                        Log.d("EditarFotoActivity", "Photo selected, currentPhotoPath: $currentPhotoPath")
                        selectedImageUri.value = Uri.fromFile(File(currentPhotoPath!!))
                        Log.d("EditarFotoActivity", "Photo URI set: ${selectedImageUri.value}")
                        Toast.makeText(this, "Foto seleccionada correctamente", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.d("EditarFotoActivity", "Result not OK, resultCode: $resultCode")
        }
    }

    private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String? {
        val returnCursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        if (returnCursor != null && returnCursor.moveToFirst()) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            val name = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex)
            returnCursor.close()

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), newDirName)
            if (!storageDir.exists()) storageDir.mkdirs()

            val file = File(storageDir, name)
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.close()
            inputStream.close()
            return file.absolutePath
        }
        return null
    }
}

@Composable
private fun EditarFotoScreen(
    userId: Int,
    userEmail: String,
    onTakePhoto: () -> Unit,
    onSelectPhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    selectedImageUri: MutableState<Uri?>,
    showLottieAnimation: MutableState<Boolean>
) {
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
            text = "Editar Foto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                Log.d("EditarFotoScreen", "Take Photo button clicked")
                onTakePhoto()
            }) {
                Text("Tomar Foto")
            }
            Button(onClick = {
                Log.d("EditarFotoScreen", "Select Photo button clicked")
                onSelectPhoto()
            }) {
                Text("Seleccionar Foto")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri.value?.let {
            Log.d("EditarFotoScreen", "Displaying selected image: $it")
            Image(
                painter = rememberImagePainter(data = it),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .padding(2.dp),
                contentScale = ContentScale.Crop
            )
        } ?: Log.d("EditarFotoScreen", "No image to display")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            Log.d("EditarFotoScreen", "Upload Photo button clicked")
            onUploadPhoto()
        }) {
            Text("Subir Foto")
        }

        if (showLottieAnimation.value) {
            Log.d("EditarFotoScreen", "Displaying Lottie animation")
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
