package presentation.screens

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import logic.useCase.CreateTaskUseCase
import mockdata.createProject
import mockdata.createTask
import org.example.logic.models.ProjectState
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.useCase.GetProjectTasksUseCase
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
        every { getProjectStatesUseCase.invoke(any()) } returns
            Single.just(
                listOf(ProjectState(ids[3], "In Progress", ids[0])),
            )
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
        every { reader.readInt() } returnsMany listOf(GO_BACK_OPTION)
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
        verify(atLeast = 2) { viewer.display("Enter task index: ") }
        assertThat(navigatedTaskId).isNotNull()
    }

    @Test
    fun `should handle invalid menu option when it is selected`() {
        every { reader.readInt() } returnsMany listOf(99, GO_BACK_OPTION)
        createUi()
        verify { viewer.display("Invalid option. Please, try again!") }
        assertThat(isNavigateBackCalled).isTrue()
    }

    @Test
    fun `should handle generic exception when loading tasks`() {
        every { getProjectTasksUseCase.invoke(any()) } returns Single.error(RuntimeException("Generic error"))
        createUi()
        verify { viewer.display("Generic error") }
    }

    companion object {
        private const val CREATE_TASK_OPTION = 1
        private const val VIEW_TASK_OPTION = 2
        private const val GO_BACK_OPTION = 3
    }
}
