package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
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

        notesDatabaseHelper = NotesDatabaseHelper(this) // Initialize notesDatabaseHelper

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val white = ContextCompat.getColor(this, android.R.color.white)
        toggle.drawerArrowDrawable.color = white

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        populateNavigationView(navigationView) // Populate navigation view with folders

        // Set a listener for drawer open event
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                // Repopulate navigation view when the drawer is opened
                populateNavigationView(navigationView)
            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                showSearchDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showSearchDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Search")
            .setView(editText)
            .setPositiveButton("Search") { dialog, _ ->
                val searchText = editText.text.toString()
                displayNotes(searchText) // Trigger displayNotes with search text
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list every time the activity resumes
        Log.d("MainActivity", "Activity resumed, refreshing notes list")
        displayNotes()
    }

    private fun displayNotes(searchText: String? = null) {
        val notes = if (searchText.isNullOrEmpty()) {
            notesDatabaseHelper.getAllNotes()
        } else {
            notesDatabaseHelper.searchNotesByTitle(searchText)
        }
        Log.d("MainActivity", "Displaying ${notes.size} notes")
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick, ::onDeleteButtonClick, ::onUpdateFolderClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    // Function to populate NavigationView with folders
    private fun populateNavigationView(navigationView: NavigationView) {
        val menu = navigationView.menu
        val folders = notesDatabaseHelper.getAllFolders() // Retrieve folders from database

        // Clear existing menu items
        menu.clear()

        // Add folders as menu items
        folders.forEachIndexed { index, folder ->
            menu.add(Menu.NONE, index, Menu.NONE, folder)
                .setIcon(R.drawable.ic_folder) // Set folder icon
        }

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selection here
            when (menuItem.itemId) {
                else -> {
                    // Handle folder item click
                    val folder = folders[menuItem.itemId]
                    // Implement your logic to display notes for the selected folder
                    true
                }
            }
        }
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

    private fun onUpdateFolderClick(note: Note) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Enter New Folder Name")

        val input = EditText(this)
        alertDialog.setView(input)

        alertDialog.setPositiveButton("OK") { _, _ ->
            val folderName = input.text.toString()
            val noteId = note.id // Assuming note has an id property
            val success = notesDatabaseHelper.updateNoteFolder(noteId, folderName)

            if (success) {
                Toast.makeText(this, "Folder updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update folder", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        alertDialog.show()
    }
}
