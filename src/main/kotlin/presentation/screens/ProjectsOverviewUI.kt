package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import org.example.logic.models.AuditLog
import org.example.logic.models.Project
import org.example.logic.useCase.*
import org.example.logic.useCase.updateProject.UpdateProjectUseCase
import org.example.logic.utils.*
import org.example.presentation.role.ProjectScreensOptions
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.*
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectsOverviewUI(
    private val onNavigateToShowProjectTasksUI: (id: Uuid) -> Unit,
    private val onNavigateToProjectStatusUI: (id: Uuid) -> Unit,
    private val onLogout: () -> Unit,
    private val onExit: () -> Unit,
    private val projectScreensOptions: ProjectScreensOptions,
    private val getAllProjectsUseCase: GetAllProjectsUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val viewer: Viewer,
    private val reader: Reader,
    private val tablePrinter: TablePrinter,
) {
    private val options: Map<String, String> = projectScreensOptions.showAllProjectsOptions()

    init {
        showMainMenu()
    }

    private fun showAllProjects(): List<Project> =
        runBlocking {
            try {
                val projects = getAllProjectsUseCase()

                if (projects.isEmpty()) {
                    displayNoProjectsMessage()
                    return@runBlocking emptyList()
                }

                showProjectsInTable(projects)
                return@runBlocking projects
            } catch (e: NoProjectsFoundException) {
                displayLoadingError(e)
                return@runBlocking emptyList()
            } catch (e: Exception) {
                displayLoadingError(e)
                return@runBlocking emptyList()
            }
        }

    private fun displayNoProjectsMessage() {
        viewer.display("No projects found.")
    }

    private fun showProjectsInTable(projects: List<Project>) {
        val indexList = projects.indices.map { (it + 1).toString() }
        val projectNames = projects.map { it.name }
        tablePrinter.printTable(
            headers = listOf("Index", "Project Name"),
            columnValues = listOf(indexList, projectNames),
        )
    }

    private fun getProjectByUserIndexSelection(projects: List<Project>): Project? {
        showProjectsInTable(projects)
        viewer.display("Enter the index of the project:")
        val input = reader.readString()
        val index = input.toIntOrNull()?.minus(1)

        if (index == null || index !in projects.indices) {
            viewer.display("Invalid selection. Please try again.".red())
            return null
        }
        return projects[index]
    }

    private fun displayLoadingError(e: Exception) {
        viewer.display("Failed to load projects: ${e.message}".red())
    }

    private fun showMainMenu() {
        while (true) {
            val projects = showAllProjects()
            if (projects.isEmpty()) return

            viewer.display("\n========== Project Menu ==========".cyan())
            val sortedOptions = options.toSortedMap()
            sortedOptions.forEach { option ->
                option.value.let {
                    viewer.display(it)
                }
            }

            viewer.display("Select an option:")
            val input = reader.readString()
            val selectedOption = MainMenuOption.fromKey(input)

            when (selectedOption) {
                MainMenuOption.SHOW_DETAILS -> showProjectDetails(projects)
                MainMenuOption.UPDATE_PROJECT -> manageProject(projects)
                MainMenuOption.DELETE_PROJECT -> deleteProject(projects)
                MainMenuOption.SHOW_PROJECT_LOGS -> showProjectLogsInTable(projects)
                MainMenuOption.LOGOUT -> {
                    logout()
                    return
                }

                MainMenuOption.EXIT -> onExit()

                null -> viewer.display("Invalid input. Please try again.".red())
            }
        }
    }

    private fun showProjectDetails(projects: List<Project>) {
        val project = getProjectByUserIndexSelection(projects) ?: return
        onNavigateToShowProjectTasksUI(project.id)
    }

    private fun manageProject(projects: List<Project>) {
        try {
            viewer.display("1 - Update project name")
            viewer.display("2 - Manage project status")
            val selected = UpdateProjectOption.fromKey(reader.readString())

            when (selected) {
                UpdateProjectOption.UPDATE_NAME -> updateProjectName(projects)
                UpdateProjectOption.MANAGE_STATUS -> {
                    val project = getProjectByUserIndexSelection(projects) ?: return
                    onNavigateToProjectStatusUI(project.id)
                }

                null -> viewer.display("Invalid input.".red())
            }
        } catch (e: Exception) {
            viewer.display("Failed to update project: ${e.message}".red())
        }
    }

    private fun deleteProject(projects: List<Project>) =
        runBlocking {
            try {
                val project = getProjectByUserIndexSelection(projects) ?: return@runBlocking
                deleteProjectUseCase(project.id)
                viewer.display("Project deleted successfully.".green())
            } catch (e: Exception) {
                viewer.display("Failed to delete project: ${e.message}".red())
            }
        }

    private fun showProjectLogsInTable(projects: List<Project>) =
        runBlocking {
            try {
                val project = getProjectByUserIndexSelection(projects) ?: return@runBlocking
                val projectLogs = getEntityAuditLogsUseCase(project.id, AuditLog.EntityType.PROJECT)
                val actions = projectLogs.map { it.toReadableMessage() }
                tablePrinter.printTable(
                    headers = listOf("Actions"),
                    columnValues = listOf(actions),
                )
            } catch (e: TaskNotFoundException) {
                viewer.display("Failed to load project logs: $TASK_NOT_FOUND_ERROR_MESSAGE")
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to load project logs: $PROJECT_NOT_FOUND_ERROR_MESSAGE")
            } catch (e: BlankInputException) {
                viewer.display("Failed to load project logs: $BLANK_ENTITY_ID_ERROR_MESSAGE")
            } catch (e: Exception) {
                viewer.display("Failed to load project logs: ${e.message}".red())
            }
        }

    private fun logout() =
        runBlocking {
            logoutUseCase()
            onLogout()
        }

    private fun updateProjectName(projects: List<Project>) =
        runBlocking {
            try {
                val project = getProjectByUserIndexSelection(projects) ?: return@runBlocking
                viewer.display("Enter new project name:")
                val newName = reader.readString()
                val updated = project.copy(name = newName)
                updateProjectUseCase(updated)
                viewer.display("Project name updated successfully.".green())
            } catch (e: BlankInputException) {
                viewer.display("Failed to update project name: ${e.message}")
            } catch (e: ProjectNotChangedException) {
                viewer.display("Failed to update project name: $NO_CHANGES_DETECTED_EXCEPTION_MESSAGE")
            } catch (e: ProjectNotFoundException) {
                viewer.display("Failed to update project name: $PROJECT_NOT_FOUND_EXCEPTION_MESSAGE")
            } catch (e: Exception) {
                viewer.display("Failed to update project name: ${e.message}".red())
            }
        }

    enum class MainMenuOption(
        val key: String,
    ) {
        SHOW_DETAILS("1"),
        UPDATE_PROJECT("2"),
        DELETE_PROJECT("3"),
        SHOW_PROJECT_LOGS("4"),
        LOGOUT("5"),
        EXIT("0"),
        ;

        companion object {
            fun fromKey(key: String): MainMenuOption? = entries.find { it.key == key }
        }
    }

    enum class UpdateProjectOption(
        val key: String,
    ) {
        UPDATE_NAME("1"),
        MANAGE_STATUS("2"),
        ;

        companion object {
            fun fromKey(key: String): UpdateProjectOption? = entries.find { it.key == key }
        }
    }

    companion object {
        fun create(
            onNavigateToShowProjectTasksUI: (id: Uuid) -> Unit,
            onNavigateToProjectStatusUI: (id: Uuid) -> Unit,
            onLogout: () -> Unit,
            onExit: () -> Unit,
            projectScreensOptions: ProjectScreensOptions,
        ): ProjectsOverviewUI =
            ProjectsOverviewUI(
                onNavigateToShowProjectTasksUI = onNavigateToShowProjectTasksUI,
                onNavigateToProjectStatusUI = onNavigateToProjectStatusUI,
                onLogout = onLogout,
                onExit = onExit,
                projectScreensOptions = projectScreensOptions,
                getAllProjectsUseCase = getKoin().get(),
                updateProjectUseCase = getKoin().get(),
                getProjectByIdUseCase = getKoin().get(),
                deleteProjectUseCase = getKoin().get(),
                getEntityAuditLogsUseCase = getKoin().get(),
                logoutUseCase = getKoin().get(),
                viewer = getKoin().get(),
                reader = getKoin().get(),
                tablePrinter = getKoin().get(),
            )

        const val PROJECT_NOT_FOUND_EXCEPTION_MESSAGE = "Project not found"
        const val NO_CHANGES_DETECTED_EXCEPTION_MESSAGE = "No changes detected ^_^"
        const val BLANK_ENTITY_ID_ERROR_MESSAGE = "Entity id cannot be blank"
        const val TASK_NOT_FOUND_ERROR_MESSAGE = "No task found with this id"
        const val PROJECT_NOT_FOUND_ERROR_MESSAGE = "No project found with this id"
    }
}
