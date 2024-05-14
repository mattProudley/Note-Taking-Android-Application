package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonNewNote: Button
    private lateinit var notesDatabaseHelper: NotesDatabaseHelper
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        buttonNewNote = findViewById(R.id.buttonNewNote)

        notesDatabaseHelper = NotesDatabaseHelper(this)

        buttonNewNote.setOnClickListener {
            // Start WriteNoteActivity when the button is clicked
            startActivity(Intent(this, WriteNoteActivity::class.java))
        }

        displayNotes()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list every time the activity resumes
        displayNotes()
    }

    private fun displayNotes() {
        val notes = notesDatabaseHelper.getAllNotes()
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick, ::onDeleteButtonClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    private fun onNoteItemClick(note: Note) {
        val intent = Intent(this, WriteNoteActivity::class.java)
        intent.putExtra("noteId", note.id)
        startActivity(intent)
    }

    private fun onDeleteButtonClick(note: Note) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Yes") { _, _ ->
            val deleted = notesDatabaseHelper.deleteNote(note.id)
            if (deleted) {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                displayNotes() // Refresh the list
            } else {
                Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}
