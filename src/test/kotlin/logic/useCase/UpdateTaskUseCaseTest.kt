package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.UpdateTaskUseCase
import org.example.logic.utils.TaskNotChangedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateTaskUseCaseTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase

    private val taskId = Uuid.random()
    private val stateId = Uuid.random()
    private val addedById = Uuid.random()
    private val projectId = Uuid.random()

    private fun createTask(name: String) =
        Task(
            id = taskId,
            name = name,
            stateId = stateId,
            stateName = "State",
            addedById = addedById,
            addedByName = "User",
            projectId = projectId,
        )

    @BeforeEach
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        createAuditLogUseCase = mockk(relaxed = true)
        updateTaskUseCase = UpdateTaskUseCase(taskRepository, createAuditLogUseCase)
    }

    @Test
    fun `should update task when changes detected and create audit log`() {
        val existingTask = createTask("Old Task")
        val updatedTask = existingTask.copy(name = "New Task")

        every { taskRepository.getTaskById(taskId) } returns Single.just(existingTask)
        every { taskRepository.updateTask(updatedTask) } returns Single.just(updatedTask)
        every {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.TASK,
                entityId = taskId,
                entityName = "New Task",
                fieldChange =
                    match {
                        it.fieldName == "name" && it.oldValue == "Old Task" && it.newValue == "New Task"
                    },
            )
        } returns mockk(relaxed = true)

        val testObserver = updateTaskUseCase(updatedTask).test()

        testObserver
            .assertValue(updatedTask)
            .assertComplete()
            .assertNoErrors()

        verify(exactly = 1) {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.TASK,
                entityId = taskId,
                entityName = "New Task",
                fieldChange =
                    match {
                        it.fieldName == "name" && it.oldValue == "Old Task" && it.newValue == "New Task"
                    },
            )
        }
    }

    @Test
    fun `should throw TaskNotChangedException when task has no changes`() {
        val existingTask = createTask("Same Task")
        val updatedTask = existingTask.copy() // no changes

        every { taskRepository.getTaskById(taskId) } returns Single.just(existingTask)

        assertThrows<TaskNotChangedException> {
            updateTaskUseCase(updatedTask).blockingGet()
        }

        verify(exactly = 0) { taskRepository.updateTask(any()) }
        verify(exactly = 0) { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) }
    }
}
