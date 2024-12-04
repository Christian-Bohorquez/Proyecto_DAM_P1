package com.prueba.proyecto_dam_p1.inicio.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Clase que gestiona la base de datos SQLite para las películas.
class PeliculaDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        // Nombre y versión de la base de datos.
        private const val DATABASE_NAME = "peli.db"
        private const val DATABASE_VERSION = 1

        // Constantes para la tabla y sus columnas.
        private const val TABLE_NAME = "laspeliculas"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_GENERO = "genero"
        private const val COLUMN_PRIORIDAD = "prioridad"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_DESCRIPCION = "descripcion"
        private const val COLUMN_IMAGEN = "imagen"
    }

    // Método que se llama cuando se crea la base de datos por primera vez.
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createTableQuery()) // Ejecuta la consulta para crear la tabla.
    }

    // Método que se llama cuando se actualiza la versión de la base de datos.
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(dropTableQuery()) // Elimina la tabla existente.
        onCreate(db) // Crea una nueva tabla.
    }

    // Devuelve la consulta SQL para crear la tabla.
    private fun createTableQuery(): String {
        return """
            CREATE TABLE $TABLE_NAME(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_GENERO TEXT,
                $COLUMN_PRIORIDAD TEXT,
                $COLUMN_DESCRIPCION TEXT,
                $COLUMN_FECHA TEXT,
                $COLUMN_IMAGEN BLOB
            )
        """
    }

    // Devuelve la consulta SQL para eliminar la tabla.
    private fun dropTableQuery(): String {
        return "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    // Inserta una película en la base de datos.
    fun insertPelicula(pelicula: Pelicula): Boolean {
        val db = writableDatabase
        return try {
            // Inserta los valores de la película en la tabla y verifica si tuvo éxito.
            db.insert(TABLE_NAME, null, peliculaToContentValues(pelicula)) > 0
        } finally {
            db.close()
        }
    }

    // Elimina una película por su ID.
    fun deletePelicula(id: Int): Boolean {
        val db = writableDatabase
        return try {
            // Elimina la película con el ID especificado y verifica si tuvo éxito.
            db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
        } finally {
            db.close()
        }
    }

    // Actualiza una película en la base de datos.
    fun updatePelicula(pelicula: Pelicula): Boolean {
        val db = writableDatabase
        return try {
            // Actualiza los valores de la película y verifica si tuvo éxito.
            db.update(
                TABLE_NAME,
                peliculaToContentValues(pelicula),
                "$COLUMN_ID = ?",
                arrayOf(pelicula.id.toString())
            ) > 0
        } finally {
            db.close()
        }
    }

    // Devuelve una lista de películas que cumplen con los filtros especificados.
    fun getAllPelicula(
        titulo: String? = null,
        genero: String? = null,
        prioridad: String? = null
    ): List<Pelicula> {
        val query = buildFilterQuery(titulo, genero, prioridad) // Construye la consulta SQL.
        val args = buildFilterArgs(titulo, genero, prioridad) // Construye los argumentos para la consulta.
        return executePeliculaQuery(query, args) // Ejecuta la consulta y devuelve los resultados.
    }

    // Devuelve una película por su ID.
    fun getPeliculaById(id: Int): Pelicula? {
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val args = arrayOf(id.toString())
        return executePeliculaQuery(query, args).firstOrNull() // Devuelve la primera película encontrada o null.
    }

    // Convierte un objeto Pelicula a un objeto ContentValues para la base de datos.
    private fun peliculaToContentValues(pelicula: Pelicula): ContentValues {
        return ContentValues().apply {
            put(COLUMN_TITLE, pelicula.title)
            put(COLUMN_GENERO, pelicula.genero)
            put(COLUMN_PRIORIDAD, pelicula.prioridad)
            put(COLUMN_FECHA, pelicula.fecha)
            put(COLUMN_DESCRIPCION, pelicula.descripcion)
            put(COLUMN_IMAGEN, pelicula.imagen)
        }
    }

    // Construye la consulta SQL con los filtros aplicados.
    private fun buildFilterQuery(
        titulo: String?,
        genero: String?,
        prioridad: String?
    ): String {
        val query = StringBuilder("SELECT * FROM $TABLE_NAME WHERE 1=1") // Base de la consulta.
        if (!titulo.isNullOrEmpty()) query.append(" AND $COLUMN_TITLE LIKE ?") // Agrega filtro por título.
        if (!genero.isNullOrEmpty()) query.append(" AND $COLUMN_GENERO = ?") // Agrega filtro por género.
        if (!prioridad.isNullOrEmpty()) query.append(" AND $COLUMN_PRIORIDAD = ?") // Agrega filtro por prioridad.
        return query.toString()
    }

    // Construye los argumentos para los filtros.
    private fun buildFilterArgs(
        titulo: String?,
        genero: String?,
        prioridad: String?
    ): Array<String> {
        val args = mutableListOf<String>()
        if (!titulo.isNullOrEmpty()) args.add("%$titulo%") // Agrega argumento para el título.
        if (!genero.isNullOrEmpty()) args.add(genero) // Agrega argumento para el género.
        if (!prioridad.isNullOrEmpty()) args.add(prioridad) // Agrega argumento para la prioridad.
        return args.toTypedArray()
    }

    // Ejecuta una consulta SQL y devuelve una lista de objetos Pelicula.
    private fun executePeliculaQuery(query: String, args: Array<String>? = null): List<Pelicula> {
        val db = readableDatabase
        val peliculas = mutableListOf<Pelicula>()
        val cursor = db.rawQuery(query, args) // Ejecuta la consulta.

        try {
            // Itera sobre los resultados y los convierte en objetos Pelicula.
            while (cursor.moveToNext()) {
                peliculas.add(cursorToPelicula(cursor))
            }
        } finally {
            cursor.close() // Cierra el cursor.
            db.close() // Cierra la base de datos.
        }

        return peliculas
    }

    // Convierte un cursor de base de datos en un objeto Pelicula.
    private fun cursorToPelicula(cursor: Cursor): Pelicula {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
        val genero = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO))
        val prioridad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORIDAD))
        val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
        val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION))
        val imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))
        return Pelicula(id, title, descripcion, genero, prioridad, fecha, imagen)
    }
}
