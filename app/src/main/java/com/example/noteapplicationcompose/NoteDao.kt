package com.example.noteapplicationcompose

import androidx.room.*
import com.example.noteapplicationcompose.ui.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, id DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY createAt DESC")
    fun getAllNotesByDate(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteEntity?

}
