package com.prueba.proyecto_dam_p1.inicio.principal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.databinding.ActivityInicioBinding
import com.prueba.proyecto_dam_p1.inicio.add.AddPeliculaActivity
import com.prueba.proyecto_dam_p1.inicio.database.Pelicula
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaAdapter
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaDatabaseHelper

class InicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInicioBinding
    private lateinit var db: PeliculaDatabaseHelper
    private lateinit var peliculaAdapter: PeliculaAdapter
    private lateinit var peliculasList: MutableList<Pelicula>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = PeliculaDatabaseHelper(this)
        peliculasList = db.getAllPelicula().toMutableList()
        peliculaAdapter = PeliculaAdapter(peliculasList, this)

        binding.moviesGrid.layoutManager = GridLayoutManager(this, 2)
        binding.moviesGrid.adapter = peliculaAdapter

        setupFilterSpinners()

        // Configurar el botón para añadir nuevas películas
        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddPeliculaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupFilterSpinners() {

        binding.filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = parent?.getItemAtPosition(position).toString()

                val optionsArray = when (selectedType) {
                    "Género" -> R.array.genero_array
                    "Prioridad" -> R.array.prioridad_array
                    else -> null
                }

                optionsArray?.let {
                    ArrayAdapter.createFromResource(
                        this@InicioActivity,
                        it,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.filterOptionsSpinner.adapter = adapter
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        binding.filterOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                val selectedType = binding.filterTypeSpinner.selectedItem.toString()

                val filteredMovies = when (selectedType) {
                    "Género" -> db.getAllPelicula(genero = selectedOption)
                    "Prioridad" -> db.getAllPelicula(prioridad = selectedOption)
                    else -> peliculasList
                }

                peliculaAdapter.resetData(filteredMovies)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}