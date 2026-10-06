@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.taskmanager.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = Locale.forLanguageTag(resources.getString(R.string.locale))
        Locale.setDefault(locale)

        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "task_database"
        ).build()

        val viewModel = TaskViewModel(db.taskDao())

        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TaskManagerLayout(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskManagerLayout(
    viewModel: TaskViewModel, modifier: Modifier = Modifier
) {
    val taskList by viewModel.taskList.collectAsState()

    TaskManagerContent(
        taskList = taskList,
        onAddTask = { title, date -> viewModel.addTask(title, date) },
        onDeleteTask = { task -> viewModel.deleteTask(task) },
        modifier = modifier
    )
}

@Composable
fun TaskManagerContent(
    taskList: List<Task>,
    onAddTask: (String, Long?) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.input_header),
                        modifier = Modifier
                            .align(alignment = Alignment.Start)
                            .padding(top = 8.dp),
                    )

                    EditTextField(
                        label = R.string.task,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                        ),
                        value = input,
                        onValueChanged = { input = it },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 16.dp)
                            .fillMaxWidth(),
                    )

                    Button(onClick = { showDatePicker = true }) {
                        Text(
                            text = stringResource(R.string.select_date),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    if (showDatePicker) {
                        TaskDatePickerField(onDateSelected = { millis ->
                            selectedDateMillis = millis
                        }, onDismiss = {
                            showDatePicker = false
                        })
                    }
                }
            })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                if (input.isNotBlank()) {
                    onAddTask(input, selectedDateMillis)
                    input = ""
                    selectedDateMillis = null
                }
            },
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.adicionar),
                contentDescription = stringResource(R.string.add_task)
            )
        }
    }) {
        LazyColumn(
            modifier = modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            items(taskList) { task ->
                TaskItem(
                    task = task, onDelete = {
                        taskToDelete = task
                    })
            }
        }

        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Concluir Tarefa") },
                text = { Text("Deseja marcar '${taskToDelete?.title}' como realizada e removê-la?") },
                confirmButton = {
                    TextButton(onClick = {
                        taskToDelete?.let { task -> onDeleteTask(task) }
                        taskToDelete = null
                    }) {
                        Text("Sim")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Cancelar")
                    }
                })
        }
    }
}

@Composable
fun EditTextField(
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TaskDatePickerField(
    onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            onDateSelected(datePickerState.selectedDateMillis)
            onDismiss()
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }) {
        DatePicker(state = datePickerState, title = {
            Text(
                text = stringResource(R.string.select_date),
                modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }, headline = {
            Text(
                text = stringResource(R.string.select_date),
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                style = MaterialTheme.typography.headlineLarge
            )
        })
    }
}

@Composable
fun TaskItem(
    task: Task, onDelete: () -> Unit, modifier: Modifier = Modifier
) {
    val formattedDate = task.dateMillis?.let { millis ->
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } ?: "Sem data"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title, style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Check, contentDescription = "Concluir tarefa"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDatePickerLayoutPreview() {
    AppTheme(dynamicColor = false) {
        Surface {
            TaskManagerContent(
                taskList = listOf(
                Task(id = 1, title = "Estudar Compose", dateMillis = null),
                Task(id = 2, title = "Comprar pão", dateMillis = null)
            ), onAddTask = { _, _ -> }, onDeleteTask = {})
        }
    }
}
