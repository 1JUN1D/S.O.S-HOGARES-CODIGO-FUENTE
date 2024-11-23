package com.uv.login

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.callback.Callback
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage

class LogoutManager(private val context: Context, private val account: Auth0) {
    private val credentialsManager: CredentialsManager

    init {
        val authentication = AuthenticationAPIClient(account)
        val storage = SharedPreferencesStorage(context)
        credentialsManager = CredentialsManager(authentication, storage)
    }

    fun logout() {
        WebAuthProvider.logout(account)
            .withScheme("app")
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    // Limpiar las credenciales guardadas
                    credentialsManager.clearCredentials()

                    // Mostrar mensaje de cierre de sesión exitoso
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                    // Redirigir a InicioActivity
                    val intent = Intent(context, InicioActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    context.startActivity(intent)
                }

                override fun onFailure(exception: AuthenticationException) {
                    // Manejar error al cerrar sesión
                    Toast.makeText(context, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
            })
    }
}