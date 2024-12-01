package com.prueba.proyecto_dam_p1.inicio.add

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.prueba.proyecto_dam_p1.databinding.ActivityAddPeliculaBinding
import com.prueba.proyecto_dam_p1.inicio.database.Pelicula
import com.prueba.proyecto_dam_p1.inicio.database.PeliculaDatabaseHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.util.Locale
import android.Manifest

class AddPeliculaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPeliculaBinding
    private lateinit var db: PeliculaDatabaseHelper
    private var selectedImageUri: Uri? = null


    companion object {
        private const val IMAGE_PICK_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPeliculaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = PeliculaDatabaseHelper(this)
        binding.btnImagen.setOnClickListener {
            openGalleryWithPermissions()
        }
        binding.btnGuardar.setOnClickListener {
            saveMovie()
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openGalleryWithPermissions() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    IMAGE_PICK_CODE
                )
            } else {
                openGallery()
            }
        } else {
            openGallery()
        }
    }

    private fun isDateValid(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: ParseException) {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                binding.imagePreview.setImageURI(imageUri)
                selectedImageUri = imageUri
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val file = File(filesDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveMovie() {
        val title = binding.etTitle.text.toString()
        val genero = binding.spGenero.selectedItem.toString()
        val prioridad = binding.spPrioridad.selectedItem.toString()
        val fecha = binding.etFecha.text.toString()
        val descripcion = binding.etDescripcion.text.toString()

        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir la imagen seleccionada a un arreglo de bytes
        val imageBytes = uriToByteArray(selectedImageUri!!)
        if (imageBytes == null) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isDateValid(fecha)) {
            Toast.makeText(
                this,
                "La fecha ingresada no es válida. Usa el formato dd/MM/yyyy.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Crear el objeto Pelicula con el arreglo de bytes para la imagen
        val pelicula = Pelicula(
            id = 0,
            title = title,
            genero = genero,
            prioridad = prioridad,
            fecha = fecha,
            descripcion = descripcion,
            imagen = imageBytes
        )

        db.insertPelicula(pelicula)

        Toast.makeText(this, "Película guardada con éxito", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    // Método para convertir un Uri a ByteArray
    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}