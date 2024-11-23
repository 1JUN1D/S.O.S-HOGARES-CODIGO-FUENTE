package com.uv.mapa_usuario

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.uv.mapa_usuario.ui.theme.SOSTheme
import java.util.Calendar

class PagoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtener los datos del Intent
        val userEmail = intent.getStringExtra("userEmail")?: "No hay"
        val idPrestador =  intent.getIntExtra("idPrestador",0)
        val servicio = intent.getStringExtra("servicio")?: "Se desconoce"
        val userLat = intent.getDoubleExtra("userLat", 0.0)
        val userLon = intent.getDoubleExtra("userLon", 0.0)
        val serviceCode = intent.getStringExtra("serviceCode")?: "No hay"
        val describe2 = intent.getStringExtra("describe2") ?: "No description"
        val precioTotal = intent.getIntExtra("precioTotal", 0)
        val selectedDateInMillis = intent.getLongExtra("selectedDate", 0L)
        val selectedHour = intent.getStringExtra("selectedHour") ?: "No hour"
        val selectedServices = intent.getStringExtra("selectedServices") ?: "No services"
        val imageUriString = intent.getStringExtra("selectedImageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        // Convertir la fecha de milisegundos a una cadena legible
        val formattedDate = if (selectedDateInMillis != 0L) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateInMillis
            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(
                Calendar.YEAR)}"
        } else {
            "No date"
        }
        setContent {
            SOSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Pago(navController,
                        userEmail = userEmail,
                        idPrestador = idPrestador,
                        serviceCode = serviceCode,
                        describe2 = describe2,
                        precioTotal = precioTotal,
                        selectedDate = formattedDate,
                        selectedHour = selectedHour,
                        selectedServices = selectedServices,
                        imageUri = imageUri,
                        servicio = servicio,
                        userLat = userLat,
                        userLon = userLon)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pago(navController: NavController,
         userEmail: String,
         idPrestador: Int,
         serviceCode: String,
         describe2: String,
         precioTotal: Int,
         selectedDate: String,
         selectedHour: String,
         selectedServices: String,
         imageUri: Uri?,
         servicio: String,
         userLat: Double,
         userLon: Double,
         modifier: Modifier = Modifier) {
    var showDialogoConfirmacion by remember { mutableStateOf(false) }
    var showDialogoConfirmacionDaviplata by remember { mutableStateOf(false) }
    var showDialogoConfirmacionNequi by remember { mutableStateOf(false) }
    var showDialogo3 by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(height = 891.dp)
        ){
            IconButton(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            // Botón de pago en efectivo
            Button(
                onClick = {
                    // Mostrar la ventana de confirmación
                    showDialogoConfirmacion = true
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 0.dp, y = -80.dp)
                    .requiredWidth(width = 204.dp)
                    .requiredHeight(height = 43.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.money), // Aquí colocas el recurso de tu icono
                    tint = Color.Green,
                    contentDescription = "Efectivo Icon",
                    modifier = Modifier.size(24.dp) // Tamaño del icono
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el icono y el texto
                Text(
                    text = "EFECTIVO",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.wrapContentSize(align = Alignment.Center)
                )
            }


            // Botón de pago Daviplata
            Button(
                onClick = {
                    // Mostrar la ventana de confirmación
                    showDialogoConfirmacionDaviplata = true
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 0.dp, y = 80.dp)
                    .requiredWidth(width = 204.dp)
                    .requiredHeight(height = 43.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.daviplata), // Aquí colocas el recurso de tu icono
                    tint = Color.Red,
                    contentDescription = "Daviplata Icon",
                    modifier = Modifier.size(24.dp) // Tamaño del icono
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el icono y el texto
                Text(
                    text = "DAVIPLATA",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.wrapContentSize(align = Alignment.Center)
                )
            }

            // Botón de pago Nequi
            Button(
                onClick = {
                    // Mostrar la ventana de confirmación
                    showDialogoConfirmacionNequi = true
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 0.dp, y = 0.dp)
                    .requiredWidth(width = 204.dp)
                    .requiredHeight(height = 43.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nequi), // Aquí colocas el recurso de tu icono
                    tint = Color.Black,
                    contentDescription = "Nequi Icon",
                    modifier = Modifier.size(24.dp) // Tamaño del icono
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el icono y el texto
                Text(
                    text = "NEQUI",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.wrapContentSize(align = Alignment.Center)
                )
            }

            Text(
                text = "Pago de servicio",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 0.dp,
                        y = (-334).dp)
                    .wrapContentHeight(align = Alignment.CenterVertically))
            Text(
                text = "Elige el metodo de pago que mejor se\najuste a su facilidad de pago.",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = (-10.5).dp,
                        y = (-257).dp)
                    .wrapContentHeight(align = Alignment.CenterVertically))
            Button(
                onClick = {
                    showDialogo3 = true
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = 0.dp,
                        y = 270.5.dp)
                    .requiredWidth(width = 204.dp)
                    .requiredHeight(height = 43.dp)){ }
            Text(
                text = "CANCELAR",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 15.sp),
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .offset(x = (-2.5).dp,
                        y = 270.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically))
        }

        // Ventana de confirmación para EFECTIVO
        if (showDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = {
                    showDialogoConfirmacion = false
                },
                title = {
                    Text(text = "¿Estás seguro de que quieres pagar en efectivo?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacion = false
                            // Crear un Intent para ir a Prueba3Activity y enviar el método de pago
                            val intent = Intent(navController.context, Prueba3Activity::class.java).apply {
                                putExtra("userEmail", userEmail)
                                putExtra("idPrestador", idPrestador)
                                putExtra("servicio",servicio)
                                putExtra("userLat",userLat)
                                putExtra("userLon",userLon)
                                putExtra("serviceCode", serviceCode)
                                putExtra("describe2", describe2)
                                putExtra("precioTotal", precioTotal)
                                putExtra("selectedDate", selectedDate)
                                putExtra("selectedHour", selectedHour)
                                putExtra("selectedServices", selectedServices)
                                putExtra("tipo_pago", "EFECTIVO")

                                // Asegurarse de que `imageUri` esté presente antes de enviarla
                                imageUri?.let {
                                    putExtra("selectedImageUri", it.toString())
                                }
                            }
                            navController.context.startActivity(intent)
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacion = false
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }

// Ventana de confirmación para DAVIPLATA
        if (showDialogoConfirmacionDaviplata) {
            AlertDialog(
                onDismissRequest = {
                    showDialogoConfirmacionDaviplata = false
                },
                title = {
                    Text(
                        text = "Instrucciones para pagar con Daviplata",
                        style = TextStyle(fontSize = 20.sp),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.CenterVertically)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr),
                                contentDescription = "Daviplata Icon",
                                modifier = Modifier.size(200.dp)
                            )
                            Spacer(modifier = Modifier.width(28.dp))
                            Text(
                                "Número de Daviplata: 3124324532",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                "Puedes tomar una captura de pantalla y leerlo desde la aplicación de Daviplata o puedes digitar el número que muestra en pantalla",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacion = false
                            // Crear un Intent para ir a Prueba3Activity y enviar el método de pago
                            val intent = Intent(navController.context, Prueba3Activity::class.java).apply {
                                putExtra("userEmail", userEmail)
                                putExtra("idPrestador", idPrestador)
                                putExtra("servicio",servicio)
                                putExtra("userLat",userLat)
                                putExtra("userLon",userLon)
                                putExtra("serviceCode", serviceCode)
                                putExtra("describe2", describe2)
                                putExtra("precioTotal", precioTotal)
                                putExtra("selectedDate", selectedDate)
                                putExtra("selectedHour", selectedHour)
                                putExtra("selectedServices", selectedServices)
                                putExtra("tipo_pago", "DAVIPLATA")

                                // Asegurarse de que `imageUri` esté presente antes de enviarla
                                imageUri?.let {
                                    putExtra("selectedImageUri", it.toString())
                                }
                            }
                            navController.context.startActivity(intent)
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacionDaviplata = false
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }

// Ventana de confirmación para NEQUI
        if (showDialogoConfirmacionNequi) {
            AlertDialog(
                onDismissRequest = {
                    showDialogoConfirmacionNequi = false
                },
                title = {
                    Text(
                        text = "Instrucciones para pagar con Nequi",
                        style = TextStyle(fontSize = 20.sp),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.CenterVertically)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr),
                                contentDescription = "Nequi Icon",
                                modifier = Modifier.size(200.dp)
                            )
                            Spacer(modifier = Modifier.width(28.dp))
                            Text(
                                "Número de Nequi: 3124324532",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                "Puedes tomar una captura de pantalla y leerlo desde la aplicación de Nequi o puedes digitar el número que muestra en pantalla",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacion = false
                            // Crear un Intent para ir a Prueba3Activity y enviar el método de pago
                            val intent = Intent(navController.context, Prueba3Activity::class.java).apply {
                                putExtra("userEmail", userEmail)
                                putExtra("idPrestador", idPrestador)
                                putExtra("servicio",servicio)
                                putExtra("userLat",userLat)
                                putExtra("userLon",userLon)
                                putExtra("serviceCode", serviceCode)
                                putExtra("describe2", describe2)
                                putExtra("precioTotal", precioTotal)
                                putExtra("selectedDate", selectedDate)
                                putExtra("selectedHour", selectedHour)
                                putExtra("selectedServices", selectedServices)
                                putExtra("tipo_pago", "NEQUI")

                                // Asegurarse de que `imageUri` esté presente antes de enviarla
                                imageUri?.let {
                                    putExtra("selectedImageUri", it.toString())
                                }
                            }
                            navController.context.startActivity(intent)
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDialogoConfirmacionNequi = false
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }


        // Diálogo de Cancelar
        if (showDialogo3) {
            AlertDialog(
                onDismissRequest = {
                    // Ocultar el diálogo de confirmación
                    showDialogo3 = false
                },
                title = {
                    Text(text = "¿Estás seguro de que quieres cancelar el servicio?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Ocultar el diálogo de confirmación
                            showDialogo3 = false
                            // Navegar a la pantalla showWithMapLocation
                            navController.navigate("showWithMapLocation")
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xff0dc2e5)) // Cambiar color del botón
                    ) {
                        Text("Cancelar servicio", color = Color.Black) // Cambiar color del texto del botón
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Ocultar el diálogo de confirmación
                            showDialogo3 = false
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xff0dc2e5)) // Cambiar color del botón
                    ) {
                        Text("Volver", color = Color.Black) // Cambiar color del texto del botón
                    }
                },
            )
        }

    }
}
