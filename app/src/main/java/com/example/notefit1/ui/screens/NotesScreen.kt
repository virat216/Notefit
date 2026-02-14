package com.example.notefit1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notefit1.data.Note
import com.example.notefit1.ui.components.SwipeToDeleteNote
import com.example.notefit1.util.SortType
import com.example.notefit1.util.ThemeMode
import com.example.notefit1.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    noteViewModel: NoteViewModel = viewModel()
) {
    val notes by noteViewModel.notes.collectAsState()
    val searchQuery by noteViewModel.searchQuery.collectAsState()
    val isGridView by noteViewModel.isGridView.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Notefit") },
                actions = {

                    // 🧱 GRID / LIST TOGGLE
                    TextButton(onClick = { noteViewModel.toggleGridView() }) {
                        Text(if (isGridView) "List" else "Grid")
                    }

                    // ☰ MENU
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        // 🔽 SORT
                        DropdownMenuItem(
                            text = { Text("Newest first") },
                            onClick = {
                                noteViewModel.onSortChange(SortType.DATE_DESC)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest first") },
                            onClick = {
                                noteViewModel.onSortChange(SortType.DATE_ASC)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Title A–Z") },
                            onClick = {
                                noteViewModel.onSortChange(SortType.TITLE_ASC)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Title Z–A") },
                            onClick = {
                                noteViewModel.onSortChange(SortType.TITLE_DESC)
                                showMenu = false
                            }
                        )

                        Divider()

                        // 🌙 DARK MODE OPTIONS
                        DropdownMenuItem(
                            text = { Text("Theme: System") },
                            onClick = {
                                noteViewModel.setThemeMode(ThemeMode.SYSTEM)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Theme: Light") },
                            onClick = {
                                noteViewModel.setThemeMode(ThemeMode.LIGHT)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Theme: Dark") },
                            onClick = {
                                noteViewModel.setThemeMode(ThemeMode.DARK)
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🔍 SEARCH BAR
            SearchBar(
                query = searchQuery,
                onQueryChange = noteViewModel::onSearchQueryChange
            )

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes found")
                }
            } else {

                if (isGridView) {
                    // 🧱 GRID VIEW
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            SwipeToDeleteNote(
                                note = note,
                                onDelete = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                },
                                onClick = {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("note", note)
                                    navController.navigate("edit")
                                },
                                onPinClick = {
                                    noteViewModel.togglePin(note)
                                }
                            )
                        }
                    }
                } else {
                    // 📃 LIST VIEW
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            SwipeToDeleteNote(
                                note = note,
                                onDelete = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                },
                                onClick = {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("note", note)
                                    navController.navigate("edit")
                                },
                                onPinClick = {
                                    noteViewModel.togglePin(note)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔴 DELETE CONFIRMATION DIALOG
    if (showDeleteDialog && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                noteToDelete = null
            },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val deletedNote = noteToDelete!!
                        noteViewModel.deleteNote(deletedNote)

                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Note deleted",
                                actionLabel = "Undo"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                noteViewModel.undoDelete()
                            }
                        }

                        showDeleteDialog = false
                        noteToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        noteToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search notes...") },
        singleLine = true
    )
}
