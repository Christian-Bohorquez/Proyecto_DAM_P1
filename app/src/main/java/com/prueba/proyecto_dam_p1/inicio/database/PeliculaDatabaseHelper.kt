package com.prueba.proyecto_dam_p1.inicio.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class PeliculaDatabaseHelper (context: Context): SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "peli.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "laspeliculas"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_GENERO = "genero"
        private const val COLUMN_PRIORIDAD = "prioridad"
        private const val COLUMN_FECHA ="fecha"
        private const val COLUMN_DESCRIPCION = "descripcion"
        private const val  COLUMN_IMAGEN ="imagen"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $COLUMN_TITLE TEXT, $COLUMN_GENERO TEXT," +
                " $COLUMN_PRIORIDAD TEXT, $COLUMN_DESCRIPCION TEXT, $COLUMN_FECHA TEXT, $COLUMN_IMAGEN TEXT)"

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)

    }
    fun insertPelicula(pelicula: Pelicula){
        val db = writableDatabase
        val values = ContentValues ().apply{
            put(COLUMN_TITLE, pelicula.title)
            put(COLUMN_GENERO, pelicula.genero)
            put(COLUMN_PRIORIDAD, pelicula.prioridad)
            put(COLUMN_FECHA, pelicula.fecha)
            put(COLUMN_DESCRIPCION, pelicula.descripcion)
            put(COLUMN_IMAGEN, pelicula.imagen)
        }
        try {
            db.insert(TABLE_NAME, null, values)
        } finally {
            db.close()
        }
    }
    fun deletePelicula(id: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted > 0
    }

    fun getAllPelicula(genero: String? = null, prioridad: String? = null): List<Pelicula>{
        val peliculaList = mutableListOf<Pelicula>()
        val db = readableDatabase
        var query = "SELECT * FROM $TABLE_NAME  WHERE 1=1"
        val selectionArgs = mutableListOf<String>()


        if (!genero.isNullOrEmpty()) {
            query += " AND $COLUMN_GENERO = ?"
            selectionArgs.add(genero)
        }
        if (!prioridad.isNullOrEmpty()) {
            query += " AND $COLUMN_PRIORIDAD = ?"
            selectionArgs.add(prioridad)
        }
        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val genero = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO))
                val prioridad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORIDAD))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION))
                val imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))

                val pelicula = Pelicula(id, title, genero, prioridad, fecha, imagen, descripcion)
                peliculaList.add(pelicula)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return peliculaList
    }
}
