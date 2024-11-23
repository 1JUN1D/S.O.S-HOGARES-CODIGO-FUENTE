package com.uv.sos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.uv.chat.ChatActivity
import com.uv.chat.Chat2Activity

import com.uv.login.InicioActivity
import com.uv.mapa_usuario.MapaUsuarioActivity
import com.uv.mapa_usuario.UbicacionActivity
import com.uv.chat.ChatListActivity
import com.uv.mapa_usuario.PedidoCerrajeroActivity
import com.uv.mapa_usuario.PedidoElectricistaActivity
import com.uv.mapa_usuario.PedidoLimpiezaActivity
import com.uv.resumen.ResumenPrestadorActivity
import com.uv.resumen.MainActivity
import com.uv.usperfil.EditarFotoActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, InicioActivity::class.java)
        startActivity(intent)
        finish()

    }
}