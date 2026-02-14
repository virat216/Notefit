package com.example.notefit1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notefit1.data.Note
import com.example.notefit1.data.NoteDatabase
import com.example.notefit1.data.ThemePreferences
import com.example.notefit1.repository.NoteRepository
import com.example.notefit1.util.SortType
import com.example.notefit1.util.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private var recentlyDeletedNote: Note? = null
    private val repository: NoteRepository
    private val themePreferences = ThemePreferences(application)

    // 🔍 Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 🔽 Sort type
    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType = _sortType.asStateFlow()

    // 🧱 Grid / List view state
    private val _isGridView = MutableStateFlow(false)
    val isGridView = _isGridView.asStateFlow()

    // 🌙 DARK MODE STATE
    val themeMode: StateFlow<ThemeMode> =
        themePreferences.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    // 📝 Notes (search + sort combined)
    val notes: StateFlow<List<Note>>

    init {
        val dao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(dao)

        notes = combine(
            searchQuery.debounce(300),
            sortType
        ) { query, sort ->
            query to sort
        }.flatMapLatest { (query, sort) ->
            if (query.isBlank()) {
                repository.getNotes(sort)
            } else {
                repository.searchNotes(query)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // 🔍 from SearchBar
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // 🔽 from Sort menu
    fun onSortChange(sortType: SortType) {
        _sortType.value = sortType
    }

    // 🧱 Toggle Grid/List view
    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    // 📌 PIN / UNPIN NOTE
    fun togglePin(note: Note) = viewModelScope.launch {
        repository.togglePin(note)
    }

    // 🌙 SET THEME MODE
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        themePreferences.setThemeMode(mode)
    }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        recentlyDeletedNote = note
        repository.delete(note)
    }

    fun undoDelete() = viewModelScope.launch {
        recentlyDeletedNote?.let {
            repository.insert(it)
            recentlyDeletedNote = null
        }
    }
}
