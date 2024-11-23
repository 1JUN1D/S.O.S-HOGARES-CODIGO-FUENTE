package com.uv.mapa_usuario.api

data class MyApiResponse(
    val success: Boolean, // Para saber si la operación fue exitosa
    val message: String?, // Mensaje informativo
    val servicio: Servicio? // Contiene los datos del servicio subido
)

data class Servicio(
    val idservicio: Int?,
    val idusuario: Int,
    val idprestador: Int,
    val categoria: Int,
    val tipo: String,
    val descripcion: String,
    val lat: Double,
    val lon: Double,
    val fecha: String, // En formato legible como "dd/MM/yyyy"
    val multimedia: String?, // Nombre del archivo subido
    val precio: Double,
    val metodo_pago: Int,
    val estado: Int,
    val valoracion: Double?,
    val comentario_val: String?,
    val hora: String,
)

