package com.example.noteapplicationcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.noteapplicationcompose.ui.theme.NoteApplicationComposeTheme

class MainActivity : ComponentActivity() {
    private val noteViewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteApplicationComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesScreen(noteViewModel)
                }
            }
        }
    }
}

@Composable
fun NotesScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    val noteColors = listOf(
        Color(0xFFFFF8E1), Color(0xFFE1F5FE), Color(0xFFE8F5E9),
        Color(0xFFFFEBEE), Color(0xFFF3E5F5), Color(0xFFFFF3E0)
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingNoteId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notes...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filteredNotes) { _, note ->
                    NoteCard(
                        title = note.title,
                        description = note.description,
                        backgroundColor = Color(0xFFE0F7FAL),
                        onEdit = {
                            editingNoteId = note.id
                            title = note.title
                            description = note.description
                            isDialogOpen = true
                        },
                        onDelete = {
                            viewModel.deleteNote(note)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingNoteId = null
                title = ""
                description = ""
                isDialogOpen = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Add Note")
        }

        if (isDialogOpen) {
            AlertDialog(
                onDismissRequest = { isDialogOpen = false },
                confirmButton = {
                    Button(onClick = {
                        if (title.isNotBlank() || description.isNotBlank()) {
                            val color = if (editingNoteId == null) noteColors.random().value else null
                            viewModel.upsertNote(editingNoteId, title, description,color?.toLong() )
                            title = ""
                            description = ""
                            editingNoteId = null
                            isDialogOpen = false
                        }
                    }) {
                        Text(if (editingNoteId == null) "Add" else "Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isDialogOpen = false
                        editingNoteId = null
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text(if (editingNoteId == null) "Add Note" else "Edit Note") },
                text = {
                    Column {
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun NoteCard(
    title: String?,
    description: String?,
    backgroundColor: Color?,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor ?: Color.Cyan)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )
                    }

                    if (!title.isNullOrBlank() && !description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF34495E)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF1976D2)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}