package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import logic.useCase.CreateTaskUseCase
import org.example.logic.models.Project
import org.example.logic.models.State
import org.example.logic.models.Task
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.useCase.GetProjectTasksUseCase
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidInputException
import org.example.logic.utils.ProjectNotFoundException
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.TablePrinter
import presentation.utils.cyan
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShowProjectTasksUI(
    private val projectId: Uuid,
    private val onNavigateToTaskDetails: (taskId: Uuid) -> Unit,
    private val onNavigateBack: () -> Unit,
    private val getProjectTasksUseCase: GetProjectTasksUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getProjectStatesUseCase: GetProjectStatesUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val reader: Reader,
    private val viewer: Viewer,
    private val tablePrinter: TablePrinter,
) {
    private lateinit var project: Project
    private lateinit var projectStates: List<State>
    private lateinit var projectTasks: List<Task>

    init {
        loadProject()
    }

    private fun loadProject() =
        runBlocking {
            viewer.display("Loading...")
            try {
                project = getProjectByIdUseCase(projectId)
                projectStates = getProjectStatesUseCase(projectId)
                loadTasks()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    private fun loadTasks() =
        runBlocking {
            try {
                projectTasks = getProjectTasksUseCase(projectId)
                getUserSelectedOption()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    private suspend fun displaySwimLanesTasksTable() {
        val (statesHeaders, tasksColumns) = getTableHeadersAndColumns()
        tablePrinter.printTable(
            headers = statesHeaders,
            columnValues = tasksColumns,
        )
    }

    private suspend fun getTableHeadersAndColumns(): Pair<List<String>, List<List<String>>> {
        val groupedTasksByState =
            getProjectStatesUseCase(projectId).map { state ->
                val tasksForState = projectTasks.filter { it.stateId == state.id }
                state to tasksForState
            }
        val statesHeaders = groupedTasksByState.map { it.first.title }
        val tasksColumns = groupedTasksByState.map { it.second.map { task -> task.name } }
        return statesHeaders to tasksColumns
    }

    private suspend fun getUserSelectedOption() {
        while (true) {
            displaySwimLanesTasksTable()
            displayOptions()
            val userInput = reader.readInt() ?: -1
            when (userInput) {
                1 -> {
                    startCreateTaskFlow()
                }

                2 -> {
                    return getSelectedTaskId()
                }

                3 -> {
                    return onNavigateBack()
                }

                else -> {
                    viewer.display("Invalid option. Please, try again!")
                }
            }
        }
    }

    private fun displayOptions() {
        viewer.display("========== Select Option to Continue ==========".cyan())
        showCreateTaskOption()
        if (projectTasks.isNotEmpty()) showViewTaskDetails()
        viewer.display("3- Back")
        viewer.display("Select an option:")
    }

    private fun showViewTaskDetails() {
        viewer.display("2- View Task Details")
    }

    private fun showCreateTaskOption() {
        viewer.display("1- Create New Task")
    }

    private fun getSelectedTaskId() {
        viewer.display("========== Select a Task by Index ==========".cyan())
        val indexedTasks = projectTasks.mapIndexed { index, task -> "${index + 1}- ${task.name}" }
        indexedTasks.forEach { viewer.display(it) }

        while (true) {
            viewer.display("Enter task index: ")
            val input = reader.readString()
            val index = input.toIntOrNull()?.minus(1)

            if (index == null || index !in projectTasks.indices) {
                viewer.display("Invalid index. Please try again.")
            } else {
                val selectedTaskId = projectTasks[index].id
                return onNavigateToTaskDetails(selectedTaskId)
            }
        }
    }

    private fun startCreateTaskFlow() =
        runBlocking {
            val taskName = readTaskName()
            val stateId = readSelectedState()
            try {
                createTaskUseCase(taskName, projectId, stateId)
                loadTasks()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    private fun readTaskName(): String {
        while (true) {
            viewer.display("Enter Task Name: ")
            val name = reader.readString()
            if (name.isBlank()) {
                viewer.display("Name cannot be blank!")
            } else if (name.contains(",")) {
                viewer.display("Name cannot contain comma!")
            } else {
                return name
            }
        }
    }

    private fun readSelectedState(): Uuid {
        viewer.display("Select a state from the following table:")

        val indices = projectStates.indices.map { (it + 1).toString() }
        val titles = projectStates.map { it.title }

        tablePrinter.printTable(
            headers = listOf("Index", "State Name"),
            columnValues = listOf(indices, titles),
        )

        while (true) {
            viewer.display("Enter state index: ")
            val input = reader.readString()
            val index = input.toIntOrNull()?.minus(1)

            if (index == null || index !in projectStates.indices) {
                viewer.display("Invalid index! Please, try again.")
            } else {
                return projectStates[index].id
            }
        }
    }

    private fun handleError(e: Exception) {
        when (e) {
            is ProjectNotFoundException -> {
                viewer.display("Error: project not found")
                return onNavigateBack()
            }
            is InvalidInputException -> {
                viewer.display("Error: Project ID is invalid")
                return onNavigateBack()
            }
            is BlankInputException -> {
                viewer.display("Error: Input cannot be blank")
            }

            else -> {
                viewer.display(e.message)
            }
        }
    }

    companion object {
        fun create(
            projectId: Uuid,
            onNavigateToTaskDetails: (taskId: Uuid) -> Unit,
            onNavigateBack: () -> Unit,
        ): ShowProjectTasksUI =
            ShowProjectTasksUI(
                projectId = projectId,
                onNavigateToTaskDetails = onNavigateToTaskDetails,
                onNavigateBack = onNavigateBack,
                getProjectTasksUseCase = getKoin().get(),
                getProjectByIdUseCase = getKoin().get(),
                createTaskUseCase = getKoin().get(),
                reader = getKoin().get(),
                viewer = getKoin().get(),
                tablePrinter = getKoin().get(),
                getProjectStatesUseCase = getKoin().get(),
            )
    }
}
