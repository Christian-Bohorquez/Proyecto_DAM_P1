package com.prueba.proyecto_dam_p1.inicio.add

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.databinding.ActivityAddPeliculaBinding
import com.prueba.proyecto_dam_p1.inicio.database.Pelicula
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaDatabaseHelper
import java.io.IOException

class AddPeliculaActivity : AppCompatActivity() {

    // View Binding y Helpers
    private lateinit var binding: ActivityAddPeliculaBinding
    private lateinit var db: PeliculaDatabaseHelper
    private var selectedImageUri: Uri? = null

    // Constantes
    companion object {
        private const val IMAGE_PICK_CODE = 100
    }

    // Configura la actividad principal, inicializa la base de datos y listeners
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPeliculaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de la base de datos
        db = PeliculaDatabaseHelper(this)

        // Configuración de listeners
        setupListeners()
    }

    // Configura los eventos para botones de la interfaz
    private fun setupListeners() {
        binding.btnImagen.setOnClickListener { openGalleryWithPermissions() }
        binding.btnGuardar.setOnClickListener { saveMovie() }
        binding.btnCancelar.setOnClickListener { navigateToInicio() }
    }

    // Cierra la actividad y regresa al inicio
    private fun navigateToInicio() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    // Maneja los permisos y abre la galería para seleccionar una imagen
    private fun openGalleryWithPermissions() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q &&
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_PICK_CODE)
        } else {
            openGallery()
        }
    }

    // Abre la galería para seleccionar una imagen
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Valida que el año sea válido o esté vacío
    private fun isYearValid(year: String): Boolean {
        return year.isEmpty() || year.matches(Regex("^\\d{4}\$"))
    }

    // Verifica que el título no esté vacío o sea solo espacios en blanco
    private fun isTitleValid(title: String): Boolean {
        return title.isNotBlank()
    }

    // Guarda la información de la película en la base de datos
    private fun saveMovie() {
        val title = binding.etTitle.text.toString().trim()
        val genero = binding.spGenero.selectedItem.toString()
        val prioridad = binding.spPrioridad.selectedItem.toString()
        val fecha = binding.etFecha.text.toString()
        val descripcion = binding.etDescripcion.text.toString()

        // Validaciones
        if (!isTitleValid(title)) {
            showToast("El título es obligatorio")
            return
        }

        if (!isYearValid(fecha)) {
            showToast("Si proporciona una fecha, debe ser un año válido (formato: yyyy)")
            return
        }

        val imageBytes = when {
            selectedImageUri != null -> uriToByteArray(selectedImageUri!!) ?: getDefaultImageBytes()
            else -> getDefaultImageBytes()
        }

        // Crear y guardar la película
        val pelicula = Pelicula(
            id = 0,
            title = title,
            genero = genero,
            prioridad = prioridad,
            fecha = if (fecha.isEmpty()) null else fecha,
            descripcion = descripcion,
            imagen = imageBytes
        )

        db.insertPelicula(pelicula)
        showToast("Película guardada con éxito")
        navigateToInicio()
    }

    // Convierte una URI de imagen a un arreglo de bytes
    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            e.printStackTrace()
            getErrorImageBytes()
        }
    }

    // Obtiene la imagen predeterminada como un arreglo de bytes
    private fun getDefaultImageBytes(): ByteArray {
        return getDrawableBytes(R.drawable.popcorn)
    }

    // Obtiene la imagen de error como un arreglo de bytes
    private fun getErrorImageBytes(): ByteArray {
        return getDrawableBytes(R.drawable.error_image)
    }

    // Convierte un drawable en un arreglo de bytes
    private fun getDrawableBytes(drawableId: Int): ByteArray {
        return resources.openRawResource(drawableId).use { it.readBytes() }
    }

    // Maneja el resultado de la selección de imagen en la galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                binding.imagePreview.setImageURI(it)
                selectedImageUri = it
            } ?: showToast("No se seleccionó ninguna imagen")
        }
    }

    // Muestra un mensaje en pantalla como un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
