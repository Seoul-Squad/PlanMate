package presentation.screens

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import logic.useCase.CreateTaskUseCase
import mockdata.createProject
import mockdata.createTask
import org.example.logic.models.ProjectState
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.useCase.GetProjectTasksUseCase
import org.example.logic.utils.ProjectNotFoundException
import org.example.presentation.screens.ProjectTasksUI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.utils.TablePrinter
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectTasksUITest {
    private lateinit var getProjectTasksUseCase: GetProjectTasksUseCase
    private lateinit var getProjectByIdUseCase: GetProjectByIdUseCase
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase
    private lateinit var createTaskUseCase: CreateTaskUseCase
    private lateinit var reader: Reader
    private lateinit var viewer: Viewer
    private lateinit var tablePrinter: TablePrinter
    private lateinit var projectTasksUi: ProjectTasksUI
    private var isNavigateBackCalled: Boolean = false
    private var navigatedTaskId: Uuid? = null
    private val ids = List(6) { Uuid.random() }
    private val project = createProject(id = ids[0], name = "Test Project")
    private val projectTasks =
        listOf(
            createTask(id = ids[1], name = "Task 1", projectId = ids[0], stateId = ids[3]),
            createTask(id = ids[2], name = "Task 2", projectId = ids[0], stateId = ids[3]),
        )

    @BeforeEach
    fun setUp() {
        getProjectTasksUseCase = mockk(relaxed = true)
        getProjectByIdUseCase = mockk(relaxed = true)
        createTaskUseCase = mockk(relaxed = true)
        getProjectStatesUseCase = mockk(relaxed = true)
        reader = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        tablePrinter = mockk(relaxed = true)
        isNavigateBackCalled = false
        navigatedTaskId = null

        every { getProjectByIdUseCase.invoke(any()) } returns Single.just(project)
        every { getProjectTasksUseCase.invoke(any()) } returns Single.just(projectTasks)
    }

    private fun createUi(): ProjectTasksUI =
        ProjectTasksUI(
            getProjectTasksUseCase = getProjectTasksUseCase,
            getProjectByIdUseCase = getProjectByIdUseCase,
            createTaskUseCase = createTaskUseCase,
            getProjectStatesUseCase = getProjectStatesUseCase,
            reader = reader,
            viewer = viewer,
            tablePrinter = tablePrinter,
            onNavigateBack = {
                isNavigateBackCalled = true
            },
            onNavigateToTaskDetails = { taskId ->
                navigatedTaskId = taskId
            },
            projectId = ids[0],
        )

    @Test
    fun `should navigate back when option 3 is selected`() {
        every { reader.readInt() } returns GO_BACK_OPTION

        createUi()

        assertThat(isNavigateBackCalled).isTrue()
        verify { viewer.display(match { it.contains("Select Option") }) }
    }

    @Test
    fun `should navigate to task details when option 2 is selected with valid task ID`() {
        every { reader.readInt() } returnsMany listOf(VIEW_TASK_OPTION, 1)

        createUi()

        assertThat(navigatedTaskId).isNotNull()
        verify { viewer.display("Enter task index: ") }
    }

    @Test
    fun `should handle invalid task ID when option 2 is selected`() {
        val invalidTaskIndex = 999
        val validTaskIndex = 1
        every { reader.readInt() } returnsMany listOf(VIEW_TASK_OPTION, invalidTaskIndex, validTaskIndex)

        createUi()

        verify { viewer.display("Invalid index. Please try again.") }
        verify(exactly = 2) { viewer.display("Enter task index: ") }
        assertThat(navigatedTaskId).isNotNull()
    }

    @Test
    fun `should not start create new task flow when project has no states`() =
        runTest {
            every { getProjectByIdUseCase(any()) } returns
                Single.just(
                    createProject(
                        id = ids[0],
                        name = "Test Project",
                    ),
                )
            every { reader.readInt() } returnsMany listOf(GO_BACK_OPTION)
            every { getProjectStatesUseCase(any()) } returns Single.just(emptyList())
            every { createTaskUseCase.invoke(any(), any(), any()) } returns
                Single.just(
                    createTask(
                        name = "New Task",
                        stateId = ids[3],
                    ),
                )

            createUi()

            assertThat(isNavigateBackCalled).isTrue()
        }

    @Test
    fun `should handle invalid menu option when it is selected`() {
        every { reader.readInt() } returnsMany listOf(99, 3)

        createUi()

        verify { viewer.display("Invalid option. Please, try again!") }
        assertThat(isNavigateBackCalled).isTrue()
    }

    @Test
    fun `should handle project not found exception`() {
        every { getProjectByIdUseCase.invoke(any()) } returns Single.error(ProjectNotFoundException())

        createUi()

        verify { viewer.display("Error: project not found") }
        assertThat(isNavigateBackCalled).isTrue()
    }

    @Test
    fun `should handle generic exception when loading tasks`() {
        every { getProjectTasksUseCase.invoke(any()) } returns Single.error(RuntimeException("Generic error"))

        createUi()

        verify { viewer.display("Generic error") }
    }

    @Test
    fun `should create a task when valid name and state are provided`() =
        runTest {
            every { getProjectStatesUseCase.invoke(any()) } returns
                Single.just(
                    listOf(
                        ProjectState(
                            ids[3],
                            "In Progress",
                            ids[0],
                        ),
                    ),
                )
            every { reader.readInt() } returnsMany listOf(CREATE_TASK_OPTION, 1, 3)
            every { reader.readString() } returns "Valid Task" andThen "1"
            every { createTaskUseCase.invoke(any(), any(), any()) } returns
                Single.just(
                    createTask(
                        name = "Valid Task",
                        projectId = ids[0],
                        stateId = ids[3],
                    ),
                )

            createUi()

            verify { viewer.display("Enter Task Name: ") }
            verify { viewer.display("Select a state from the following table:") }
            assertThat(isNavigateBackCalled).isTrue()
        }

    @Test
    fun `should reject task name with comma`() =
        runTest {
            every { reader.readInt() } returnsMany listOf(1, 3)
            every { reader.readString() } returnsMany listOf("Invalid,Name", "Valid Name", "1")
            every { getProjectStatesUseCase.invoke(any()) } returns
                    Single.just(
                    listOf(
                        ProjectState(
                            ids[3],
                            "State",
                            ids[0],
                        ),
                    ),
                )
            every { createTaskUseCase.invoke(any(), any(), any()) } returns
                Single.just(
                    createTask(
                        name = "Valid Name",
                        projectId = ids[0],
                        stateId = ids[3],
                    ),
                )

            createUi()

            verify { viewer.display("Name cannot contain comma!") }
            assertThat(isNavigateBackCalled).isTrue()
        }

    @Test
    fun `should prompt again for invalid state index`() =
        runTest {
            every { reader.readInt() } returnsMany listOf(1, 3)
            every { reader.readString() } returnsMany listOf("Valid Name", "999", "1") // invalid, then valid
            every { getProjectStatesUseCase.invoke(any()) } returns
                    Single.just(
                        listOf(
                            ProjectState(
                            ids[3],
                            "State",
                            ids[0],
                        ),
                    ),
                )
            every { createTaskUseCase.invoke(any(), any(), any()) } returns
                Single.just(
                    createTask(
                        name = "Task",
                        projectId = ids[0],
                        stateId = ids[3],
                    ),
                )

            createUi()

            verify { viewer.display("Invalid index! Please, try again.") }
            assertThat(isNavigateBackCalled).isTrue()
        }

    @Test
    fun `should handle exception when creating a task`() =
        runTest {
            every { reader.readInt() } returnsMany listOf(1, 3)
            every { reader.readString() } returnsMany listOf("Valid Name", "1")
            every { getProjectStatesUseCase.invoke(any()) } returns
                    Single.just(
                        listOf(
                            ProjectState(
                                ids[3],
                            "Done",
                            ids[0],
                        ),
                    ),
                )
            every {
                createTaskUseCase.invoke(
                    any(),
                    any(),
                    any(),
                )
            } returns Single.error(RuntimeException("Task creation failed"))

            createUi()

            verify { viewer.display("Task creation failed") }
        }

    companion object {
        private const val CREATE_TASK_OPTION = 1
        private const val VIEW_TASK_OPTION = 2
        private const val GO_BACK_OPTION = 3
    }
}
