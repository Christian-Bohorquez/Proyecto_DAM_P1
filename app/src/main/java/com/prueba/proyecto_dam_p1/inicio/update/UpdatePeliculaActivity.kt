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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePeliculaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = PeliculaDatabaseHelper(this)

        // Obtener el ID de la película a actualizar
        peliculaId = intent.getIntExtra("pelicula_id", -1)
        if (peliculaId == -1) {
            Toast.makeText(this, "ID de película no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar datos de la película
        val pelicula = db.getPeliculaById(peliculaId)
        if (pelicula != null) {
            binding.etUpdatedTitle.setText(pelicula.title)
            binding.spUpdatedGenero.setSelection(getSpinnerPosition(pelicula.genero, R.array.genero_array))
            binding.spUpdatedPrioridad.setSelection(getSpinnerPosition(pelicula.prioridad, R.array.prioridad_array))
            binding.etUpdatedFecha.setText(pelicula.fecha)
            binding.etUpdatedDescription.setText(pelicula.descripcion)

            // Mostrar la imagen si existe
            if (pelicula.imagen != null && pelicula.imagen.isNotEmpty()) {
                val uri = byteArrayToUri(pelicula.imagen)
                selectedImageUri = uri
                binding.ivUpdatedImage.setImageURI(uri)
            } else {
                binding.ivUpdatedImage.setImageResource(R.drawable.error_image) // Imagen por defecto
            }
        }

        // Listener para seleccionar nueva imagen
        binding.btnUpdatedImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Listener para guardar la película (opcionalidad de la imagen manejada)
        binding.btnUpdatedSave.setOnClickListener { updateMovie() }

        binding.btnUpdatedCancel.setOnClickListener {
            navegationToInicio()
        }
    }

    private fun navegationToInicio() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    private fun updateMovie() {
        val title = binding.etUpdatedTitle.text.toString()
        val genero = binding.spUpdatedGenero.selectedItem.toString()
        val prioridad = binding.spUpdatedPrioridad.selectedItem.toString()
        val fecha = binding.etUpdatedFecha.text.toString()
        val descripcion = binding.etUpdatedDescription.text.toString()

        // Convertir el Uri de la imagen seleccionada a bytes si existe
        val imageBytes = selectedImageUri?.let { uriToByteArray(it) }

        val updatedPelicula = Pelicula(
            id = peliculaId,
            title = title,
            genero = genero,
            prioridad = prioridad,
            fecha = fecha,
            descripcion = descripcion,
            imagen = imageBytes // Se permite que sea nulo
        )

        db.updatePelicula(updatedPelicula) // Actualizar en la base de datos
        Toast.makeText(this, "Película actualizada con éxito", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            binding.ivUpdatedImage.setImageURI(selectedImageUri)
        }
    }

    private fun byteArrayToUri(imageBytes: ByteArray): Uri? {
        return try {
            val file = File(cacheDir, "temp_image.jpg")
            val fos = FileOutputStream(file)
            fos.write(imageBytes)
            fos.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getSpinnerPosition(value: String, arrayResourceId: Int): Int {
        val array = resources.getStringArray(arrayResourceId)
        return array.indexOf(value)
    }
}
