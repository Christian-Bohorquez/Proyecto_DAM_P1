package com.prueba.proyecto_dam_p1.inicio.database

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.prueba.proyecto_dam_p1.inicio.add.AddPeliculaActivity
import com.prueba.proyecto_dam_p1.inicio.principal.InicioActivity
import java.io.File

class PeliculaAdapter (
    private var peliculas: MutableList<Pelicula>,
    private val context: Context
) : RecyclerView.Adapter<PeliculaAdapter.PeliculaViewHolder>() {

    private  val db:PeliculaDatabaseHelper = PeliculaDatabaseHelper(context)

    class PeliculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val generoTextView: TextView = itemView.findViewById(R.id.tv_genero)
        val prioridadTextView: TextView = itemView.findViewById(R.id.tv_prioridad)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButtom)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButtom)
        val imagePreview: ImageView = itemView.findViewById(R.id.image_preview)

    }
    // Acción de crear
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula  , parent, false)
        return PeliculaViewHolder(view)
    }
    // Acción de crear
    override fun onBindViewHolder(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]
        holder.titleTextView.text = pelicula.title
        holder.generoTextView.text = pelicula.genero
        holder.prioridadTextView.text= pelicula.prioridad
        val imageFile = File(pelicula.imagen)
        if (imageFile.exists()) {
            Glide.with(holder.itemView.context)
                .load(imageFile)
                .placeholder(R.drawable.uno)
                .error(R.drawable.borrar)
                .into(holder.imagePreview)
        } else {
            holder.imagePreview.setImageResource(R.drawable.borrar)
        }

        // Acción de editar
        holder.updateButton.setOnClickListener {
            val intent = Intent(context, AddPeliculaActivity::class.java)
            intent.putExtra("peliculaId", pelicula.id) // Pasa el ID de la película para editar
            context.startActivity(intent)
        }

        // Acción de eliminar
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Eliminar Película")
                .setMessage("¿Estás seguro de que deseas eliminar esta película?")
                .setPositiveButton("Sí") { _, _ ->
                    val position = holder.adapterPosition
                    val pelicula = peliculas[position]

                    // Eliminar de la base de datos
                    val isDeleted = db.deletePelicula(pelicula.id)

                    if (isDeleted) {
                        // Eliminar de la lista y notificar al adaptador
                        peliculas.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Película eliminada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

    }

    // Acción de filtro
    fun resetData(originalData: List<Pelicula>) {
        peliculas = originalData.toMutableList()
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return peliculas.size
    }

}

