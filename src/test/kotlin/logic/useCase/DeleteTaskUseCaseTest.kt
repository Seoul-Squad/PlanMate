package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.DeleteTaskUseCase
import org.example.logic.useCase.GetTaskByIdUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteTaskUseCaseTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private val testTask = Task(
        id = Uuid.random(),
        name = "Test Task",
        stateId = Uuid.random(),
        stateName = "To Do",
        addedById = Uuid.random(),
        addedByName = "Test User",
        projectId = Uuid.random()
    )

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        getTaskByIdUseCase = mockk()
        createAuditLogUseCase = mockk()
        deleteTaskUseCase = DeleteTaskUseCase(
            taskRepository = taskRepository,
            getTaskByIdUseCase = getTaskByIdUseCase,
            createAuditLogUseCase = createAuditLogUseCase
        )
    }

    @Test
    fun `should delete task and create audit log`() {
        every { getTaskByIdUseCase(testTask.id) } returns Single.just(testTask)
        every { taskRepository.deleteTask(testTask.id) } returns Completable.complete()
        every {
            createAuditLogUseCase.logDeletion(
                AuditLog.EntityType.TASK,
                testTask.id,
                testTask.name,
            )
        } returns Single.just(mockk())

        val testObserver = deleteTaskUseCase(testTask.id).test()

        testObserver.assertComplete()
        verify { taskRepository.deleteTask(testTask.id) }
        verify {
            createAuditLogUseCase.logDeletion(
                AuditLog.EntityType.TASK,
                testTask.id,
                testTask.name,
            )
        }
    }
}
