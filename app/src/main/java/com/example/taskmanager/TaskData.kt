package com.example.taskmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * =================================================================================
 * 1. ENTIDADE (Mapeamento do Banco de Dados)
 * =================================================================================
 * A anotação @Entity avisa ao Room que esta classe vai virar uma TABELA no SQLite.
 * O nome da tabela será "tasks". Cada propriedade vira uma coluna no banco.
 */
@Entity(tableName = "tasks")
data class Task(
    // @PrimaryKey indica que a coluna 'id' é a chave única de cada linha.
    // autoGenerate = true faz o banco criar os números (1, 2, 3...) automaticamente.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,        // O texto/descrição da tarefa
    val dateMillis: Long?     // A data escolhida salva em formato de milissegundos (ou null se não tiver data)
)

/*
 * =================================================================================
 * 2. DAO - Data Access Object (Objeto de Acesso aos Dados)
 * =================================================================================
 * O DAO é a "ponte" de comandos entre o Kotlin e o banco SQL.
 * Aqui declaramos quais consultas e alterações queremos fazer no banco.
 */
@Dao
interface TaskDao {

    // Busca todas as tarefas do banco em ordem decrescente (as mais recentes primeiro).
    // O retorno Flow<List<Task>> é um "canal contínuo": sempre que uma tarefa for inserida ou
    // deletada, o Room avisa automaticamente o app para atualizar a lista na tela.
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    // insere uma nova tarefa no banco de dados.
    // O modificador 'suspend' faz a operação rodar de forma assíncrona (em segundo plano),
    // para não travar a tela/interface do celular durante a gravação.
    @Insert
    suspend fun insertTask(task: Task)

    // Deleta uma tarefa específica do banco de dados (também assíncrono com 'suspend').
    @Delete
    suspend fun deleteTask(task: Task)
}

/*
 * =================================================================================
 * 3. VIEWMODEL (Gerenciador de Estado da Interface)
 * =================================================================================
 * O ViewModel é o "cérebro" que conecta o Banco de Dados com a Interface (Compose).
 * Ele guarda os dados da tela e não perde as informações se o celular girar a tela.
 */
class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    // Transforma o 'Flow' contínuo do banco em um Estado que a tela Jetpack Compose consegue ler.
    // .stateIn mantém a lista atualizada e compartilhada com a interface.
    val taskList = taskDao.getAllTasks().stateIn(
        scope = viewModelScope,                           // Executa dentro do ciclo de vida do ViewModel
        started = SharingStarted.WhileSubscribed(5000),   // Mantém ativo enquanto a tela estiver visível
        initialValue = emptyList()                        // Começa com uma lista vazia até o banco carregar
    )

    // Função para adicionar uma nova tarefa.
    // Lança uma Corrotina (viewModelScope.launch) para rodar o salvamento em segundo plano.
    fun addTask(title: String, dateMillis: Long?) {
        viewModelScope.launch {
            taskDao.insertTask(Task(title = title, dateMillis = dateMillis))
        }
    }

    // Função para deletar uma tarefa existente do banco.
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}

/*
 * =================================================================================
 * 4. CLASSE PRINCIPAL DO BANCO DE DADOS
 * =================================================================================
 * Junta todas as entidades (@Entity) e DAOs (@Dao) e cria a estrutura do SQLite.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Função abstrata que o Room implementa automaticamente para nos dar acesso ao TaskDao.
    abstract fun taskDao(): TaskDao
}
