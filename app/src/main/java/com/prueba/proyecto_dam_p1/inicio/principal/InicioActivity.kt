package com.prueba.proyecto_dam_p1.inicio.principal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.databinding.ActivityInicioBinding
import com.prueba.proyecto_dam_p1.inicio.add.AddPeliculaActivity
import com.prueba.proyecto_dam_p1.inicio.database.Pelicula
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaAdapter
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaDatabaseHelper

class InicioActivity : AppCompatActivity() {

    // Declaración de variables para el enlace de vistas, la base de datos, el adaptador y la lista de películas
    private lateinit var binding: ActivityInicioBinding
    private lateinit var db: PeliculaDatabaseHelper
    private lateinit var peliculaAdapter: PeliculaAdapter
    private lateinit var peliculasList: MutableList<Pelicula>

    private var currentFilterType: String? = null
    private var currentFilterOption: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa el enlace de vistas con el diseño XML correspondiente
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración inicial del Activity
        initializeDatabase()
        initializeRecyclerView()
        setupListeners()

    }

    // Inicializa la base de datos y obtiene todas las películas guardadas
    private fun initializeDatabase() {
        db = PeliculaDatabaseHelper(this)
        peliculasList = db.getAllPelicula().toMutableList()
    }



    // Configura el RecyclerView con un GridLayoutManager y el adaptador de películas
    private fun initializeRecyclerView() {
        peliculaAdapter = PeliculaAdapter(peliculasList, this)
        binding.moviesGrid.apply {
            layoutManager = GridLayoutManager(this@InicioActivity, 2) // Diseño de cuadrícula con 2 columnas
            adapter = peliculaAdapter
        }
    }

    // Configura los listeners para los elementos interactivos
    private fun setupListeners() {
        setupSearchView()
        setupFilterSpinners()
        setupAddButton()
    }

    // Configura el SearchView para realizar búsquedas dinámicas en tiempo real
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterMoviesByTitle(it) } // Filtra las películas según el título
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterMoviesByTitle(it) } // Filtra mientras el texto cambia
                return true
            }
        })
    }

    // Filtra las películas de la lista según el título proporcionado
    private fun filterMoviesByTitle(query: String) {
        val filteredMovies = peliculasList.filter {
            it.title.contains(query, ignoreCase = true)
        }
        peliculaAdapter.resetData(filteredMovies) // Actualiza los datos en el adaptador
    }

    // Configura los Spinners para aplicar filtros a las películas
    private fun setupFilterSpinners() {
        setupFilterTypeSpinner()
        setupFilterOptionsSpinner()
    }

    // Configura el Spinner para seleccionar el tipo de filtro
    private fun setupFilterTypeSpinner() {
        binding.filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                updateFilterOptionsSpinner(selectedType) // Actualiza las opciones según el tipo
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Actualiza las opciones del segundo Spinner según el tipo de filtro seleccionado
    private fun updateFilterOptionsSpinner(selectedType: String) {
        val optionsArray = when (selectedType) {
            "Género" -> R.array.genero_array // Opciones para género
            "Prioridad" -> R.array.prioridad_array // Opciones para prioridad
            else -> null
        }

        // Establece las opciones en el Spinner
        optionsArray?.let {
            ArrayAdapter.createFromResource(
                this,
                it,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.filterOptionsSpinner.adapter = adapter
            }
        }
    }

    // Configura el Spinner para seleccionar una opción de filtro específica
    private fun setupFilterOptionsSpinner() {
        binding.filterOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                applyFilter(selectedOption) // Aplica el filtro seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Aplica el filtro seleccionado a las películas según el tipo y la opción
    private fun applyFilter(selectedOption: String) {
        val selectedType = binding.filterTypeSpinner.selectedItem.toString()

        currentFilterType = selectedType
        currentFilterOption = selectedOption

        val filteredMovies = when (selectedType) {
            "Género" -> db.getAllPelicula(genero = selectedOption)
            "Prioridad" -> db.getAllPelicula(prioridad = selectedOption)
            else -> peliculasList
        }
        peliculaAdapter.resetData(filteredMovies) // Actualiza el adaptador con los datos filtrados
    }

    //Método para aplicar el filtro actual cuando se refresque la lista
    private fun refreshFilteredMovies() {
        if (currentFilterType != null && currentFilterOption != null) {
            applyFilter(currentFilterOption!!)
        } else {
            showAllMovies() // Si no hay filtro, muestra todos los datos
        }
    }

    // Método para refrescar y mostrar toda la lista de películas
    private fun showAllMovies() {
        val updatedMovies = db.getAllPelicula() // Obtén los datos actualizados desde la base de datos
        peliculaAdapter.resetData(updatedMovies) // Actualiza el adaptador con los nuevos datos
    }


    // Configura el botón para agregar una nueva película
    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            navigateToAddPeliculaActivity() // Navega a la actividad para agregar una película
        }
    }

    // Navega a la actividad AddPeliculaActivity
    private fun navigateToAddPeliculaActivity() {
        val intent = Intent(this, AddPeliculaActivity::class.java)
        startActivity(intent)
    }

    // Método llamado cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        // Refresca la lista de películas al reanudar la actividad
        refreshFilteredMovies()
    }

    // Refresca la lista de películas según los datos actuales en la base de datos
    // Se actualizo la forma en la que se aplicaria este metodo, ahora es innecesario pero no se borrara
    private fun refreshMovieList() {
        peliculasList.apply {
            clear()
            addAll(db.getAllPelicula()) // Obtén todos los datos actualizados
        }
        peliculaAdapter.notifyDataSetChanged() // Notifica cambios al adaptador
    }
}
