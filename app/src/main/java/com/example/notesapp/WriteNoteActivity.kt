package com.example.notesapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class WriteNoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextNote: EditText
    private lateinit var buttonSaveNote: Button
    private lateinit var buttonCancelNote: Button
    private lateinit var notesDatabaseHelper: NotesDatabaseHelper
    private var noteId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_note)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextNote = findViewById(R.id.editTextNote)
        buttonSaveNote = findViewById(R.id.buttonSaveNote)
        buttonCancelNote = findViewById(R.id.buttonCancelNote)
        notesDatabaseHelper = NotesDatabaseHelper(this)

        noteId = intent.getLongExtra("noteId", -1)
        if (noteId != -1L) {
            // If noteId is provided, it's an update operation
            Log.d("WriteNoteActivity", "Editing note with ID: $noteId")
            val note = notesDatabaseHelper.readNote(noteId)
            editTextTitle.setText(note?.second ?: "")
            editTextNote.setText(note?.third ?: "")
        }

        buttonSaveNote.setOnClickListener {
            val titleText = editTextTitle.text.toString().trim()
            val noteText = editTextNote.text.toString().trim()
            if (noteText.isNotEmpty()) {
                if (noteId != -1L) {
                    // If noteId is provided, it's an update operation
                    val success = notesDatabaseHelper.updateNote(noteId, titleText, noteText)
                    if (success) {
                        Log.d("WriteNoteActivity", "Note updated successfully with ID: $noteId")
                        Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Log.e("WriteNoteActivity", "Failed to update note with ID: $noteId")
                        Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Otherwise, it's a new note, so save it
                    val newRowId = notesDatabaseHelper.createNote(titleText, noteText)
                    if (newRowId != -1L) {
                        Log.d("WriteNoteActivity", "Note saved successfully with new ID: $newRowId")
                        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Log.e("WriteNoteActivity", "Failed to save new note")
                        Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please include note content", Toast.LENGTH_SHORT).show()
            }
        }

        // Add logic for the "Cancel" button
        buttonCancelNote.setOnClickListener {
            // Finish the activity without saving changes
            Log.d("WriteNoteActivity", "Cancel clicked, canceling changes")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}

