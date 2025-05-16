package presentation.screens

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.datetime.Clock
import org.example.logic.models.AuditLog
import org.example.logic.models.Project
import org.example.logic.models.UserRole
import org.example.logic.useCase.DeleteProjectUseCase
import org.example.logic.useCase.GetAllProjectsUseCase
import org.example.logic.useCase.GetEntityAuditLogsUseCase
import org.example.logic.useCase.LogoutUseCase
import org.example.logic.useCase.updateProject.UpdateProjectUseCase
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.ProjectNotChangedException
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskNotFoundException
import org.example.presentation.role.ProjectScreensOptions
import org.example.presentation.screens.ProjectsOverviewUI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.utils.TablePrinter
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectsOverviewUITest {
    private lateinit var getAllProjectsUseCase: GetAllProjectsUseCase
    private lateinit var updateProjectUseCase: UpdateProjectUseCase
    private lateinit var getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var reader: Reader
    private lateinit var deleteProjectUseCase: DeleteProjectUseCase
    private lateinit var viewer: Viewer
    private lateinit var tablePrinter: TablePrinter
    private lateinit var projectScreensOptions: ProjectScreensOptions

    private val mockOnNavigateToShowProjectTasksUI = mockk<(Uuid) -> Unit>(relaxed = true)
    private val mockOnNavigateToProjectStatusUI = mockk<(Uuid) -> Unit>(relaxed = true)
    private val mockOnNavigateBack = mockk<() -> Unit>(relaxed = true)
    private val mockOnLogout = mockk<() -> Unit>(relaxed = true)
    private val mockOnExit = mockk<() -> Unit>(relaxed = true)

    private val id1 = Uuid.random()
    private val id2 = Uuid.random()
    private val sampleProjects =
        listOf(
            Project(id1, "Project Alpha"),
            Project(id2, "Project Beta"),
        )

    private fun launchUI() {
        ProjectsOverviewUI(
            onNavigateToShowProjectTasksUI = mockOnNavigateToShowProjectTasksUI,
            onNavigateToProjectStatusUI = mockOnNavigateToProjectStatusUI,
            onNavigateBack = mockOnNavigateBack,
            onLogout = mockOnLogout,
            onExit = mockOnExit,
            userRoute = UserRole.USER,
            getAllProjectsUseCase = getAllProjectsUseCase,
            updateProjectUseCase = updateProjectUseCase,
            getEntityAuditLogsUseCase = getEntityAuditLogsUseCase,
            logoutUseCase = logoutUseCase,
            deleteProjectUseCase = deleteProjectUseCase,
            reader = reader,
            viewer = viewer,
            tablePrinter = tablePrinter,
            projectScreensOptions = projectScreensOptions,
        )
    }

    @BeforeEach
    fun setUp() {
        getAllProjectsUseCase = mockk(relaxed = true)
        updateProjectUseCase = mockk(relaxed = true)
        getEntityAuditLogsUseCase = mockk(relaxed = true)
        logoutUseCase = mockk(relaxed = true)
        reader = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        deleteProjectUseCase = mockk(relaxed = true)
        tablePrinter = mockk(relaxed = true)
        projectScreensOptions = mockk(relaxed = true)

        every { projectScreensOptions.showAllProjectsOptions() } returns
            mapOf(
                "1" to "1 - Show Project Details",
                "2" to "2 - Update Project",
                "3" to "3 - Delete Project",
                "4" to "4 - Show Logs",
                "5" to "5 - Logout",
                "0" to "0 - Exit",
            )
    }

    @Test
    fun `should return task UI navigation when selecting show project details`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("1", "1", "5")

        launchUI()

        verify { mockOnNavigateToShowProjectTasksUI(id1) }
    }

    @Test
    fun `should return status UI navigation when selecting manage project status`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "2", "1", "5")

        launchUI()

        verify { mockOnNavigateToProjectStatusUI(id1) }
    }

    @Test
    fun `should update project name when new name is provided`() {
        val newName = "Updated Project Alpha"
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "1", "1", newName, "5")

        launchUI()

        coVerify { updateProjectUseCase(match { it.name == newName }) }
    }

    @Test
    fun `should throw ProjectNotChangedException when updating with the same name`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "1", "1", "Project Alpha", "5")
        every { updateProjectUseCase(any()) } throws ProjectNotChangedException()

        launchUI()

        verify { viewer.display(match { it.contains("No changes detected") }) }
    }

    @Test
    fun `should throw ProjectNotFoundException when updating a non-existing project`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "1", "1", "New Name", "5")
        every { updateProjectUseCase(any()) } throws ProjectNotFoundException()

        launchUI()

        verify { viewer.display(match { it.contains("Project not found") }) }
    }

    @Test
    fun `should throw RuntimeException when unexpected error occurs during update`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "1", "1", "new name", "5")
        every { updateProjectUseCase(any()) } throws RuntimeException("Unexpected")

        launchUI()

        verify { viewer.display(match { it.contains("Unexpected") }) }
    }

    @Test
    fun `should return logs display when audit logs are fetched successfully`() {
        val logs =
            listOf(
                AuditLog(
                    id = id1,
                    userId = Uuid.random(),
                    createdAt = Clock.System.now(),
                    entityType = AuditLog.EntityType.PROJECT,
                    entityId = id1,
                    actionType = AuditLog.ActionType.UPDATE,
                    userName = "testuser",
                    entityName = "Project Alpha",
                    fieldChange = null,
                ),
            )

        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { getEntityAuditLogsUseCase(id1, AuditLog.EntityType.PROJECT) } returns Single.just(logs)
        every { reader.readString() } returnsMany listOf("4", "1", "5")

        launchUI()

        verify { tablePrinter.printTable(any(), any()) }
    }

    @Test
    fun `should throw TaskNotFoundException when fetching logs for a task that doesn't exist`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("4", "1", "5")
        every { getEntityAuditLogsUseCase(any(), any()) } throws TaskNotFoundException()

        launchUI()

        verify { viewer.display(match { it.contains("No task found") }) }
    }

    @Test
    fun `should throw ProjectNotFoundException when fetching logs for a non-existing project`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("4", "1", "5")
        every { getEntityAuditLogsUseCase(any(), any()) } throws ProjectNotFoundException()

        launchUI()

        verify { viewer.display(match { it.contains("No project found") }) }
    }

    @Test
    fun `should throw BlankInputException when blank input is given for logs fetching`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("4", "1", "5")
        every { getEntityAuditLogsUseCase(any(), any()) } throws BlankInputException()

        launchUI()

        verify { viewer.display(match { it.contains("cannot be blank") }) }
    }

    @Test
    fun `should return exit action when user selects exit from main menu`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("0")

        launchUI()

        verify { mockOnExit() }
    }

    @Test
    fun `should return invalid option message when unrecognized main menu input is entered`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("99", "5")

        launchUI()

        verify { viewer.display(match { it.contains("Invalid input") }) }
    }

    @Test
    fun `should return invalid input message when unrecognized update option is selected`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("2", "999", "5")

        launchUI()

        verify { viewer.display(match { it.contains("Invalid input") }) }
    }

    @Test
    fun `should delete project when valid index is selected`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("3", "1", "5")
        every { deleteProjectUseCase(id1) } returns Completable.complete()

        launchUI()

        coVerify { deleteProjectUseCase(id1) }
        verify { viewer.display(match { it.contains("Project deleted successfully") }) }
    }

    @Test
    fun `should return without deletion when delete index is invalid`() {
        every { getAllProjectsUseCase() } returns Single.just(sampleProjects)
        every { reader.readString() } returnsMany listOf("3", "", "5")

        launchUI()

        coVerify(exactly = 0) { deleteProjectUseCase(any()) }
    }

    @Test
    fun `should throw RuntimeException when unknown error occurs while loading projects`() {
        every { getAllProjectsUseCase() } returns Single.error(RuntimeException("Database unavailable"))
        every { reader.readString() } returns "5"

        launchUI()

        verify { viewer.display(match { it.contains("Database unavailable") }) }
    }
}
