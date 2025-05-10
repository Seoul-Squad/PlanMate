package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import org.example.logic.models.AuditLog
import org.example.logic.models.State
import org.example.logic.models.Task
import org.example.logic.useCase.*
import org.example.logic.utils.*
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.TablePrinter
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import presentation.utils.toReadableMessage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ShowTaskInformation(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getStateNameUseCase: GetStateNameUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getProjectStatesUseCase: GetProjectStatesUseCase,
    private val viewer: Viewer,
    private val reader: Reader,
    private val tablePrinter: TablePrinter,
    private val onNavigateBack: () -> Unit,
) {
    fun showTaskInformation(taskId: Uuid) =
        runBlocking {
            var isRunning = true
            while (isRunning) {
                try {
                    val task = getTaskByIdUseCase(taskId)
                    val stateName = getStateNameUseCase(taskId)
                    val project = getProjectByIdUseCase(task.projectId)
                    val projectTests = getProjectStatesUseCase(task.projectId)
                    displayTaskDetails(task, stateName)
                    displayMenu()

                    when (reader.readString().trim()) {
                        "1" -> updateTask(task, projectTests)
                        "2" -> {
                            deleteTask(taskId)
                            isRunning = false
                        }

                        "3" -> showTaskLogs(taskId)
                        "4" -> {
                            onNavigateBack
                        }

                        else -> viewer.display("Invalid choice. Please try again.")
                    }
                } catch (e: InvalidInputException) {
                    viewer.display("Error: Task ID should be alphanumeric")
                    isRunning = false
                } catch (e: BlankInputException) {
                    viewer.display("Error: Task ID cannot be blank")
                    isRunning = false
                } catch (e: TaskNotFoundException) {
                    viewer.display("Error: No task found with id: $taskId")
                    isRunning = false
                } catch (e: TaskStateNotFoundException) {
                    viewer.display("Error: State not found")
                    isRunning = false
                } catch (e: Exception) {
                    viewer.display("Error: ${e.message}")
                    isRunning = false
                }
            }
        }

    private fun displayTaskDetails(
        task: Task,
        stateName: String,
    ) {
        val headers = listOf("Field", "Value")
        val rows =
            listOf(
                listOf("Name", task.name),
                listOf("Added By", task.addedBy),
                listOf("State", stateName),
            )

        val columnValues = List(headers.size) { colIndex -> rows.map { it[colIndex] } }

        viewer.display("Task Information:")
        tablePrinter.printTable(headers, columnValues)
        viewer.display("")
    }

    private fun displayMenu() {
        viewer.display("Select an option:")
        viewer.display("1. Update Task")
        viewer.display("2. Delete Task")
        viewer.display("3. Show Task Logs")
        viewer.display("4. Exit")
        viewer.display("Enter your choice:")
    }

    private fun updateTask(
        task: Task,
        projectState: List<State>,
    ) = runBlocking {
        try {
            viewer.display("Enter new task name:")
            val newName = reader.readString().takeIf { it.isNotBlank() } ?: task.name

            viewer.display("Select a new state from the following list:")

            val stateNames = projectState.map { it.title }
            val stateIds = projectState.map { it.id }
            val headers = listOf("Index", "State Name")
            val columnValues =
                listOf(
                    stateNames.indices.map { (it + 1).toString() },
                    stateNames,
                )
            tablePrinter.printTable(headers, columnValues)

            viewer.display("Select a new state index:")
            val index = reader.readInt()
            val newStateId =
                if (index == null || index !in 1..stateIds.size) {
                    viewer.display("Invalid index, keeping old state.")
                    task.stateId
                } else {
                    stateIds[index - 1]
                }

            val updatedTask = task.copy(name = newName, stateId = newStateId)
            updateTaskUseCase(task.id, updatedTask)
            viewer.display("Task updated successfully.")
        } catch (e: TaskNotFoundException) {
            viewer.display("Error Task with id ${task.id} not found")
        } catch (e: TaskNotChangedException) {
            viewer.display("Error No changes detected for task with id ${task.id}")
        } catch (e: Exception) {
            viewer.display("Error updating task: ${e.message}")
        }
    }

    private fun deleteTask(taskId: Uuid): Boolean =
        runBlocking {
            try {
                viewer.display("Do you want to delete this task? (y/n)")
                val confirmation = reader.readString().trim().lowercase()
                if (confirmation == "y") {
                    deleteTaskUseCase(taskId)
                    viewer.display("Task deleted successfully.")
                    return@runBlocking true
                } else {
                    viewer.display("Deletion cancelled.")
                    return@runBlocking false
                }
            } catch (e: TaskDeletionFailedException) {
                viewer.display("Error: Cannot delete task")
                return@runBlocking false
            } catch (e: Exception) {
                viewer.display("Error deleting task: ${e.message}")
                return@runBlocking false
            }
        }

    private fun showTaskLogs(taskId: Uuid) =
        runBlocking {
            try {
                val taskLogs = getEntityAuditLogsUseCase(taskId, AuditLog.EntityType.TASK)
                if (taskLogs.isEmpty()) {
                    viewer.display("No logs found for this task.")
                    return@runBlocking
                }
                val actions = taskLogs.map { it.toReadableMessage() }
                tablePrinter.printTable(
                    headers = listOf("Actions"),
                    columnValues = listOf(actions),
                )
            } catch (e: ProjectNotFoundException) {
                viewer.display("Error: No project found with this id")
            } catch (e: TaskNotFoundException) {
                viewer.display("Error: No task found with this id")
            } catch (e: BlankInputException) {
                viewer.display("Error: Entity id cannot be blank")
            } catch (e: Exception) {
                viewer.display("Error fetching logs: ${e.message}")
            }
        }

    companion object {
        fun create(onNavigateBack: () -> Unit): ShowTaskInformation =
            ShowTaskInformation(
                getTaskByIdUseCase = getKoin().get(),
                onNavigateBack = onNavigateBack,
                getStateNameUseCase = getKoin().get(),
                updateTaskUseCase = getKoin().get(),
                deleteTaskUseCase = getKoin().get(),
                getEntityAuditLogsUseCase = getKoin().get(),
                getProjectByIdUseCase = getKoin().get(),
                viewer = getKoin().get(),
                reader = getKoin().get(),
                tablePrinter = getKoin().get(),
                getProjectStatesUseCase = getKoin().get(),
            )
    }
}
