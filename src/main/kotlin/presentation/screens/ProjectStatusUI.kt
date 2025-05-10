package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import org.example.logic.useCase.CreateStateUseCase
import org.example.logic.useCase.DeleteStateUseCase
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.useCase.UpdateStateUseCase
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.TablePrinter
import presentation.utils.cyan
import presentation.utils.green
import presentation.utils.red
import presentation.utils.io.Reader
import presentation.utils.io.Viewer

class ProjectStatusUI(
    private val createStateUseCase: CreateStateUseCase,
    private val updateStateUseCase: UpdateStateUseCase,
    private val deleteStateUseCase: DeleteStateUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val projectId: String,
    private val viewer: Viewer,
    private val reader: Reader,
    private val onNavigateBack: () -> Unit,
    private val tablePrinter: TablePrinter
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

    private fun showProjectStates() = runBlocking {
        try {
            val project = getProjectByIdUseCase(projectId)
            viewer.display("\n========== Project States ==========".cyan())

            val indexList = project.states.indices.map { (it + 1).toString() }
            val stateTitles = project.states.map { it.title }

            tablePrinter.printTable(
                headers = listOf("Index", "State Title"),
                columnValues = listOf(indexList, stateTitles)
            )

        } catch (e: Exception) {
            viewer.display("Failed to fetch project states: ${e.message}".red())
        }
    }

    private fun createProjectState() = runBlocking {
        try {
            viewer.display("Enter the new state name:")
            val stateName = reader.readString()
            createStateUseCase(projectId, stateName)
            viewer.display("State created successfully.".green())
        } catch (e: Exception) {
            viewer.display("Failed to create state: ${e.message}".red())
        }
    }

    private fun updateProjectState() = runBlocking {
        try {
            val project = getProjectByIdUseCase(projectId)

            viewer.display("Enter the index of the state to update:".cyan())
            val input = reader.readString()
            val stateIndex = input.toIntOrNull()?.minus(1)

            if (stateIndex == null || stateIndex !in project.states.indices) {
                viewer.display("Invalid index. Please try again.".red())
                return@runBlocking
            }

            val stateId = project.states[stateIndex].id
            viewer.display("Enter the new state name:".cyan())
            val newStateName = reader.readString()

            updateStateUseCase(newStateName, stateId, projectId)
            viewer.display("State updated successfully.".green())

        } catch (e: Exception) {
            viewer.display("Failed to update state: ${e.message}".red())
        }
    }

    private fun deleteProjectState() = runBlocking {
        try {
            val project = getProjectByIdUseCase(projectId)

            viewer.display("Enter the index of the state to delete:".cyan())
            val input = reader.readString()
            val stateIndex = input.toIntOrNull()?.minus(1)

            if (stateIndex == null || stateIndex !in project.states.indices) {
                viewer.display("Invalid index. Please try again.".red())
                return@runBlocking
            }

            val stateId = project.states[stateIndex].id
            deleteStateUseCase(stateId, projectId)
            viewer.display("State deleted successfully.".green())

        } catch (e: Exception) {
            viewer.display("Failed to delete state: ${e.message}".red())
        }
    }

    companion object {
        fun create(
            projectId: String,
            onNavigateBack: () -> Unit
        ): ProjectStatusUI {
            return ProjectStatusUI(
                projectId = projectId,
                onNavigateBack = onNavigateBack,
                createStateUseCase = getKoin().get(),
                updateStateUseCase = getKoin().get(),
                deleteStateUseCase = getKoin().get(),
                getProjectByIdUseCase = getKoin().get(),
                viewer = getKoin().get(),
                reader = getKoin().get(),
                tablePrinter = getKoin().get()
            )
        }
    }

    enum class StatusMenuOption(val key: String, val label: String) {
        CREATE("1", "Create project state"),
        UPDATE("2", "Update project state"),
        DELETE("3", "Delete project state"),
        BACK("4", "Back to show all projects");

        companion object {
            fun fromKey(key: String): StatusMenuOption? = entries.find { it.key == key }
        }
    }
}
