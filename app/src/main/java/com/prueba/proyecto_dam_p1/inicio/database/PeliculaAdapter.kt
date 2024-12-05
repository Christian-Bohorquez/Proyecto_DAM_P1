package com.prueba.proyecto_dam_p1.inicio.database

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prueba.proyecto_dam_p1.R
import com.prueba.proyecto_dam_p1.inicio.principal.InicioActivity
import com.prueba.proyecto_dam_p1.inicio.update.UpdatePeliculaActivity

class PeliculaAdapter(
    private var peliculas: MutableList<Pelicula>,
    private val context: Context
) : RecyclerView.Adapter<PeliculaAdapter.PeliculaViewHolder>() {

    private val db: PeliculaDatabaseHelper = PeliculaDatabaseHelper(context)

    /**
     * ViewHolder para manejar los elementos de la vista.
     */
    class PeliculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val generoTextView: TextView = itemView.findViewById(R.id.tv_genero)
        val prioridadTextView: TextView = itemView.findViewById(R.id.tv_prioridad)
        val updateButton: ImageView = itemView.findViewById(R.id.btnUpdate)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButtom)
        val imagePreview: ImageView = itemView.findViewById(R.id.image_preview)
    }

    /**
     * Crea la vista para el ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        return PeliculaViewHolder(view)
    }

    /**
     * Configura los datos del ViewHolder y los listeners.
     */
    override fun onBindViewHolder(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]
        bindData(holder, pelicula)
        setupUpdateButton(holder, pelicula)
        setupDeleteButton(holder, position)
    }

    /**
     * Vincula los datos de una película al ViewHolder.
     */
    private fun bindData(holder: PeliculaViewHolder, pelicula: Pelicula) {
        holder.titleTextView.text = pelicula.title
        holder.generoTextView.text = pelicula.genero
        holder.prioridadTextView.text = pelicula.prioridad

        val imageData = pelicula.imagen
        if (imageData != null && imageData.isNotEmpty()) {
            Glide.with(context)
                .load(imageData)
                .placeholder(R.drawable.popcorn)
                .into(holder.imagePreview)
        } else {
            holder.imagePreview.setImageResource(R.drawable.error_image)
        }
    }

    /**
     * Configura el botón de actualización.
     * Cuando el usuario actualiza una película, regresa a InicioActivity y actualiza la lista automáticamente.
     */
    private fun setupUpdateButton(holder: PeliculaViewHolder, pelicula: Pelicula) {
        holder.updateButton.setOnClickListener {
            val intent = Intent(context, UpdatePeliculaActivity::class.java).apply {
                putExtra("pelicula_id", pelicula.id)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Configura el botón de eliminación.
     */
    private fun setupDeleteButton(holder: PeliculaViewHolder, position: Int) {
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(holder, position)
        }
    }

    /**
     * Muestra un cuadro de diálogo de confirmación para eliminar una película.
     */
    private fun showDeleteConfirmationDialog(holder: PeliculaViewHolder, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Película")
            .setMessage("¿Estás seguro de que deseas eliminar esta película?")
            .setPositiveButton("Sí") { _, _ -> deletePelicula(holder, position) }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Elimina una película de la base de datos y actualiza la lista.
     */
    private fun deletePelicula(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]
        if (db.deletePelicula(pelicula.id)) {
            peliculas.removeAt(position)
            notifyItemRemoved(position)
            refreshData(db.getAllPelicula())
            Toast.makeText(context, "Película eliminada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Actualiza la lista de películas con nuevos datos.
     */
    fun refreshData(newPeliculas: List<Pelicula>) {
        peliculas = newPeliculas.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Restablece la lista de películas al estado original.
     */
    fun resetData(originalData: List<Pelicula>) {
        peliculas = originalData.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Devuelve el número de elementos en la lista.
     */
    override fun getItemCount(): Int {
        return peliculas.size
    }
}
