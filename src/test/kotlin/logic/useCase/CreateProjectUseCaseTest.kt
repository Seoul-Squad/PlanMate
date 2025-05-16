package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.Project
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectRepository
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.CreateProjectUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.ProjectCreationFailedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateProjectUseCaseTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var validation: Validation
    private lateinit var createProjectUseCase: CreateProjectUseCase

    private val projectName = "Test Project"
    private val dummyProjectId = Uuid.random()
    private val dummyProject = Project(id = dummyProjectId, name = projectName)

    @BeforeEach
    fun setUp() {
        projectRepository = mockk(relaxed = true)
        createAuditLogUseCase = mockk(relaxed = true)
        projectStateRepository = mockk(relaxed = true)
        validation = mockk(relaxed = true)
        createProjectUseCase =
            CreateProjectUseCase(
                projectRepository,
                createAuditLogUseCase,
                projectStateRepository,
                validation,
            )
    }

    @Test
    fun `should return created project when input is valid`() {
        every { validation.validateProjectNameOrThrow(projectName) } returns Unit

        every { projectRepository.createProject(any()) } returns Single.just(dummyProject)

        every {
            createAuditLogUseCase.logCreation(any(), any(), any())
        } returns
            Single.just(
                AuditLog(
                    userId = Uuid.random(),
                    userName = "Test User",
                    entityId = dummyProjectId,
                    entityType = AuditLog.EntityType.PROJECT,
                    entityName = projectName,
                    actionType = AuditLog.ActionType.CREATE,
                ),
            )

        every { projectStateRepository.createProjectState(any()) } returns
            Single.just(
                ProjectState(title = "To Do", projectId = dummyProjectId),
            )

        createProjectUseCase(projectName).blockingGet()

        verify { validation.validateProjectNameOrThrow(projectName) }
        verify { projectRepository.createProject(any()) }
        verify { createAuditLogUseCase.logCreation(any(), any(), any()) }
        verify(exactly = 3) { projectStateRepository.createProjectState(any()) }
    }

    @Test
    fun `should throw ProjectCreationFailedException when projectName is too long`() {
        val projectName = "a".repeat(100)
        every { validation.validateProjectNameOrThrow(projectName) } throws ProjectCreationFailedException()

        assertThrows<ProjectCreationFailedException> {
            createProjectUseCase(projectName).blockingGet()
        }
    }
}
