package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.useCase.GetProjectByIdUseCase
import org.example.logic.utils.ProjectNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectByIdUseCaseTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var getProjectByIdUseCase: GetProjectByIdUseCase
    private val ids = List(6) { Uuid.random() }
    private val project =
        Project(
            id = ids[1],
            name = "spacecraft work",
        )

    @BeforeEach
    fun setUp() {
        projectRepository = mockk(relaxed = true)
        getProjectByIdUseCase = GetProjectByIdUseCase(projectRepository)
    }

    @Test
    fun `should return project when pass valid id`() {
        val projectId = ids[4]
        every { projectRepository.getProjectById(projectId) } returns Single.just(project)

        val testObserver: TestObserver<Project> = getProjectByIdUseCase(projectId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(project)
    }

    @Test
    fun `should emit error when project not found`() {
        val projectId = ids[3]
        val error = ProjectNotFoundException()
        every { projectRepository.getProjectById(projectId) } returns Single.error(error)

        val testObserver: TestObserver<Project> = getProjectByIdUseCase(projectId).test()

        testObserver.assertError(ProjectNotFoundException::class.java)
        testObserver.assertNotComplete()
    }
}
