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
        id: Int? = null,
        title: String,
        description: String,
        colorHex: Long,
        isPinned: Boolean = false,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id ?: 0,
                title = title,
                description = description,
                colorHex = colorHex,
                isPinned = isPinned,
                createAt = System.currentTimeMillis(),
                imageUri = imageUri  // 👈 persist image uri
            )
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
