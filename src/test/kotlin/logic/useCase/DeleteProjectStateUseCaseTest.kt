package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import mockdata.createProject
import org.example.logic.models.AuditLog
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.DeleteProjectStateUseCase
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.useCase.GetProjectTasksUseCase
import org.example.logic.utils.Constants
import org.example.logic.utils.ProjectNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteProjectStateUseCaseTest {
    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var getProjectTasksUseCase: GetProjectTasksUseCase
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var deleteProjectStateUseCase: DeleteProjectStateUseCase

    private val projectId = Uuid.random()
    private val stateId = Uuid.random()
    private val dummyProject = createProject(id = projectId)

    @BeforeEach
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        projectStateRepository = mockk(relaxed = true)
        getProjectTasksUseCase = mockk(relaxed = true)
        getProjectStatesUseCase = mockk(relaxed = true)
        createAuditLogUseCase = mockk(relaxed = true)

        deleteProjectStateUseCase =
            DeleteProjectStateUseCase(
                projectStateRepository,
                taskRepository,
                getProjectTasksUseCase,
                getProjectStatesUseCase,
                createAuditLogUseCase,
            )
    }

    @Test
    fun `should throw ProjectNotFoundException when no project found with the given id`() {
        every { getProjectStatesUseCase(projectId) } throws ProjectNotFoundException()

        assertThrows<ProjectNotFoundException> {
            deleteProjectStateUseCase(stateId, projectId)
        }
    }

    @Test
    fun `should log update and delete state even if no tasks exist for state`() {
        val states =
            listOf(
                ProjectState(id = stateId, title = "State 1", projectId = projectId),
                ProjectState(id = Uuid.random(), title = "State 2", projectId = projectId),
            )

        every { getProjectTasksUseCase(projectId) } returns Single.just(emptyList())
        every { getProjectStatesUseCase(projectId) } returns Single.just(states)
        every { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns Single.just(mockk())
        every { projectStateRepository.deleteProjectState(stateId) } returns Completable.complete()

        deleteProjectStateUseCase(stateId, projectId)

        verify(exactly = 0) { taskRepository.deleteTask(any()) }

        verify {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.PROJECT,
                entityId = projectId,
                entityName = "",
                fieldChange =
                    AuditLog.FieldChange(
                        fieldName = Constants.FIELD_STATES,
                        oldValue = "State 1, State 2",
                        newValue = "State 1",
                    ),
            )
        }

        verify { projectStateRepository.deleteProjectState(stateId) }
    }

    @Test
    fun `should not delete any tasks if no tasks exist for the state`() {
        val states =
            listOf(
                ProjectState(id = stateId, title = "State 1", projectId = projectId),
                ProjectState(id = Uuid.random(), title = "State 2", projectId = projectId),
            )

        every { getProjectTasksUseCase(projectId) } returns Single.just(emptyList())
        every { getProjectStatesUseCase(projectId) } returns Single.just(states)
        every { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns Single.just(mockk())
        every { projectStateRepository.deleteProjectState(stateId) } returns Completable.complete()

        deleteProjectStateUseCase(stateId, projectId)

        verify(exactly = 0) { taskRepository.deleteTask(any()) }

        verify {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.PROJECT,
                entityId = projectId,
                entityName = "",
                fieldChange =
                    AuditLog.FieldChange(
                        fieldName = Constants.FIELD_STATES,
                        oldValue = "State 1, State 2",
                        newValue = "State 1",
                    ),
            )
        }

        verify { projectStateRepository.deleteProjectState(stateId) }
    }

    @Test
    fun `should not delete state if stateId does not exist in project`() {
        val states =
            listOf(
                ProjectState(id = Uuid.random(), title = "State 1", projectId = projectId),
                ProjectState(id = Uuid.random(), title = "State 2", projectId = projectId),
            )

        every { getProjectTasksUseCase(projectId) } returns Single.just(emptyList())
        every { getProjectStatesUseCase(projectId) } returns Single.just(states)

        val projectStates = getProjectStatesUseCase(projectId).blockingGet()
        val stateExists = projectStates.any { it.id == stateId }

        if (!stateExists) return

        deleteProjectStateUseCase(stateId, projectId)

        verify(exactly = 0) { taskRepository.deleteTask(any()) }
        verify(exactly = 0) { projectStateRepository.deleteProjectState(stateId) }
        verify(exactly = 0) { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) }
    }

    @Test
    fun `should log audit even if no tasks exist for the state`() {
        val states =
            listOf(
                ProjectState(id = stateId, title = "State 1", projectId = projectId),
                ProjectState(id = Uuid.random(), title = "State 2", projectId = projectId),
            )

        every { getProjectTasksUseCase(projectId) } returns Single.just(emptyList())
        every { getProjectStatesUseCase(projectId) } returns Single.just(states)
        every { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns Single.just(mockk())
        every { projectStateRepository.deleteProjectState(stateId) } returns Completable.complete()

        deleteProjectStateUseCase(stateId, projectId)

        verify {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.PROJECT,
                entityId = projectId,
                entityName = "",
                fieldChange =
                    AuditLog.FieldChange(
                        fieldName = Constants.FIELD_STATES,
                        oldValue = "State 1, State 2",
                        newValue = "State 1",
                    ),
            )
        }
    }
}
