package com.example.notesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

class NoteAdapter(
    private val notes: List<Note>, // List of notes to display
    private val onItemClick: (Note) -> Unit, // Callback for item click event
    private val onDeleteClick: (Note) -> Unit, // Callback for delete button click event
    private val onUpdateFolderClick: (Note) -> Unit // Callback for folder button click event
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder class to hold references to UI elements
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView) // TextView for note title
        val previewTextView: TextView = itemView.findViewById(R.id.previewTextView) // TextView for note preview
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton) // ImageButton for delete action
        val folderButton: ImageButton = itemView.findViewById(R.id.folderButton) // ImageButton for folder action
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Create a new ViewHolder when needed
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        // Bind data to ViewHolder
        val currentNote = notes[position]
        holder.titleTextView.text = currentNote.title // Set note title
        holder.previewTextView.text = currentNote.preview // Set note preview

        holder.itemView.setOnClickListener {
            // Call onItemClick callback when item is clicked
            onItemClick(currentNote)
        }

        holder.deleteButton.setOnClickListener {
            // Call onDeleteClick callback when delete button is clicked
            onDeleteClick(currentNote)
        }

        holder.folderButton.setOnClickListener {
            // Call onUpdateFolderClick callback when folder button is clicked
            onUpdateFolderClick(currentNote)
        }
    }

    override fun getItemCount(): Int {
        // Return the number of items in the list
        return notes.size
    }
}
