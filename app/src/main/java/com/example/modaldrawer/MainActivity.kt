package com.example.modaldrawer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesApp()
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun NotesApp() {
    val notes by remember { mutableStateOf(mutableListOf(
        Note("Добро пожаловать!", "Напишите свою первую заметку")
    )) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showCreateNoteScreen by remember { mutableStateOf(false) }

    if (showCreateNoteScreen) {
        CreateNoteScreen(
            onSave = { title, content ->
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    notes.add(Note(title, content))
                    selectedNote = notes.lastOrNull()
                }
                showCreateNoteScreen = false
            },
            onCancel = { showCreateNoteScreen = false }
        )
    } else {
        NotesScreen(
            snackbarHostState,
            scope,
            notes = notes,
            selectedNote = selectedNote,
            onNoteSelected = { selectedNote = it },
            onNoteDeleted = { note ->
                if (notes.size > 1) {
                    notes.remove(note)
                    selectedNote = notes.lastOrNull()
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Должна оставаться хотя бы одна заметка")
                    }
                }
            },
            onCreateNoteClicked = { showCreateNoteScreen = true }
        )
    }
}

@Composable
fun NotesScreen(
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    notes: List<Note>,
    selectedNote: Note?,
    onNoteSelected: (Note) -> Unit,
    onNoteDeleted: (Note) -> Unit,
    onCreateNoteClicked: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    ModalNavigationDrawer(
        modifier = Modifier.padding(top = 50.dp),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.LightGray,
                drawerContentColor = Color.Gray
            ) {
                Column {
                    notes.forEach { note ->
                        NavigationDrawerItem(
                            label = { Text(note.title) },
                            icon = {
                                IconButton(onClick = { onNoteDeleted(note) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            },
                            selected = note == selectedNote,
                            onClick = {
                                onNoteSelected(note)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        },
        content = {

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = onCreateNoteClicked) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                },
                content = { padding ->
                    Row {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        },
                            content = {
                                Icon(Icons.Filled.Menu, "")
                            }
                        )

                    }
                    selectedNote?.let {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                it.title, fontSize = 24.sp, textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(it.content, fontSize = 16.sp)
                            SnackbarHost(hostState = snackbarHostState)
                        }
                    }
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Note") },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            onSave(title, content)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Все поля должны быть заполнены")
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Save")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Загаловок") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Содержание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Button(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Отмена")
                }
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    )
}