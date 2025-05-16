package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.DeleteProjectUseCase
import org.example.logic.useCase.GetProjectByIdUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteProjectUseCaseTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var getProjectByIdUseCase: GetProjectByIdUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var deleteProjectUseCase: DeleteProjectUseCase

    private val testProject =
        Project(
            id = Uuid.random(),
            name = "Test Project",
        )

    @BeforeEach
    fun setUp() {
        projectRepository = mockk()
        getProjectByIdUseCase = mockk()
        createAuditLogUseCase = mockk()
        deleteProjectUseCase =
            DeleteProjectUseCase(
                projectRepository,
                getProjectByIdUseCase,
                createAuditLogUseCase,
            )
    }

    @Test
    fun `should delete project and create audit log`() {
        every { getProjectByIdUseCase(testProject.id) } returns Single.just(testProject)
        every { projectRepository.deleteProject(testProject.id) } returns Completable.complete()
        every {
            createAuditLogUseCase.logDeletion(
                AuditLog.EntityType.PROJECT,
                testProject.id,
                testProject.name,
            )
        } returns Single.just(mockk())

        val testObserver = deleteProjectUseCase(testProject.id).test()

        testObserver.assertComplete()
        verify { projectRepository.deleteProject(testProject.id) }
        verify {
            createAuditLogUseCase.logDeletion(
                AuditLog.EntityType.PROJECT,
                testProject.id,
                testProject.name,
            )
        }
    }
}
