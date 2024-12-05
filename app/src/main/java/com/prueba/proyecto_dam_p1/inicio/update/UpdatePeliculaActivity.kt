package com.prueba.proyecto_dam_p1.inicio.update

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.databinding.ActivityUpdatePeliculaBinding
import com.prueba.proyecto_dam_p1.inicio.database.Pelicula
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaDatabaseHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UpdatePeliculaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePeliculaBinding
    private lateinit var db: PeliculaDatabaseHelper
    private var selectedImageUri: Uri? = null
    private var peliculaId: Int = -1

    companion object {
        const val IMAGE_PICK_CODE = 1001
    }

    // Método llamado al iniciar la actividad. Configura la interfaz de usuario, inicializa la base de datos y carga los datos de la película.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        initializeDatabase()
        loadPeliculaData()
        setupListeners()
    }

    // Configura la vista de la actividad utilizando View Binding.
    private fun setupUI() {
        binding = ActivityUpdatePeliculaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // Inicializa la base de datos y valida el ID de la película recibido.
    private fun initializeDatabase() {
        db = PeliculaDatabaseHelper(this)
        peliculaId = intent.getIntExtra("pelicula_id", -1)
        if (peliculaId == -1) {
            showToast("ID de película no válido")
            finish()
        }
    }

    // Carga los datos de la película desde la base de datos y los muestra en la interfaz.
    private fun loadPeliculaData() {
        val pelicula = db.getPeliculaById(peliculaId)
        pelicula?.let {
            populateFields(it)
            loadImage(it.imagen)
        }
    }

    // Llena los campos del formulario con los datos de la película.
    private fun populateFields(pelicula: Pelicula) {
        binding.etUpdatedTitle.setText(pelicula.title)
        binding.spUpdatedGenero.setSelection(getSpinnerPosition(pelicula.genero, R.array.genero_array))
        binding.spUpdatedPrioridad.setSelection(getSpinnerPosition(pelicula.prioridad, R.array.prioridad_array))
        binding.etUpdatedFecha.setText(pelicula.fecha ?: "") // Manejamos fecha como opcional
        binding.etUpdatedDescription.setText(pelicula.descripcion)
    }

    // Carga una imagen en la interfaz a partir de un arreglo de bytes.
    private fun loadImage(imageBytes: ByteArray?) {
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val uri = byteArrayToUri(imageBytes)
            selectedImageUri = uri
            binding.ivUpdatedImage.setImageURI(uri)
        } else {
            binding.ivUpdatedImage.setImageResource(R.drawable.error_image)
        }
    }

    // Configura los listeners de los botones de la actividad.
    private fun setupListeners() {
        binding.btnUpdatedImage.setOnClickListener { openGallery() }
        binding.btnUpdatedSave.setOnClickListener { updateMovie() }
        binding.btnUpdatedCancel.setOnClickListener { navigateToInicio() }
    }

    // Navega de regreso a la actividad principal.
    private fun navigateToInicio() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    // Abre la galería para seleccionar una imagen.
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Actualiza los datos de una película en la base de datos.
    private fun updateMovie() {
        val title = binding.etUpdatedTitle.text.toString().trim()
        val genero = binding.spUpdatedGenero.selectedItem.toString()
        val prioridad = binding.spUpdatedPrioridad.selectedItem.toString()
        val fechaInput = binding.etUpdatedFecha.text.toString().trim()
        val descripcion = binding.etUpdatedDescription.text.toString()

        // Validar título
        if (title.isEmpty()) {
            showToast("El título es obligatorio")
            return
        }

        // Validar imagen
        if (selectedImageUri == null) {
            showToast("Selecciona una imagen")
            return
        }

        // Manejo de fecha opcional
        val fecha = if (fechaInput.isEmpty()) null else fechaInput

        // Validar formato del año si se proporciona
        if (fecha != null && !isYearValid(fecha)) {
            showToast("El año ingresado no es válido. Usa el formato yyyy.")
            return
        }

        val imageBytes = uriToByteArray(selectedImageUri!!)
        if (imageBytes == null) {
            showToast("Error al procesar la imagen")
            return
        }

        // Actualizar la película
        val pelicula = Pelicula(
            id = peliculaId,
            title = title,
            genero = genero,
            prioridad = prioridad,
            fecha = fecha,
            descripcion = descripcion,
            imagen = imageBytes
        )

        db.updatePelicula(pelicula)
        showToast("Película actualizada con éxito")
        navigateToInicio()
    }

    // Verifica si un año tiene el formato correcto (yyyy).
    private fun isYearValid(year: String): Boolean {
        return year.matches(Regex("^\\d{4}\$"))
    }

    // Obtiene la posición de un valor en un spinner.
    private fun getSpinnerPosition(value: String, arrayResourceId: Int): Int {
        val array = resources.getStringArray(arrayResourceId)
        return array.indexOf(value)
    }

    // Convierte un arreglo de bytes a un URI para mostrar imágenes.
    private fun byteArrayToUri(imageBytes: ByteArray): Uri? {
        return try {
            val file = File(cacheDir, "temp_image.jpg")
            FileOutputStream(file).use { it.write(imageBytes) }
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Convierte un URI a un arreglo de bytes.
    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Muestra un mensaje emergente en pantalla.
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Maneja el resultado de la selección de imágenes.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            binding.ivUpdatedImage.setImageURI(selectedImageUri)
        }
    }
}
