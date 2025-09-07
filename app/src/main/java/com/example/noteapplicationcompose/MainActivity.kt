package com.example.noteapplicationcompose

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.noteapplicationcompose.ui.NoteEntity
import com.example.noteapplicationcompose.ui.theme.NoteApplicationComposeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File
import java.util.Date

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

    var expanded by remember { mutableStateOf(false) } // for dropdown
    var selectedColor by remember { mutableStateOf(Color(0xFFE0F7FA)) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingNoteId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument() // 👈 instead of GetContent
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()

            // 👇 Persist permission so app can access image even after restart
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }


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
                ),
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Sort",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Date") },
                            onClick = {
                                Log.e("TAG", "NotesScreen: cdcjc" )
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Export as Text") },
                            onClick = {
                                exportNotesAsText(context, notes)
                                expanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Export as PDF") },
                            onClick = {
                                exportNotesAsPdf(context, notes)
                                expanded = false
                            }
                        )

                    }
                }
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
                            isPinned = note.isPinned,
                            createAt = note.createAt,
                            onEdit = {
                                editingNoteId = note.id
                                title = note.title
                                description = note.description
                                selectedColor = Color(note.colorHex)
                                isDialogOpen = true
                            },
                            onDelete = {
                                viewModel.deleteNote(note)
                            },
                            onPin = {
                                viewModel.togglePin(note)
                            },
                            note = note,
                            imageUri = note.imageUri
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
                                viewModel.upsertNote(editingNoteId, title, description, selectedColor.toArgb().toLong(), imageUri = selectedImageUri.toString())
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
                            val colors = listOf(
                                Color(0xFFFFCDD2), // Red
                                Color(0xFFC8E6C9), // Green
                                Color(0xFFFFF9C4), // Yellow
                                Color(0xFFBBDEFB), // Blue
                                Color(0xFFD1C4E9)  // Purple
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                colors.forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .clickable { selectedColor = c }
                                            .border(
                                                width = if (selectedColor == c) 3.dp else 1.dp,
                                                color = if (selectedColor == c) Color.Black else Color.Gray,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
                            Spacer(modifier = Modifier.height(12.dp))

                            // ✅ Image picker
                            Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
                                Text("Add Image")
                            }


                            // ✅ Preview selected image
                            selectedImageUri?.let { uri ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(uri)),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
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
    isPinned: Boolean,
    createAt: Long,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    note: NoteEntity,
    imageUri: String?
) {

    val context = LocalContext.current
    val formattedDate = remember(createAt) {
        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date(createAt))
    }
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

                    if (!imageUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(Uri.parse(imageUri)),
                            contentDescription = "Note Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Created: $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onPin) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) Color(0xFF388E3C) else Color.Gray
                        )
                    }
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

                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, note.title)
                            putExtra(Intent.EXTRA_TEXT, "From Note Application ${note.title}\n\n${note.description}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share note via"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }

                }
            }
        }
    }
}

fun exportNotesAsText(context: Context, notes: List<NoteEntity>) {
    try {
        val file = File(context.cacheDir, "notes.txt")
        file.bufferedWriter().use { out ->
            notes.forEach { note ->
                out.write("Title: ${note.title}\n")
                out.write("Description: ${note.description}\n")
                out.write("Date: ${Date(note.createAt)}\n\n")
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // 👈 check manifest
            file
        )

        // 🔥 Share directly after export
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Notes as Text"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export text", Toast.LENGTH_SHORT).show()
    }
}



fun exportNotesAsPdf(context: Context, notes: List<NoteEntity>) {
    try {
        val file = File(context.cacheDir, "notes.pdf")

        val document = PdfDocument()
        val paint = android.graphics.Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50
        notes.forEach { note ->
            canvas.drawText("Title: ${note.title}", 20f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Description: ${note.description}", 20f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Date: ${Date(note.createAt)}", 20f, y.toFloat(), paint)
            y += 40
        }

        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Notes as PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
    }
}




enum class SortType { DATE}