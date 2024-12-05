package com.prueba.proyecto_dam_p1.inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.inicio.principal.InicioActivity

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        val btnNextApp = findViewById<Button>(R.id.btnNext)
        btnNextApp.setOnClickListener {
            navegarToInicioApp()
        }
    }

    private fun navegarToInicioApp() {
        val intent = Intent(this, InicioActivity::class.java)
        startActivity(intent)
        finish()
    }
}