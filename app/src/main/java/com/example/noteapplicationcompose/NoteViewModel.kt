package com.example.noteapplicationcompose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapplicationcompose.ui.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getInstance(application).noteDao()

    val notes = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun upsertNote(id: Int?, title: String, description: String, color: Long?) {
        viewModelScope.launch {
            val note = if (id != null) {
                NoteEntity(id, title, description, color ?: 0xFFE0F7FA)
            } else {
                NoteEntity(title = title, description = description, colorHex = color ?: 0xFFE0F7FA)
            }
            noteDao.insert(note)
        }
    }



    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }
}
