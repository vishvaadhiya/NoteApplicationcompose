package com.example.noteapplicationcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.util.splitToIntList
import com.example.noteapplicationcompose.ui.theme.NoteApplicationComposeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingNoteId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = colorResource(R.color.violet)
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note App", color = colorResource(R.color.white)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.violet)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNoteId = null
                    title = ""
                    description = ""
                    isDialogOpen = true
                },
                contentColor = Color.White,
                containerColor = colorResource(id = R.color.violet),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {

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
                            backgroundColor = Color(note.colorHex),
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

                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No notes added", style = MaterialTheme.typography.bodyLarge)
                    }
                } else if (filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No results found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (isDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    confirmButton = {
                        Button(onClick = {
                            if (title.isNotBlank() || description.isNotBlank()) {
                                val color = if (editingNoteId == null) 0xFFE1F5FE else 0xFFFFEBEE
                                viewModel.upsertNote(editingNoteId, title, description, color)
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
                            style = if (title.isNullOrBlank()) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
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