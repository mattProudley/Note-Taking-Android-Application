package com.example.notesapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 3 // Incremented version number
        const val TABLE_NAME = "notes"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_NOTE = "note"
        const val COLUMN_FOLDER = "folder"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the database table when the database is created
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_NOTE TEXT, $COLUMN_FOLDER TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // If upgrading from version 2 to version 3, add the folder column
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_FOLDER TEXT")
        }
    }

    // Function to create a new note in the database
    fun createNote(newTitle: String, newNote: String, newFolder: String?): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, newTitle)
            put(COLUMN_NOTE, newNote)
            put(COLUMN_FOLDER, newFolder)
        }
        val newRowId = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return newRowId
    }

    // Function to read a note from the database
    fun readNote(id: Long): Triple<Long, String, String>? {
        var noteTriple: Triple<Long, String, String>? = null
        val db = readableDatabase
        val query = "SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_NOTE FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(COLUMN_ID)
                val titleIndex = it.getColumnIndex(COLUMN_TITLE)
                val noteIndex = it.getColumnIndex(COLUMN_NOTE)
                if (idIndex != -1 && titleIndex != -1 && noteIndex != -1) {
                    val noteId = it.getLong(idIndex)
                    val title = it.getString(titleIndex)
                    val note = it.getString(noteIndex)
                    noteTriple = Triple(noteId, title, note)
                }
            }
        }
        return noteTriple
    }

    // Function to update a note in the database
    fun updateNote(noteId: Long, newTitle: String, newNote: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, newTitle)
            put(COLUMN_NOTE, newNote)
        }
        val updatedRows = db.update(TABLE_NAME, contentValues, "$COLUMN_ID=?", arrayOf(noteId.toString()))
        db.close()
        return updatedRows > 0
    }

    // Function to update the folder of a note in the database
    fun updateNoteFolder(noteId: Long, newFolder: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_FOLDER, newFolder) // Update folder value
        }
        val updatedRows = db.update(TABLE_NAME, contentValues, "$COLUMN_ID=?", arrayOf(noteId.toString()))
        db.close()
        return updatedRows > 0
    }

    // Function to delete a note from the database
    fun deleteNote(id: Long): Boolean {
        val db = writableDatabase
        val deletedRows = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        return deletedRows > 0
    }

    // Function to search notes by title
    @SuppressLint("Range")
    fun searchNotesByTitle(title: String): List<Note> {
        val notes = mutableListOf<Note>()
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_NOTE FROM $TABLE_NAME WHERE $COLUMN_TITLE LIKE ?"
        val selectionArgs = arrayOf("%$title%")
        val cursor = db.rawQuery(query, selectionArgs)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(COLUMN_ID))
                val noteTitle = it.getString(it.getColumnIndex(COLUMN_TITLE))
                val noteContent = it.getString(it.getColumnIndex(COLUMN_NOTE))
                notes.add(Note(id, noteTitle, noteContent))
            }
        }
        return notes
    }

    // Function to get all notes from the database
    @SuppressLint("Range")
    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val query = "SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_NOTE FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(COLUMN_ID))
                val title = it.getString(it.getColumnIndex(COLUMN_TITLE))
                val note = it.getString(it.getColumnIndex(COLUMN_NOTE))
                notes.add(Note(id, title, note))
            }
        }
        return notes
    }

    // Function to get all distinct folders from the database
    @SuppressLint("Range")
    fun getAllFolders(): List<String> {
        val folders = mutableListOf<String>()
        val db = readableDatabase
        val query = "SELECT DISTINCT $COLUMN_FOLDER FROM $TABLE_NAME WHERE $COLUMN_FOLDER IS NOT NULL"
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val folder = it.getString(it.getColumnIndex(COLUMN_FOLDER))
                if (!folder.isNullOrBlank()) {
                    folders.add(folder)
                }
            }
        }
        return folders
    }

    // Function to get notes by folder from the database
    @SuppressLint("Range")
    fun getNotesByFolder(folderName: String): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val query = "SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_NOTE FROM $TABLE_NAME WHERE $COLUMN_FOLDER = ?"
        val cursor = db.rawQuery(query, arrayOf(folderName))
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(COLUMN_ID))
                val title = it.getString(it.getColumnIndex(COLUMN_TITLE))
                val note = it.getString(it.getColumnIndex(COLUMN_NOTE))
                notes.add(Note(id, title, note))
            }
        }
        return notes
    }
}

