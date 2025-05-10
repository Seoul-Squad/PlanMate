package presentation.screens

import io.mockk.*
import kotlinx.datetime.Clock
import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLogActionType
import org.example.logic.models.AuditLogEntityType
import org.example.logic.models.Task
import org.example.logic.useCase.DeleteTaskUseCase
import org.example.logic.useCase.GetEntityAuditLogsUseCase
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.useCase.GetStateNameUseCase
import org.example.logic.useCase.GetTaskByIdUseCase
import org.example.logic.useCase.UpdateTaskUseCase
import org.example.presentation.screens.ShowTaskInformation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.utils.TablePrinter
import presentation.utils.io.Reader
import presentation.utils.io.Viewer

class ShowTaskInformationTest {
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var getStateNameUseCase: GetStateNameUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase
    private lateinit var getProjectByIdUseCase: GetProjectByIdUseCase
    private lateinit var viewer: Viewer
    private lateinit var reader: Reader
    private lateinit var showTaskInformation: ShowTaskInformation
    private val tablePrinter = mockk<TablePrinter>(relaxed = true)

    private val sampleTask =
        Task(
            id = "task-1",
            name = "Old Name",
            stateId = "state-1",
            addedBy = "user-1",
            auditLogsIds = emptyList(),
            projectId = "proj-1",
        )
    val logs =
        listOf(
            AuditLog(
                id = "log-1",
                userId = "user-1",
                action = "Created Task",
                createdAt = Clock.System.now(),
                entityType = AuditLogEntityType.TASK,
                entityId = "task-1",
                actionType = AuditLogActionType.CREATE,
            ),
            AuditLog(
                id = "log-2",
                userId = "user-2",
                action = "Updated Task",
                createdAt = Clock.System.now(),
                entityType = AuditLogEntityType.TASK,
                entityId = "task-1",
                actionType = AuditLogActionType.UPDATE,
            ),
        )

    @BeforeEach
    fun setUp() {
        getTaskByIdUseCase = mockk(relaxed = true)
        getStateNameUseCase = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)
        getEntityAuditLogsUseCase = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        reader = mockk(relaxed = true)

        showTaskInformation =
            ShowTaskInformation(
                tablePrinter = tablePrinter,
                getTaskByIdUseCase = getTaskByIdUseCase,
                getStateNameUseCase = getStateNameUseCase,
                updateTaskUseCase = updateTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                getEntityAuditLogsUseCase = getEntityAuditLogsUseCase,
                viewer = viewer,
                reader = reader,
                getProjectByIdUseCase = getProjectByIdUseCase,
                onNavigateBack = {},
            )
    }

    @Test
    fun `should exit immediately when tasks are displayed and choice is 4`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("4")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should return update task flow when choice is 1`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("1", "New Name", "New State", "4")

        showTaskInformation.showTaskInformation("task-1")

        val expectedUpdated = sampleTask.copy(name = "New Name", stateId = "New State")
        coVerify { updateTaskUseCase("task-1", expectedUpdated) }
        verify { viewer.display(any()) }
    }

    @Test
    fun `should return delete task flow when choice is 2 `() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("2", "y")

        showTaskInformation.showTaskInformation("task-1")

        coVerify { deleteTaskUseCase("task-1") }
        verify { viewer.display(any()) }
    }

    @Test
    fun `should return logs flow when choice is 3`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        coEvery { getEntityAuditLogsUseCase("task-1", AuditLogEntityType.TASK) } returns logs
        every { reader.readString() } returnsMany listOf("3", "4")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
        logs.forEach { log ->
            verify {
                viewer.display(any())
            }
        }
    }

    @Test
    fun `should display error updating task when updateTaskUseCase throws`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany
            listOf(
                "1",
                "New Name",
                "new-state",
                "4",
            )
        coEvery { updateTaskUseCase("task-1", any()) } throws RuntimeException("update failure")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display error deleting task when deleteTaskUseCase throws`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("2", "y")
        coEvery { deleteTaskUseCase("task-1") } throws RuntimeException("delete failure")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display deletion cancelled task when user enter n`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("2", "n")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display error fetching logs when getEntityAuditLogsUseCase throws`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("3", "4")
        coEvery { getEntityAuditLogsUseCase("task-1", AuditLogEntityType.TASK) } throws RuntimeException("logs failure")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display invalide option when user enter wrong option`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany listOf("5", "4")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display no logs message when logs list is empty`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        coEvery { getEntityAuditLogsUseCase("task-1", AuditLogEntityType.TASK) } returns emptyList()
        every { reader.readString() } returnsMany listOf("3", "4")

        showTaskInformation.showTaskInformation("task-1")

        verify { viewer.display(any()) }
    }

    @Test
    fun `should update task with default values when new name and sate id are blank`() {
        coEvery { getTaskByIdUseCase("task-1") } returns sampleTask
        coEvery { getStateNameUseCase("task-1") } returns "To Do"
        every { reader.readString() } returnsMany
            listOf(
                "1",
                "",
                "",
                "4",
            )

        showTaskInformation.showTaskInformation("task-1")

        val expectedTask = sampleTask.copy(name = sampleTask.name, stateId = sampleTask.stateId)
        coVerify { updateTaskUseCase("task-1", expectedTask) }
        verify { viewer.display(any()) }
    }
}
