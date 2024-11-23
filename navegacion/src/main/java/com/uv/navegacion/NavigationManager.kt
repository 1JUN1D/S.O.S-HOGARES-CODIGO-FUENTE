// NavigationManager.kt
package com.uv.navegacion

import android.content.Context
import android.content.Intent

fun navigateToMap(context: Context, userEmail: String) {
    val intent = Intent(context, Class.forName("com.uv.mapa_usuario.MapaUsuarioActivity")).apply {
        putExtra("userEmail", userEmail)
    }
    context.startActivity(intent)
}

fun navigateToPerfil(context: Context, userEmail: String) {
    // Utiliza el nombre completo de la actividad de Perfil
    val intent = Intent(context, Class.forName("com.uv.usperfil.PerfilUsuarioActivity")).apply {
        putExtra("userEmail", userEmail)
    }
    context.startActivity(intent)
}

fun navigateToChat(context: Context, userEmail: String) {
    // Utiliza el nombre completo de la actividad de Perfil
    val intent = Intent(context, Class.forName("com.uv.chat.ChatListActivity")).apply {
        putExtra("userEmail", userEmail)
    }
    context.startActivity(intent)
}

// Función para navegar a otros destinos si es necesario
fun navigateToSettings(context: Context, userEmail: String) {
    val intent = Intent(context, Class.forName("com.uv.resumen.MainActivity")).apply {
        putExtra("userEmail", userEmail)
    }
    context.startActivity(intent)
}

fun navigateToResumen(context: Context, userEmail: String, idPrestador: Int) {
    val intent = Intent(context, Class.forName("com.uv.resumen.ResumenPrestadorActivity")).apply {
        putExtra("userEmail", userEmail)
        putExtra("idPrestador", idPrestador)
    }
    context.startActivity(intent)
}

fun navigateToPerfilPrestador(context: Context, userEmail: String, idPrestador: Int) {
    // Utiliza el nombre completo de la actividad de Perfil
    val intent = Intent(context, Class.forName("com.uv.usperfil.PerfilPrestadorActivity")).apply {
        putExtra("userEmail", userEmail)
        putExtra("idPrestador", idPrestador)

    }
    context.startActivity(intent)
}

fun navigateToPrestadorMap(context: Context, userEmail: String, idPrestador: Int) {
    // Utiliza el nombre completo de la actividad de Perfil
    val intent = Intent(context, Class.forName("com.uv.mapa_usuario.MapaPrestadorActivity")).apply {
        putExtra("userEmail", userEmail)
        putExtra("idPrestador", idPrestador)

    }
    context.startActivity(intent)
}

fun navigateToChatPrestador(context: Context, userEmail: String, idPrestador: Int) {
    // Utiliza el nombre completo de la actividad de Perfil
    val intent = Intent(context, Class.forName("com.uv.chat.ChatList2Activity")).apply {
        putExtra("userEmail", userEmail)
        putExtra("idPrestador", idPrestador)
    }
    context.startActivity(intent)
}