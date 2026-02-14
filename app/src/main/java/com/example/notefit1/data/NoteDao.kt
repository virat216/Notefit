package com.example.notefit1.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // 📌 ALL NOTES (PINNED FIRST)
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    // 🔍 SEARCH NOTES (PINNED FIRST)
    @Query(
        "SELECT * FROM notes " +
                "WHERE title LIKE '%' || :query || '%' " +
                "OR content LIKE '%' || :query || '%' " +
                "ORDER BY isPinned DESC, timestamp DESC"
    )
    fun searchNotes(query: String): Flow<List<Note>>

    // 🔽 SORT NOTES (PINNED FIRST)
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getNotesByDateDesc(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp ASC")
    fun getNotesByDateAsc(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, title COLLATE NOCASE ASC")
    fun getNotesByTitleAsc(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, title COLLATE NOCASE DESC")
    fun getNotesByTitleDesc(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
}
