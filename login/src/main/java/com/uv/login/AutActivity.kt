package com.uv.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.Credentials
import com.auth0.android.callback.Callback
import com.uv.registro.RegistroActivity
import com.uv.mapa_usuario.MapaUsuarioActivity

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.delay

class AutActivity : AppCompatActivity() {
    private lateinit var account: Auth0
    private lateinit var credentialsManager: CredentialsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = Auth0("wE6Wh4QdN9T4H3rf3CcquLuxHnBxLDvX", "dev-s30qk1z7tdznthon.us.auth0.com")
        val authentication = AuthenticationAPIClient(account)
        val storage = SharedPreferencesStorage(this)
        credentialsManager = CredentialsManager(authentication, storage)
        loginWithBrowser()
    }

    private fun loginWithBrowser() {

        WebAuthProvider.login(account)
            .withScheme("app")
            .withScope("openid profile email")
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(credentials: Credentials) {
                    credentialsManager.saveCredentials(credentials)

                    val jwt = JWT(credentials.idToken!!)
                    val userEmail = jwt.getClaim("email").asString()

                    val intent = Intent(this@AutActivity, MapaUsuarioActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        putExtra("userEmail", userEmail ?: "Email not available")
                    }
                    startActivity(intent)
                    finish()
                }
            })
    }



}
