package com.uv.resumen.api

import android.provider.ContactsContract.CommonDataKinds.Nickname

data class Service(
    val idservicio: Int,
    val idusuario: Int,
    val idprestador: Int,
    val nickname: String,
    val categoria: String,
    val tipo: String,
    val descripcion: String,
    val lat: Double,
    val lon: Double,
    val fecha: String,
    val multimedia: String,
    val precio: Double,
    val metodo_pago: Int,
    val estado: Int,
    val valoracion: Double?,
    val hora: String
)