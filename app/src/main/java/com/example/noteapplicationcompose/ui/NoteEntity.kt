package com.example.noteapplicationcompose.ui

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val colorHex: Long,
    val createAt: Long = System.currentTimeMillis(),
    val isPinned:Boolean = false,
    val imageUri:String?=null
)
