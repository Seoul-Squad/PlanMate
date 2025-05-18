package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import mockdata.createTask
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.GetTaskByIdUseCase
import org.example.logic.utils.TaskNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetTaskByIdUseCaseTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private val ids = List(6) { Uuid.random() }

    @BeforeEach
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        getTaskByIdUseCase = GetTaskByIdUseCase(taskRepository)
    }

    @Test
    fun `should return task by ID when task exists`() {
        val taskID = ids[0]
        val expectedTask = createTask(taskID, "task")
        every { taskRepository.getTaskById(taskID) } returns Single.just(expectedTask)

        val testObserver: TestObserver<Task> = getTaskByIdUseCase(taskID).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == expectedTask
        }
    }

    @Test
    fun `should throw TaskNotFoundException when task does not exist`() {
        val taskID = ids[1]
        // Assuming repository returns Single.just(null) or you adapt it to Single.error for not found
        every { taskRepository.getTaskById(taskID) } returns Single.error(TaskNotFoundException())

        val testObserver: TestObserver<Task> = getTaskByIdUseCase(taskID).test()

        testObserver.assertError(TaskNotFoundException::class.java)
        testObserver.assertNotComplete()
    }

    @Test
    fun `should return task when it exists`() {
        val projectUuid = Uuid.random()
        val expectedTask = createTask(projectUuid, "task")
        every { taskRepository.getTaskById(projectUuid) } returns Single.just(expectedTask)

        val testObserver: TestObserver<Task> = getTaskByIdUseCase(projectUuid).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == expectedTask
        }
    }
}
