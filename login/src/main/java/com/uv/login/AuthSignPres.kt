package com.uv.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.jwt.JWT
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.uv.mapa_usuario.MapaUsuarioActivity
import com.uv.registro.RegistroActivity

class AuthSignPres : AppCompatActivity() {
    private lateinit var account: Auth0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = Auth0("wE6Wh4QdN9T4H3rf3CcquLuxHnBxLDvX", "dev-s30qk1z7tdznthon.us.auth0.com")
        signUpWithBrowser()
    }

    private fun signUpWithBrowser() {
        val params = mapOf(
            "screen_hint" to "signup"
        )
        WebAuthProvider.login(account)
            .withScheme("app")
            .withScope("openid profile email")
            .withParameters(params)
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    Log.e("Auth0", "Error durante el registro", exception)
                }

                override fun onSuccess(credentials: Credentials) {
                    val jwt = JWT(credentials.idToken!!)
                    val userEmail = jwt.getClaim("email").asString()

                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString("current_activity", "RegistroActivity")
                        putString("userEmail", userEmail)
                        apply()
                    }

                    val intent = Intent(this@AuthSignPres, RegistroActivity::class.java).apply {
                        putExtra("userEmail", userEmail ?: "Email not available")
                    }
                    startActivity(intent)
                    finish()
                }
            })
    }
}

