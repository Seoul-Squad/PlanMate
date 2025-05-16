package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.GetProjectTasksUseCase
import org.example.logic.utils.NoTasksFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectTasksUseCaseTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var getProjectTasksUseCase: GetProjectTasksUseCase

    private val projectId = Uuid.random()
    private val otherProjectId = Uuid.random()
    private val tasks = listOf(
        Task(
            id = Uuid.random(),
            name = "Task 1",
            stateId = Uuid.random(),
            stateName = "To Do",
            projectId = projectId,
            addedById = Uuid.random(),
            addedByName = "User 1"
        ),
        Task(
            id = Uuid.random(),
            name = "Task 2",
            stateId = Uuid.random(),
            stateName = "In Progress",
            projectId = projectId,
            addedById = Uuid.random(),
            addedByName = "User 1"
        ),
        Task(
            id = Uuid.random(),
            name = "Task 3",
            stateId = Uuid.random(),
            stateName = "Done",
            projectId = otherProjectId,
            addedById = Uuid.random(),
            addedByName = "User 2"
        )
    )

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        getProjectTasksUseCase = GetProjectTasksUseCase(taskRepository)
    }

    @Test
    fun `should return only tasks for the specified project when tasks exist`() {
        every { taskRepository.getAllTasks() } returns Single.just(tasks)

        val testObserver: TestObserver<List<Task>> = getProjectTasksUseCase(projectId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { filteredTasks ->
            filteredTasks.size == 2 && filteredTasks.all { it.projectId == projectId }
        }

        verify(exactly = 1) { taskRepository.getAllTasks() }
    }

    @Test
    fun `should return empty list when no tasks exist for the project`() {
        every { taskRepository.getAllTasks() } returns Single.just(tasks)

        val testObserver: TestObserver<List<Task>> = getProjectTasksUseCase(Uuid.random()).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { it.isEmpty() }

        verify(exactly = 1) { taskRepository.getAllTasks() }
    }

    @Test
    fun `should return empty list when no tasks exist at all`() {
        every { taskRepository.getAllTasks() } returns Single.just(emptyList())

        val testObserver: TestObserver<List<Task>> = getProjectTasksUseCase(projectId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { it.isEmpty() }

        verify(exactly = 1) { taskRepository.getAllTasks() }
    }

    @Test
    fun `should propagate NoTasksFoundException from repository`() {
        val exception = NoTasksFoundException()
        every { taskRepository.getAllTasks() } returns Single.error(exception)

        val testObserver: TestObserver<List<Task>> = getProjectTasksUseCase(projectId).test()

        testObserver.assertError(NoTasksFoundException::class.java)
        testObserver.assertNotComplete()

        verify(exactly = 1) { taskRepository.getAllTasks() }
    }

    @Test
    fun `should handle repository runtime exceptions`() {
        val errorMessage = "Database connection failed"
        val runtimeException = RuntimeException(errorMessage)
        every { taskRepository.getAllTasks() } returns Single.error(runtimeException)

        val testObserver: TestObserver<List<Task>> = getProjectTasksUseCase(projectId).test()

        testObserver.assertError(RuntimeException::class.java)
        testObserver.assertNotComplete()

        verify(exactly = 1) { taskRepository.getAllTasks() }
    }
}
