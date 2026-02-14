package com.example.notefit1.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notefit1.data.Note
import com.example.notefit1.reminder.ReminderScheduler
import com.example.notefit1.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    navController: NavController,
    noteViewModel: NoteViewModel = viewModel(),
    note: Note? = null
) {

    val context = LocalContext.current   // ✅ FIX: proper context

    val noteToEdit =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<Note>("note")

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // 🔔 REMINDER STATE
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    val formatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LaunchedEffect(noteToEdit) {
        noteToEdit?.let {
            title = it.title
            content = it.content
            reminderTime = it.reminderTime
        }
    }

    // 🔢 WORD & CHARACTER COUNT
    val charCount = content.length
    val wordCount = content
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (noteToEdit == null) "Add Note" else "Edit Note")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isBlank() && content.isBlank()) return@FloatingActionButton

                    if (noteToEdit == null) {
                        // ✅ ADD NOTE
                        val newNote = Note(
                            title = title,
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            reminderTime = reminderTime
                        )

                        noteViewModel.addNote(newNote)
                        ReminderScheduler.schedule(context, newNote)

                    } else {
                        // ✅ UPDATE NOTE
                        val updatedNote = noteToEdit.copy(
                            title = title,
                            content = content,
                            reminderTime = reminderTime
                        )

                        noteViewModel.updateNote(updatedNote)
                        ReminderScheduler.cancel(context, noteToEdit.id)
                        ReminderScheduler.schedule(context, updatedNote)
                    }

                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Note")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔔 SET REMINDER BUTTON
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)

                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                    calendar.set(Calendar.SECOND, 0)

                                    reminderTime = calendar.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (reminderTime == null)
                        "Set Reminder ⏰"
                    else
                        "Reminder: ${formatter.format(Date(reminderTime!!))}"
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 🔢 WORD / CHARACTER COUNT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 72.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Words: $wordCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Characters: $charCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
