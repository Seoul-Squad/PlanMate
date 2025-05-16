package presentation.screens

import io.mockk.*
import io.reactivex.rxjava3.core.Single
import mockdata.createAuditLog
import mockdata.createProject
import mockdata.createTask
import org.example.logic.models.AuditLog
import org.example.logic.useCase.*
import org.example.presentation.screens.TaskInformationUI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.utils.TablePrinter
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TaskInformationUITest {
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase
    private lateinit var getProjectByIdUseCase: GetProjectByIdUseCase
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase
    private lateinit var viewer: Viewer
    private lateinit var reader: Reader
    private lateinit var taskInformationUi: TaskInformationUI
    private val tablePrinter = mockk<TablePrinter>(relaxed = true)
    private val ids = List(6) { Uuid.random() }
    private val sampleTask =
        createTask(
            id = ids[0],
            name = "Old Name",
            stateId = ids[3],
        )
    private val sampleProject =
        createProject(
            id = ids[4],
        )
    val logs =
        listOf(
            createAuditLog(
                id = ids[1],
            ),
            createAuditLog(
                id = ids[2],
            ),
            createAuditLog(
                id = ids[3],
            ),
        )

    @BeforeEach
    fun setUp() {
        getTaskByIdUseCase = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)
        getEntityAuditLogsUseCase = mockk(relaxed = true)
        getProjectStatesUseCase = mockk(relaxed = true)
        getProjectByIdUseCase = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        reader = mockk(relaxed = true)

        taskInformationUi =
            TaskInformationUI(
                tablePrinter = tablePrinter,
                getTaskByIdUseCase = getTaskByIdUseCase,
                updateTaskUseCase = updateTaskUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                getEntityAuditLogsUseCase = getEntityAuditLogsUseCase,
                getProjectStatesUseCase = getProjectStatesUseCase,
                viewer = viewer,
                reader = reader,
                onNavigateBack = {},
            )
    }

    @Test
    fun `should exit immediately when tasks are displayed and choice is 4`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("4")

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should return update task flow when choice is 1`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("1", "New Name", "New State", "4")

        taskInformationUi.showTaskInformation(ids[0])

        val expectedUpdated = sampleTask.copy(name = "New Name")
        coVerify { updateTaskUseCase(expectedUpdated) }
        verify { viewer.display(any()) }
    }

    @Test
    fun `should return delete task flow when choice is 2 `() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("2", "y")

        taskInformationUi.showTaskInformation(ids[0])

        coVerify { deleteTaskUseCase(ids[0]) }
        verify { viewer.display(any()) }
    }

    @Test
    fun `should return logs flow when choice is 3`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        coEvery { getEntityAuditLogsUseCase(ids[0], AuditLog.EntityType.TASK) } returns Single.just(logs)
        every { reader.readString() } returnsMany listOf("3", "4")

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
        logs.forEach { log ->
            verify {
                viewer.display(any())
            }
        }
    }

    @Test
    fun `should display error updating task when updateTaskUseCase throws`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany
            listOf(
                "1",
                "New Name",
                "new-state",
                "4",
            )
        coEvery { updateTaskUseCase(any()) } returns Single.error(RuntimeException("update failure"))

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display error deleting task when deleteTaskUseCase throws`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("2", "y")
        coEvery { deleteTaskUseCase(ids[0]) }

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display deletion cancelled task when user enter n`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("2", "n")

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display error fetching logs when getEntityAuditLogsUseCase throws`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany listOf("3", "4")
        coEvery {
            getEntityAuditLogsUseCase(
                ids[0],
                AuditLog.EntityType.TASK,
            )
        } returns Single.error(RuntimeException("logs failure"))

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should display invalid option when user enter wrong option`() {
        val taskId = ids[0]
        coEvery { getTaskByIdUseCase(taskId) } returns Single.just(sampleTask)
        coEvery { getProjectByIdUseCase(any()) } returns Single.just(sampleProject)
        coEvery { getProjectStatesUseCase(any()) } returns Single.just(listOf())
        every { reader.readString() } returnsMany listOf("5", "4")

        taskInformationUi.showTaskInformation(taskId)

        verify { viewer.display("Invalid choice. Please try again.") }
        verify { viewer.display(any()) }
    }

    @Test
    fun `should display no logs message when logs list is empty`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        coEvery { getEntityAuditLogsUseCase(ids[0], AuditLog.EntityType.TASK) } returns Single.just(emptyList())
        every { reader.readString() } returnsMany listOf("3", "4")

        taskInformationUi.showTaskInformation(ids[0])

        verify { viewer.display(any()) }
    }

    @Test
    fun `should update task with default values when new name and sate id are blank`() {
        coEvery { getTaskByIdUseCase(ids[0]) } returns Single.just(sampleTask)
        every { reader.readString() } returnsMany
            listOf(
                "1",
                "",
                "",
                "4",
            )

        taskInformationUi.showTaskInformation(ids[0])

        val expectedTask = sampleTask.copy(name = sampleTask.name, stateId = sampleTask.stateId)
        coVerify { updateTaskUseCase(expectedTask) }
        verify { viewer.display(any()) }
    }
}
