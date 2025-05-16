package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import mockdata.createTask
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.GetTasksByProjectStateUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetTasksByProjectStateUseCaseTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var getTasksByProjectStateUseCase: GetTasksByProjectStateUseCase
    private val stateId = Uuid.random()

    @BeforeEach
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        getTasksByProjectStateUseCase = GetTasksByProjectStateUseCase(taskRepository)
    }

    @Test
    fun `should return tasks when tasks exist for the given state`() {
        val tasks =
            listOf(
                createTask(stateId, "task1"),
                createTask(stateId, "task2"),
            )
        every { taskRepository.getTasksByProjectState(stateId) } returns Single.just(tasks)

        val testObserver: TestObserver<List<Task>> = getTasksByProjectStateUseCase(stateId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == tasks
        }
    }

    @Test
    fun `should return empty list when no tasks exist for the given state`() {
        every { taskRepository.getTasksByProjectState(stateId) } returns Single.just(emptyList())

        val testObserver: TestObserver<List<Task>> = getTasksByProjectStateUseCase(stateId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.isEmpty()
        }
    }
}
