package com.example.noteapplicationcompose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapplicationcompose.ui.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getInstance(application).noteDao()

    // Always sorted by date (newest first)
//    val notes = noteDao.getAllNotesByDate()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val notes = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun upsertNote(
        id: Int?,
        title: String,
        description: String,
        color: Long,
        createdAt: Long? = null,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val note = if (id != null) {
                // Editing → preserve existing fields
                val existingNote = noteDao.getNoteById(id)  // <-- Add this function in DAO
                NoteEntity(
                    id = id,
                    title = title,
                    description = description,
                    colorHex = color,
                    createAt = createdAt ?: existingNote?.createAt ?: System.currentTimeMillis(),
                    isPinned = existingNote?.isPinned ?: false,
                    imageUri = imageUri ?: existingNote?.imageUri
                )
            } else {
                // Adding new → always use current time
                NoteEntity(
                    title = title,
                    description = description,
                    colorHex = color,
                    imageUri = imageUri
                )
            }
            noteDao.insert(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            val updated = note.copy(isPinned = !note.isPinned)
            noteDao.insert(updated)
        }
    }
}
