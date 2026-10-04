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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * =================================================================================
 * ponto de entrada principal do Aplicativo (MainActivity)
 * =================================================================================
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = Locale.forLanguageTag(resources.getString(R.string.locale))
        Locale.setDefault(locale)

        // 1. INICIALIZAÇÃO DO BANCO DE DADOS ROOM:
        // Room.databaseBuilder cria ou abre o arquivo físico "task_database" no celular.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_database"
        ).build()

        // 2. INICIALIZAÇÃO DO VIEWMODEL:
        // Passamos o DAO do banco (db.taskDao()) para o ViewModel conseguir executar comandos de leitura/escrita.
        val viewModel = TaskViewModel(db.taskDao())

        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 3. DESENHO DA INTERFACE:
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                // Abre a tela principal passando o ViewModel configurado
                TaskManagerLayout(viewModel = viewModel)
            }
        }
    }
}

/*
 * =================================================================================
 * COMPOSABLE STATEFUL (Com Gerenciamento de Estado)
 * =================================================================================
 * Conecta o ViewModel com a interface do Compose.
 */
@Composable
fun TaskManagerLayout(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    // collectAsState(): Ouve o banco de dados em tempo real. Sempre que o banco muda,
    // a variável 'taskList' se atualiza sozinha e re-desenha a lista na tela.
    val taskList by viewModel.taskList.collectAsState()

    // Delega o desenho visual para a função TaskManagerContent
    TaskManagerContent(
        taskList = taskList,
        onAddTask = { title, date -> viewModel.addTask(title, date) },     // Ação de salvar no banco
        onDeleteTask = { task -> viewModel.deleteTask(task) },              // Ação de deletar do banco
        modifier = modifier
    )
}

/*
 * =================================================================================
 * COMPOSABLE STATELESS (Interface Visual e Lógica dos Campos)
 * =================================================================================
 * Controla os estados temporários do formulário (texto digitado, data escolhida e diálogos).
 */
@Composable
fun TaskManagerContent(
    taskList: List<Task>,
    onAddTask: (String, Long?) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // ESTADOS TEMPORÁRIOS DO FORMULÁRIO:
    // 'input': Armazena o texto que o usuário digita na caixa de tarefas.
    var input by remember { mutableStateOf("") }

    // 'selectedDateMillis': Guarda a data escolhida no calendário em milissegundos (ou null se não escolheu).
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // 'showDatePicker': Controla a exibição da janela do calendário (true = visível, false = oculto).
    var showDatePicker by remember { mutableStateOf(false) }

    // 'taskToDelete': Guarda temporariamente a tarefa que o usuário clicou para apagar.
    // Quando não é null, faz a caixa de confirmação (AlertDialog) aparecer na tela.
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

                    // LÓGICA DO CAMPO DE TEXTO:
                    // Atualiza a variável 'input' a cada letra que o usuário digita.
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

                    // LÓGICA DO BOTÃO "SELECIONAR DATA":
                    // Muda 'showDatePicker' para true, fazendo o componente de calendário aparecer.
                    Button(onClick = { showDatePicker = true }) {
                        Text(
                            text = stringResource(R.string.select_date),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    // SELEÇÃO DE DATA (DATE PICKER):
                    // Exibe a janela de calendário somente se 'showDatePicker' for verdadeiro.
                    if (showDatePicker) {
                        TaskDatePickerField(
                            onDateSelected = { millis ->
                                selectedDateMillis = millis // Guarda os milissegundos da data escolhida
                            },
                            onDismiss = {
                                showDatePicker = false // Fecha a janela do calendário
                            }
                        )
                    }
                }
            })
    }, floatingActionButton = {
        // LÓGICA DO BOTÃO FLUTUANTE DE ADICIONAR (FAB):
        FloatingActionButton(
            onClick = {
                // 1. Verifica se o usuário digitou algum texto (ignora espaços em branco)
                if (input.isNotBlank()) {
                    // 2. Chama a função que manda o ViewModel gravar a tarefa no banco de dados
                    onAddTask(input, selectedDateMillis)

                    // 3. Limpa os campos do formulário para a próxima tarefa
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
        // RENDERIZAÇÃO DA LISTA DE TAREFAS:
        LazyColumn(
            modifier = modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Desenha cada item retornado do banco de dados na tela
            items(taskList) { task ->
                TaskItem(
                    task = task,
                    onDelete = {
                        // Ao clicar no botão de concluir/deletar do item,
                        // armazena a tarefa em 'taskToDelete' para disparar o diálogo de confirmação.
                        taskToDelete = task
                    }
                )
            }
        }

        // DIÁLOGO DE CONFIRMAÇÃO DE EXCLUSÃO:
        // Aparece automaticamente quando 'taskToDelete' guarda uma tarefa.
        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null }, // Cancela se clicar fora da janela
                title = { Text("Concluir Tarefa") },
                text = { Text("Deseja marcar '${taskToDelete?.title}' como realizada e removê-la?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Se clicar em "Sim", dispara a exclusão no banco via ViewModel
                        taskToDelete?.let { task -> onDeleteTask(task) }
                        taskToDelete = null // Reseta a variável para fechar o diálogo
                    }) {
                        Text("Sim")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) { // Cancela e fecha o diálogo
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/*
 * Componente do Campo de Texto customizado.
 */
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

/*
 * Componente de Diálogo do Calendário (DatePickerDialog).
 */
@Composable
fun TaskDatePickerField(
    onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit
) {
    // Guarda o estado interno do calendário do Material 3
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Pega os milissegundos da data selecionada e devolve para a tela principal
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss() // Fecha a janela do calendário
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = stringResource(R.string.select_date),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            headline = {
                Text(
                    text = stringResource(R.string.select_date),
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        )
    }
}

/*
 * Componente visual de cada item individual da lista de tarefas.
 */
@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // CONVERSÃO DE DATA:
    // Converte os milissegundos salvos no banco para o formato legível "dd/MM/yyyy" (Ex: 25/10/2025).
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
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Data: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // BOTÃO DE CONCLUIR/DELETAR:
            // Dispara o callback 'onDelete' avisando que esta tarefa deve ser excluída.
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Concluir tarefa"
                )
            }
        }
    }
}

/*
 * Pré-visualização do Layout no Android Studio com dados Fictícios (Mock).
 */
@Preview(showBackground = true)
@Composable
fun TaskDatePickerLayoutPreview() {
    MaterialTheme {
        Surface {
            TaskManagerContent(
                taskList = listOf(
                    Task(id = 1, title = "Estudar Compose", dateMillis = null),
                    Task(id = 2, title = "Comprar pão", dateMillis = null)
                ),
                onAddTask = { _, _ -> },
                onDeleteTask = {}
            )
        }
    }
}
