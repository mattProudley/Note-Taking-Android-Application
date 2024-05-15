package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonNewNote: Button
    private lateinit var notesDatabaseHelper: NotesDatabaseHelper
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val white = ContextCompat.getColor(this, android.R.color.white)
        toggle.drawerArrowDrawable.color = white

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Handle home click
                    true
                }
                R.id.nav_settings -> {
                    // Handle settings click
                    true
                }
                // Handle other items here
                else -> false
            }
        }

        recyclerView = findViewById(R.id.recyclerView)
        buttonNewNote = findViewById(R.id.buttonNewNote)

        notesDatabaseHelper = NotesDatabaseHelper(this)

        buttonNewNote.setOnClickListener {
            // Start WriteNoteActivity when the button is clicked
            Log.d("MainActivity", "New Note button clicked, starting WriteNoteActivity")
            startActivity(Intent(this, WriteNoteActivity::class.java))
        }

        displayNotes()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list every time the activity resumes
        Log.d("MainActivity", "Activity resumed, refreshing notes list")
        displayNotes()
    }

    private fun displayNotes() {
        val notes = notesDatabaseHelper.getAllNotes()
        Log.d("MainActivity", "Displaying ${notes.size} notes")
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick, ::onDeleteButtonClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    private fun onNoteItemClick(note: Note) {
        Log.d("MainActivity", "Note clicked with ID: ${note.id}")
        val intent = Intent(this, WriteNoteActivity::class.java)
        intent.putExtra("noteId", note.id)
        startActivity(intent)
    }

    private fun onDeleteButtonClick(note: Note) {
        Log.d("MainActivity", "Delete button clicked for note with ID: ${note.id}")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Yes") { _, _ ->
            val deleted = notesDatabaseHelper.deleteNote(note.id)
            if (deleted) {
                Log.d("MainActivity", "Note deleted with ID: ${note.id}")
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                displayNotes() // Refresh the list
            } else {
                Log.e("MainActivity", "Failed to delete note with ID: ${note.id}")
                Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            Log.d("MainActivity", "Delete operation cancelled for note with ID: ${note.id}")
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}
