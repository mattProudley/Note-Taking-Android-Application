package com.example.notesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

class NoteAdapter(
    private val notes: List<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onUpdateFolderClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val previewTextView: TextView = itemView.findViewById(R.id.previewTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val folderButton: ImageButton = itemView.findViewById(R.id.folderButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.titleTextView.text = currentNote.title
        holder.previewTextView.text = currentNote.preview

        holder.itemView.setOnClickListener {
            // Call onItemClick callback
            onItemClick(currentNote)
        }

        holder.deleteButton.setOnClickListener {
            // Call onDeleteClick callback
            onDeleteClick(currentNote)
        }

        holder.folderButton.setOnClickListener {
            // Call onUpdateFolderClick callback
            onUpdateFolderClick(currentNote)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
