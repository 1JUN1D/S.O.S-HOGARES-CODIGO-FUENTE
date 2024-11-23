package com.uv.navegacion
import android.content.Context
import android.content.Intent

interface NavegacionIntentProvider {
    fun createPerfilIntent(context: Context): Intent
    fun createMainIntent(context: Context): Intent
}