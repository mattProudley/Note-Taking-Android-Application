package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    private fun onNoteItemClick(note: Note) {
        val intent = Intent(this, WriteNoteActivity::class.java)
        intent.putExtra("noteId", note.id)
        startActivity(intent)
    }
}
