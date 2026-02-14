package com.example.notefit1.repository

import com.example.notefit1.data.Note
import com.example.notefit1.data.NoteDao
import com.example.notefit1.util.SortType
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    // 🔽 SORT NOTES (PINNED FIRST handled in DAO)
    fun getNotes(sortType: SortType): Flow<List<Note>> {
        return when (sortType) {
            SortType.DATE_DESC -> dao.getNotesByDateDesc()
            SortType.DATE_ASC -> dao.getNotesByDateAsc()
            SortType.TITLE_ASC -> dao.getNotesByTitleAsc()
            SortType.TITLE_DESC -> dao.getNotesByTitleDesc()
        }
    }

    // 🔍 SEARCH NOTES (PINNED FIRST handled in DAO)
    fun searchNotes(query: String): Flow<List<Note>> {
        return dao.searchNotes(query)
    }

    // ✏️ CRUD
    suspend fun insert(note: Note) {
        dao.insertNote(note)
    }

    suspend fun update(note: Note) {
        dao.updateNote(note)
    }

    suspend fun delete(note: Note) {
        dao.deleteNote(note)
    }

    // 📌 PIN / UNPIN (helper)
    suspend fun togglePin(note: Note) {
        dao.updateNote(
            note.copy(isPinned = !note.isPinned)
        )
    }
}

