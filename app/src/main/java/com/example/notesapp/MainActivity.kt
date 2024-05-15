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

    // Variables for navigation drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    // Variables for RecyclerView and other UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonNewNote: Button
    private lateinit var notesDatabaseHelper: NotesDatabaseHelper
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize NotesDatabaseHelper to handle database operations
        notesDatabaseHelper = NotesDatabaseHelper(this)

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set drawer arrow color to white
        val white = ContextCompat.getColor(this, android.R.color.white)
        toggle.drawerArrowDrawable.color = white

        // Populate navigation view with folders
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        populateNavigationView(navigationView)

        // Set a listener for drawer open event to repopulate navigation view
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                populateNavigationView(navigationView)
            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })

        // Set up RecyclerView and button
        recyclerView = findViewById(R.id.recyclerView)
        buttonNewNote = findViewById(R.id.buttonNewNote)

        // Initialize NotesDatabaseHelper
        notesDatabaseHelper = NotesDatabaseHelper(this)

        // Start WriteNoteActivity when the button is clicked
        buttonNewNote.setOnClickListener {
            Log.d("MainActivity", "New Note button clicked")
            startActivity(Intent(this, WriteNoteActivity::class.java))
        }

        // Display all notes when the activity starts
        displayAllNotes()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list every time the activity resumes
        Log.d("MainActivity", "Refreshing notes list")
        displayAllNotes()
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

    // Function to show search dialog
    private fun showSearchDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Search")
            .setView(editText)
            .setPositiveButton("Search") { dialog, _ ->
                val searchText = editText.text.toString()
                displayAllNotes(searchText)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    // Function to display all notes
    private fun displayAllNotes(searchText: String? = null) {
        val notes = if (searchText.isNullOrEmpty()) {
            // If no search text provided, get all notes from the database
            notesDatabaseHelper.getAllNotes()
        } else {
            // If search text provided, search notes by title
            notesDatabaseHelper.searchNotesByTitle(searchText)
        }
        Log.d("MainActivity", "Displaying ${notes.size} notes")
        // Set up RecyclerView with all notes
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick, ::onDeleteButtonClick, ::onUpdateFolderClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Function to display notes by folder
    private fun displayNotesByFolder(folderName: String) {
        // Get notes from the database by folder
        val notes = notesDatabaseHelper.getNotesByFolder(folderName)
        Log.d("MainActivity", "Displaying ${notes.size} notes for folder: $folderName")
        // Set up RecyclerView with notes from the folder
        noteAdapter = NoteAdapter(notes, ::onNoteItemClick, ::onDeleteButtonClick, ::onUpdateFolderClick)
        recyclerView.adapter = noteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Function to handle click on a note
    private fun onNoteItemClick(note: Note) {
        // Open WriteNoteActivity with the clicked note
        Log.d("MainActivity", "Note clicked with ID: ${note.id}")
        val intent = Intent(this, WriteNoteActivity::class.java)
        intent.putExtra("noteId", note.id)
        startActivity(intent)
    }

    // Function to handle click on delete button for a note
    private fun onDeleteButtonClick(note: Note) {
        // Show confirmation dialog for deleting a note
        Log.d("MainActivity", "Delete button clicked for note with ID: ${note.id}")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Yes") { _, _ ->
            val deleted = notesDatabaseHelper.deleteNote(note.id)
            if (deleted) {
                Log.d("MainActivity", "Note deleted with ID: ${note.id}")
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                // Refresh the list after deletion
                displayAllNotes()
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

    // Function to handle click on update folder button for a note
    private fun onUpdateFolderClick(note: Note) {
        // Show dialog to update the folder of a note
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Enter New Folder Name")

        val input = EditText(this)
        alertDialog.setView(input)

        alertDialog.setPositiveButton("OK") { _, _ ->
            val folderName = input.text.toString()
            val noteId = note.id
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

    // Function to populate NavigationView with folders
    private fun populateNavigationView(navigationView: NavigationView) {
        val menu = navigationView.menu
        val folders = notesDatabaseHelper.getAllFolders() // Retrieve folders from database

        // Clear existing menu items
        menu.clear()

        // Add "All Notes" item at the top with an appropriate icon
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "All Notes")
            .setOnMenuItemClickListener {
                // Display all notes when "All Notes" item is clicked
                displayAllNotes()
                drawerLayout.closeDrawers()
                true
            }

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
                    displayNotesByFolder(folder) // Display notes for the selected folder
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }
}

