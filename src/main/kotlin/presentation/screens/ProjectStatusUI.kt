package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import org.example.logic.useCase.*
import org.example.logic.useCase.CreateStateUseCase
import org.example.logic.useCase.DeleteStateUseCase
import org.example.logic.useCase.UpdateStateUseCase
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidInputException
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskStateNotFoundException
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.TablePrinter
import presentation.utils.cyan
import presentation.utils.green
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import presentation.utils.red
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectStatusUI(
    private val createStateUseCase: CreateStateUseCase,
    private val updateStateUseCase: UpdateStateUseCase,
    private val deleteStateUseCase: DeleteStateUseCase,
    private val getProjectStatesUseCase: GetProjectStatesUseCase,
    private val projectId: Uuid,
    private val viewer: Viewer,
    private val reader: Reader,
    private val onNavigateBack: () -> Unit,
    private val tablePrinter: TablePrinter,
) {
    init {
        manageProjectStatus()
    }

    private fun manageProjectStatus() {
        while (true) {
            showProjectStates()
            displayMenu()

            val input = reader.readString()
            val selectedOption = StatusMenuOption.fromKey(input)

            when (selectedOption) {
                StatusMenuOption.CREATE -> createProjectState()
                StatusMenuOption.UPDATE -> updateProjectState()
                StatusMenuOption.DELETE -> deleteProjectState()
                StatusMenuOption.BACK -> {
                    onNavigateBack()
                    return
                }
                null -> viewer.display("Invalid input. Please try again.".red())
            }
        }
    }

    private fun displayMenu() {
        viewer.display("\n========== Manage Project States ==========".cyan())
        StatusMenuOption.entries.forEach {
            viewer.display("${it.key}- ${it.label}")
        }
        viewer.display("Select an option:")
    }

    private fun showProjectStates() =
        runBlocking {
            try {
                val projectStates = getProjectStatesUseCase(projectId)
                viewer.display("\n========== Project States ==========".cyan())

                val indexList = projectStates.indices.map { (it + 1).toString() }
                val stateTitles = projectStates.map { it.title }

                tablePrinter.printTable(
                    headers = listOf("Index", "State Title"),
                    columnValues = listOf(indexList, stateTitles),
                )
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to fetch project states: ${e.message}")
            } catch (e: InvalidInputException) {
                viewer.display("Failed to fetch project states: Project ID is invalid")
            } catch (e: Exception) {
                viewer.display("Failed to fetch project states: ${e.message}".red())
            }
        }

    private fun createProjectState() =
        runBlocking {
            try {
                viewer.display("Enter the new state name:")
                val stateName = reader.readString()
                createStateUseCase(projectId, stateName)
                viewer.display("State created successfully.".green())
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to create state: Project not found")
            } catch (e: BlankInputException) {
                viewer.display("Input cannot be blank")
            } catch (e: Exception) {
                viewer.display("Failed to create state: ${e.message}".red())
            }
        }

    private fun updateProjectState() =
        runBlocking {
            try {
                val projectStates = getProjectStatesUseCase(projectId)

                viewer.display("Enter the index of the state to update:".cyan())
                val input = reader.readString()
                val stateIndex = input.toIntOrNull()?.minus(1)

                if (stateIndex == null || stateIndex !in projectStates.indices) {
                    viewer.display("Invalid index. Please try again.".red())
                    return@runBlocking
                }

                val stateId = projectStates[stateIndex].id
                viewer.display("Enter the new state name:".cyan())
                val newStateName = reader.readString()

                updateStateUseCase(newStateName, stateId, projectId)
                viewer.display("State updated successfully.".green())
            } catch (e: TaskStateNotFoundException) {
                viewer.display("Failed to update state: State not found")
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to update state: Project not found")
            } catch (e: BlankInputException) {
                viewer.display("Input cannot be blank")
            } catch (e: Exception) {
                viewer.display("Failed to update state: ${e.message}".red())
            }
        }

    private fun deleteProjectState() =
        runBlocking {
            try {
                val projectStates = getProjectStatesUseCase(projectId)

                viewer.display("Enter the index of the state to delete:".cyan())
                val input = reader.readString()
                val stateIndex = input.toIntOrNull()?.minus(1)

                if (stateIndex == null || stateIndex !in projectStates.indices) {
                    viewer.display("Invalid index. Please try again.".red())
                    return@runBlocking
                }

                val stateId = projectStates[stateIndex].id
                deleteStateUseCase(stateId, projectId)
                viewer.display("State deleted successfully.".green())
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to delete state: Project not found")
            } catch (e: BlankInputException) {
                viewer.display("Input cannot be blank")
            } catch (e: Exception) {
                viewer.display("Failed to delete state: ${e.message}".red())
            }
        }

    companion object {
        fun create(
            projectId: Uuid,
            onNavigateBack: () -> Unit,
        ): ProjectStatusUI =
            ProjectStatusUI(
                projectId = projectId,
                onNavigateBack = onNavigateBack,
                createStateUseCase = getKoin().get(),
                updateStateUseCase = getKoin().get(),
                deleteStateUseCase = getKoin().get(),
                getProjectStatesUseCase = getKoin().get(),
                viewer = getKoin().get(),
                reader = getKoin().get(),
                tablePrinter = getKoin().get(),
            )
    }

    enum class StatusMenuOption(
        val key: String,
        val label: String,
    ) {
        CREATE("1", "Create project state"),
        UPDATE("2", "Update project state"),
        DELETE("3", "Delete project state"),
        BACK("4", "Back to show all projects"),
        ;

        companion object {
            fun fromKey(key: String): StatusMenuOption? = entries.find { it.key == key }
        }
    }
}
