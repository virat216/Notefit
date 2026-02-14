package com.example.notefit1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notefit1.data.Note
import com.example.notefit1.ui.screens.AddEditNoteScreen
import com.example.notefit1.ui.screens.NotesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "notes"
    ) {
        composable("notes") {
            NotesScreen(navController)
        }

        composable("add") {
            AddEditNoteScreen(
                navController = navController
            )
        }

        composable("edit") {
            val note =
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Note>("note")

            AddEditNoteScreen(
                navController = navController,
                note = note
            )
        }
    }
}
